========================================================================
                      PROYECTO HEAD SOCCER - DOPO
========================================================================

TÍTULO DEL PROYECTO:
Head Soccer 2D - Videojuego de Fútbol Cabezón con Programación Orientada a Objetos

PROPÓSITO DEL PROYECTO:
Implementar un videojuego 2D interactivo en Java aplicando arquitectura en capas
(Dominio y Presentación), principios de diseño SOLID, persistencia de datos (I/O),
IA con tres dificultades reales, menú de pausa dinámico con ESC, pruebas automatizadas
con JUnit 5, cobertura de código con JaCoCo y soporte completo para Visual Studio Code.

VERSIÓN O FECHA:
Noviembre 2026

------------------------------------------------------------------------
CÓMO INICIAR EL PROYECTO:
------------------------------------------------------------------------
1. EN VISUAL STUDIO CODE:
   - Abrir la carpeta "HeadSoccer" en VS Code.
   - Instalar la extensión recomendada "Extension Pack for Java" (Microsoft).
   - Presionar [F5] para iniciar con depuración gracias a la configuración en ".vscode/launch.json".
   - O bien, abrir la clase "presentacion/GameWindow.java" y hacer clic en "Run" sobre el método main.

2. MEDIANTE SCRIPTS DIRECTOS (WINDOWS):
   - Doble clic en "run.bat" para compilar y ejecutar de inmediato.
   - Doble clic en "test.bat" para ejecutar la batería de pruebas unitarias.
   - Doble clic en "package-jar.bat" para generar el archivo ejecutable "HeadSoccer.jar".

3. MEDIANTE MAVEN:
   - Compilar y ejecutar:
     mvn compile exec:java -Dexec.mainClass="presentacion.GameWindow"
   - Generar el JAR ejecutable:
     mvn clean package
     El JAR generado se encontrará en "target/head-soccer-1.0.0.jar".
     Ejecución directa: java -jar target/head-soccer-1.0.0.jar

4. EN BLUEJ:
   - Abrir "package.bluej", entrar al paquete "presentacion", clic derecho en "GameWindow" -> "void main(String[] args)".

------------------------------------------------------------------------
INSTRUCCIONES DE USUARIO Y CONTROLES:
------------------------------------------------------------------------
- Movimiento:
  * Teclas [A] / Flecha Izquierda: Desplazar hacia la izquierda.
  * Teclas [D] / Flecha Derecha: Desplazar hacia la derecha.
  * Tecla [W] / Flecha Arriba: Saltar (cabecear y bloquear).
- Acciones con el balón:
  * Barra Espaciadora [ESPACIO]: Patear con fuerza hacia adelante.
  * Tecla [E]: Activar habilidad especial (requiere medidor al 100% y cooldown listo).
- Control de Partida y Menú de Pausa:
  * Tecla [ESC]: Pausa instantánea de físicas, tiempo e IA.
  * Opciones del Menú de Pausa (accesibles con clic del mouse o números 1 al 5):
    [1] Reanudar Partido (o presionar ESC de nuevo).
    [2] Guardar Partida (abre selector de archivos).
    [3] Cargar Partida (recupera marcador, tiempo, posiciones y dificultad).
    [4] Reiniciar Partido (comienza un nuevo encuentro 0-0 de 90s).
    [5] Salir del Juego.
- Pantalla de Fin de Partido (Resultados):
  * Al expirar los 90 segundos, se despliega automáticamente el marcador final,
    el veredicto de victoria/empate y los botones interactivos:
    [R] o [Enter]: Jugar de nuevo.
    [Q] o [ESC]: Salir del juego.

------------------------------------------------------------------------
INTELIGENCIA ARTIFICIAL MULTIDIFICULTAD (BotAI):
------------------------------------------------------------------------
El oponente cuenta con tres niveles reales de comportamiento táctico (seleccionables
desde el menú "Opciones -> Dificultad IA"):
1. FÁCIL:
   - Tiempo de reacción retardado (evalúa decisiones cada 6 fotogramas).
   - Rango de impacto y salto reducido; no anticipa trayectorias y no usa poderes.
