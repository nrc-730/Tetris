# Soviet Tetris (Android)

Un clon minimalista de bloques cayendo inspirado en mecánicas clásicas, implementado en Kotlin con Jetpack Compose. No usa assets de terceros.

## Estado
- Motor: piezas, rotación, colisión, limpieza de líneas, puntuación y niveles (velocidad creciente).
- UI: siguiente pieza (NEXT), retener (HOLD), caída dura, reinicio.
- Sonido: bloqueo y limpieza con tonos del sistema.
- Récord local: guarda la mejor puntuación.

## Requisitos
- Android Studio Koala+ (AGP 8.5+)
- JDK 17
- Dispositivo o emulador Android con API 24+

## Abrir y ejecutar
1. Abrir este folder como proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar la configuración `app` en un emulador o dispositivo.

## Notas de marca y copyright
- “Tetris” es una marca registrada de The Tetris Company. Para uso público distribuible se recomienda evitar ese nombre. Aquí se usa “Soviet Tetris” solo como nombre visible; el paquete permanece genérico.
- El proyecto implementa lógicas originales y no reutiliza assets de terceros.

## Estructura
- `app/build.gradle.kts` — Configuración del módulo Android.
- `app/src/main/java/com/example/fallingblocks/MainActivity.kt` — UI y lógica del juego.
- `app/src/main/java/com/example/fallingblocks/ui/Theme.kt` — Tema Compose.
- `app/src/main/res/values/themes/themes.xml` — Tema Material3 XML.
- `app/src/main/res/values/colors.xml` — Paleta rojo/negro.
- `app/src/main/AndroidManifest.xml` — Manifiesto.

## Controles
- `⟵` izquierda, `⟶` derecha, `⟱` abajo, `⟳` rotar, `DROP` caída dura, `HOLD` retener, `RESET` reiniciar.