package com.example.tetris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GameScreen()
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
fun GameScreen() {
    var board by remember { mutableStateOf(Board()) }
    var current by remember { mutableStateOf(spawnPiece(board)) }
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            if (!gameOver) {
                val moved = current.copy(origin = Point(current.origin.x, current.origin.y + 1))
                if (board.collides(pieceCells(moved))) {
                    board.lock(current)
                    val cleared = board.clearLines()
                    score += when (cleared) { 1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0 }
                    current = spawnPiece(board)
                    if (board.collides(pieceCells(current))) {
                        gameOver = true
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
            text = if (gameOver) "Game Over - Puntos: $score" else "Puntos: $score",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        BoardView(board = board, active = current)
        Controls(
            onLeft = { tryMove(board, current, -1, 0)?.let { current = it } },
            onRight = { tryMove(board, current, 1, 0)?.let { current = it } },
            onDown = { tryMove(board, current, 0, 1)?.let { current = it } },
            onRotate = { tryRotate(board, current)?.let { current = it } },
            onDrop = {
                var p = current
                while (true) {
                    val n = p.copy(origin = Point(p.origin.x, p.origin.y + 1))
                    if (board.collides(pieceCells(n))) break
                    p = n
                }
                current = p
            },
            onReset = {
                board = Board()
                current = spawnPiece(board)
                score = 0
                gameOver = false
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
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ControlButton("⟵", onLeft)
        ControlButton("⟶", onRight)
        ControlButton("⟱", onDown)
        ControlButton("⟳", onRotate)
        ControlButton("DROP", onDrop)
        ControlButton("RESET", onReset)
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