2. NORMAL:
   - Reacción ágil con repliegue a portería si el balón está lejos.
   - Saltos de intercepción y uso del poder especial "Teleport" en situaciones de peligro.
3. DIFÍCIL:
   - Reacción instantánea fotograma a fotograma.
   - Predicción de trayectoria balística: calcula la posición futura del balón (X + V*t)
     y se posiciona estratégicamente detrás para rematar a gol.
   - Saltos ofensivos y activación quirúrgica de "Teleport" en contragolpes letales.

------------------------------------------------------------------------
PRUEBAS AUTOMATIZADAS Y COBERTURA JACOCO:
------------------------------------------------------------------------
- Suite JUnit 5 (Jupiter): "dominio/HeadSoccerJUnit5Test.java" (21 casos de prueba).
- Suite JUnit 4 (BlueJ): "dominio/HeadSoccerTest.java" (12 casos de prueba).
- Cómo ejecutar pruebas y generar reporte JaCoCo con Maven:
  mvn clean test jacoco:report
- Cómo visualizar el reporte JaCoCo:
  1. Abrir en el navegador el archivo: "target/site/jacoco/index.html"
  2. Muestra porcentaje de líneas e instrucciones cubiertas por clase y paquete.
  3. Las líneas en verde indican código ejecutado por las pruebas; amarillo indica
     ramas condicionales parcialmente exploradas; rojo indica código no alcanzado.

------------------------------------------------------------------------
ARQUITECTURA DEL PROYECTO:
------------------------------------------------------------------------
1. Paquete "dominio":
   - GameObject: Clase abstracta base (coordenadas, dimensiones, serializable).
   - Bounds: Hitboxes AABB sin dependencia de librerías GUI.
   - ICollidable: Interfaz de objetos con interacción física y rebote.
   - Character: Clase abstracta de personajes con físicas, patadas y barras de poder.
   - Player: Personaje controlado por el usuario (habilidad FireStrike).
   - BotAI: Oponente con 3 dificultades reales (Fácil, Normal, Difícil).
   - Ball: Físicas del balón (gravedad, fricción elástica, límites y supertiro).
   - Goal: Arcos con detección de gol bajo el travesaño y rebotes independientes.
   - Ability (Abstracta): Estrategia de habilidades con ciclo de cooldown y duración.
   - FireStrike, Teleport, GiantKick: Habilidades concretas (OCP).
   - MatchManager: Coordinador del partido, tiempo, marcador y colisiones.
   - MatchPersistence: Servicio de serialización y lectura de partidas en disco.
   - HeadSoccerException: Excepción de dominio para control de errores de I/O y reglas.
   - HeadSoccerJUnit5Test: Pruebas unitarias completas con JUnit 5 Jupiter.
   - HeadSoccerTest: Pruebas unitarias compatibles con JUnit 4.

2. Paquete "presentacion":
   - GameWindow: Ventana principal JFrame con HUD superior, menú interactivo de pausa
     por tecla ESC, selector de dificultad, pantalla de resultados y pie minimalista.
   - FieldCanvas: Lienzo gráfico personalizado con renderizado procedural 2D del
     estadio, tribunas, arcos, personajes y menú overlay interactivo con mouse.

------------------------------------------------------------------------
DIAGRAMAS ASTAH A ACTUALIZAR:
------------------------------------------------------------------------
En "Diagramas HeadSoccer.asta" se recomienda reflejar las siguientes incorporaciones:
- Clase "HeadSoccerException" en dominio.
- Clase "MatchPersistence" asociada a "MatchManager".
- Subclase "GiantKick" heredando de "Ability".
- Atributos y constantes de dificultad en "BotAI" (DIFFICULTY_EASY, NORMAL, HARD).
- Atributos de cooldown en "Ability" (cooldownRemaining, durationRemaining, isReady).
========================================================================
