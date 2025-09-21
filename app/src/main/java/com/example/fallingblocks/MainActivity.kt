package com.example.fallingblocks

import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.fallingblocks.ui.FallingBlocksTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FallingBlocksTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GameScreen(appContext = applicationContext)
                }
            }
        }
    }
}

// --- Game logic (simplified falling blocks clone, not branded) ---

data class Point(val x: Int, val y: Int)

data class Tetromino(val cells: List<Point>)

enum class Shape { I, O, T, S, Z, J, L }

fun shapeCells(shape: Shape): List<Point> = when (shape) {
    Shape.I -> listOf(Point(-2,0), Point(-1,0), Point(0,0), Point(1,0))
    Shape.O -> listOf(Point(0,0), Point(1,0), Point(0,1), Point(1,1))
    Shape.T -> listOf(Point(-1,0), Point(0,0), Point(1,0), Point(0,1))
    Shape.S -> listOf(Point(0,0), Point(1,0), Point(-1,1), Point(0,1))
    Shape.Z -> listOf(Point(-1,0), Point(0,0), Point(0,1), Point(1,1))
    Shape.J -> listOf(Point(-1,0), Point(-1,1), Point(0,0), Point(1,0))
    Shape.L -> listOf(Point(1,1), Point(-1,0), Point(0,0), Point(1,0))
}

data class Piece(val shape: Shape, val origin: Point, val rotation: Int)

fun rotate(p: Point, r: Int): Point {
    var x = p.x
    var y = p.y
    repeat(((r % 4) + 4) % 4) {
        val nx = -y
        val ny = x
        x = nx
        y = ny
    }
    return Point(x, y)
}

fun pieceCells(piece: Piece): List<Point> = shapeCells(piece.shape).map { c ->
    val rc = rotate(c, piece.rotation)
    Point(rc.x + piece.origin.x, rc.y + piece.origin.y)
}

class Board(val width: Int = 10, val height: Int = 20) {
    private val grid: Array<IntArray> = Array(height) { IntArray(width) { 0 } }

    fun inside(p: Point) = p.x in 0 until width && p.y in 0 until height

    fun collides(cells: List<Point>): Boolean = cells.any { c -> !inside(c) || grid[c.y][c.x] != 0 }

    fun lock(piece: Piece) {
        pieceCells(piece).forEach { c -> grid[c.y][c.x] = piece.shape.ordinal + 1 }
    }

    fun clearLines(): Int {
        var cleared = 0
        val newRows = mutableListOf<IntArray>()
        for (y in 0 until height) {
            val row = grid[y]
            if (row.all { it != 0 }) {
                cleared++
            } else {
                newRows.add(row)
            }
        }
        repeat(cleared) { newRows.add(0, IntArray(width) { 0 }) }
        for (y in 0 until height) grid[y] = newRows[y]
        return cleared
    }

    fun cell(x: Int, y: Int) = grid[y][x]
}

