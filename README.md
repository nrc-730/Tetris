# Falling Blocks (Android)

Un clon minimalista de bloques cayendo inspirado en mecánicas clásicas, implementado en Kotlin con Jetpack Compose. No usa marcas registradas ni assets de terceros.

## Estado
- Motor básico: piezas, rotación, colisión, limpieza de líneas, puntuación.
- Controles táctiles: izquierda, derecha, abajo, rotar, caída dura, reinicio.

## Requisitos
- Android Studio Koala+ (AGP 8.5+)
- JDK 17
- Dispositivo o emulador Android con API 24+

## Abrir y ejecutar
1. Abrir este folder como proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar la configuración `app` en un emulador o dispositivo.

## Notas de marca y copyright
- “Tetris” es una marca registrada de The Tetris Company. Este proyecto evita usar ese nombre en el título de la app, iconografía o elementos de identificación. Usa el nombre genérico “Falling Blocks”.
- El código implementa lógicas de juego originales y no reutiliza código, assets o ROMs de terceros.

## Estructura
- `app/build.gradle.kts` — Configuración del módulo Android.
- `app/src/main/java/com/example/tetris/MainActivity.kt` — UI y lógica del juego.
- `app/src/main/AndroidManifest.xml` — Manifiesto.

## Próximos pasos (ideas)
- Sonidos y animaciones.
- Almacenamiento de mejores puntuaciones.
- Pausa, reanudar y niveles de dificultad.
- Input por gestos (swipe/drag) y gamepad.