@Composable
fun GameScreen(appContext: Context) {
    var board by remember { mutableStateOf(Board()) }
    var current by remember { mutableStateOf(spawnPiece(board)) }
    var next by remember { mutableStateOf(spawnPiece(board)) }
    var hold: Piece? by remember { mutableStateOf(null) }
    var canHold by remember { mutableStateOf(true) }
    var score by remember { mutableStateOf(0) }
    val prefs = remember { appContext.getSharedPreferences("scores", Context.MODE_PRIVATE) }
    var best by remember { mutableStateOf(prefs.getInt("best", 0)) }
    var lines by remember { mutableStateOf(0) }
    var level by remember { mutableStateOf(1) }
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        while (true) {
            val delayMs = (600L - (level - 1) * 40L).coerceAtLeast(120L)
            kotlinx.coroutines.delay(delayMs)
            if (!gameOver) {
                val moved = current.copy(origin = Point(current.origin.x, current.origin.y + 1))
                if (board.collides(pieceCells(moved))) {
                    board.lock(current)
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                    val cleared = board.clearLines()
                    lines += cleared
                    if (cleared > 0) tone.startTone(ToneGenerator.TONE_PROP_ACK, 80)
                    score += when (cleared) { 1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0 } * level
                    level = 1 + lines / 10
                    if (score > best) { best = score; prefs.edit().putInt("best", best).apply() }
                    current = next
                    next = spawnPiece(board)
                    canHold = true
                    if (board.collides(pieceCells(current))) {
                        gameOver = true
                        tone.startTone(ToneGenerator.TONE_PROP_NACK, 200)
                    }
                } else {
                    current = moved
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (gameOver) "Game Over - Puntos: $score  Nivel: $level  Líneas: $lines  Récord: $best" else "Puntos: $score  Nivel: $level  Líneas: $lines  Récord: $best",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            MiniBoard(title = "HOLD", piece = hold)
            BoardView(board = board, active = current)
            MiniBoard(title = "NEXT", piece = next)
        }
        Controls(
            onLeft = { tryMove(board, current, -1, 0)?.let { current = it } },
            onRight = { tryMove(board, current, 1, 0)?.let { current = it } },
            onDown = { tryMove(board, current, 0, 1)?.let { current = it } },
            onRotate = { tryRotate(board, current)?.let { current = it } },
            onDrop = { current = hardDrop(board, current) },
            onReset = {
                board = Board()
                current = spawnPiece(board)
                next = spawnPiece(board)
                hold = null
                canHold = true
                score = 0
                lines = 0
                level = 1
                gameOver = false
            },
            onHold = {
                if (canHold) {
                    val swapped = hold?.copy(origin = current.origin) ?: next.copy(origin = current.origin)
                    hold = current.copy(origin = Point(0,0), rotation = 0)
                    current = swapped
                    if (hold != null) {
                        // cuando ya había hold, no consumir next
                    } else {
                        next = spawnPiece(board)
                    }
                    canHold = false
                }
            }
        )
    }
}

fun spawnPiece(board: Board): Piece {
    val shapes = Shape.values()
    val s = shapes.random()
    return Piece(s, origin = Point(board.width / 2, 0), rotation = 0)
}

fun tryMove(board: Board, piece: Piece, dx: Int, dy: Int): Piece? {
    val moved = piece.copy(origin = Point(piece.origin.x + dx, piece.origin.y + dy))
    return if (board.collides(pieceCells(moved))) null else moved
}

fun tryRotate(board: Board, piece: Piece): Piece? {
    val r = (piece.rotation + 1) % 4
    val rotated = piece.copy(rotation = r)
    return if (board.collides(pieceCells(rotated))) null else rotated
}

fun hardDrop(board: Board, piece: Piece): Piece {
    var p = piece
    while (true) {
        val n = p.copy(origin = Point(p.origin.x, p.origin.y + 1))
        if (board.collides(pieceCells(n))) return p
        p = n
    }
}

@Composable
fun BoardView(board: Board, active: Piece) {
    val cellSize = 22.dp
    Column(modifier = Modifier.background(Color(0xFF111111)).padding(8.dp)) {
        for (y in 0 until board.height) {
            Row { for (x in 0 until board.width) {
                val occ = if (active.let { p -> pieceCells(p).any { it.x == x && it.y == y } }) active.shape.ordinal + 1 else board.cell(x, y)
                Box(
                    Modifier.size(cellSize)
                        .background(if (occ == 0) Color.DarkGray else colorFor(occ))
                )
            } }
        }
    }
}

fun colorFor(i: Int): Color = when ((i - 1 + 7) % 7) {
    0 -> Color.Cyan
    1 -> Color.Yellow
    2 -> Color.Magenta
    3 -> Color.Green
    4 -> Color.Red
    5 -> Color(0xFF4169E1)
    else -> Color(0xFFFFA500)
}

@Composable
fun Controls(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onDown: () -> Unit,
    onRotate: () -> Unit,
    onDrop: () -> Unit,
    onReset: () -> Unit,
    onHold: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ControlButton("⟵", onLeft)
        ControlButton("⟶", onRight)
        ControlButton("⟱", onDown)
        ControlButton("⟳", onRotate)
        ControlButton("DROP", onDrop)
        ControlButton("HOLD", onHold)
        ControlButton("RESET", onReset)
    }
}

@Composable
fun MiniBoard(title: String, piece: Piece?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        val size = 12.dp
        val cells = piece?.let { shapeCells(it.shape) } ?: emptyList()
        val minX = cells.minOfOrNull { it.x } ?: 0
        val minY = cells.minOfOrNull { it.y } ?: 0
        val normalized = cells.map { Point(it.x - minX, it.y - minY) }
        val w = (normalized.maxOfOrNull { it.x } ?: 2) + 1
        val h = (normalized.maxOfOrNull { it.y } ?: 2) + 1
        Column(modifier = Modifier.background(Color(0xFF111111)).padding(4.dp)) {
            for (y in 0 until h) {
                Row { for (x in 0 until w) {
                    val occ = normalized.any { it.x == x && it.y == y }
                    Box(Modifier.size(size).background(if (occ) Color.Red else Color.DarkGray))
                } }
            }
        }
    }
}

@Composable
fun ControlButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(width = 72.dp, height = 44.dp)
            .background(Color(0xFF333333))
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
    }
}
