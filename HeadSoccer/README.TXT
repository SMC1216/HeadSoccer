========================================================================
                      PROYECTO HEAD SOCCER - DOPO
========================================================================

TÍTULO DEL PROYECTO:
Head Soccer 2D - Juego de Fútbol Cabezón con Programación Orientada a Objetos

PROPÓSITO DEL PROYECTO:
Implementar un videojuego 2D interactivo en Java aplicando arquitectura en capas
(Dominio y Presentación), principios de diseño SOLID, persistencia de datos,
manejo estructurado de excepciones y pruebas unitarias con JUnit 4.

VERSIÓN O FECHA:
Noviembre 2026

CÓMO INICIAR EL PROYECTO:
- En BlueJ:
  1. Abrir el proyecto cargando el archivo "package.bluej".
  2. Entrar al paquete "presentacion".
  3. Hacer clic derecho sobre la clase "GameWindow" y seleccionar "void main(String[] args)".
- Desde Consola:
  javac -cp . dominio/*.java presentacion/*.java
  java -cp . presentacion.GameWindow

AUTORES:
Estudiantes del curso Diseño y Programación Orientada a Objetos (DOPO)

INSTRUCCIONES DE USUARIO Y CONTROLES:
- Movimiento horizontal: Teclas [A] (Izquierda) y [D] (Derecha), o flechas del teclado.
- Salto: Tecla [W] o flecha arriba.
- Patada al balón: Barra espaciadora [ESPACIO].
- Habilidad Especial: Tecla [E] (requiere medidor de poder al 100%).
- Menú Archivo:
  * Nuevo Partido: Reinicia el partido con marcador 0-0 y reloj en 90 segundos.
  * Abrir: Carga una partida guardada previamente desde un archivo serializado.
  * Salvar: Guarda el estado actual del partido (marcador, tiempo, posiciones, energía).
  * Salir: Confirma y cierra la aplicación.
- Menú Edición:
  * Cambiar Color Cancha: Permite personalizar el color del césped con JColorChooser.

ARQUITECTURA DEL PROYECTO:
1. Paquete "dominio":
   - GameObject: Clase base abstracta de entidades con posición y dimensiones.
   - Bounds: Hitbox rectangular para detección física independiente de librerías GUI.
   - ICollidable: Interfaz de objetos con detección de colisión y respuesta a impactos.
   - Character: Clase abstracta para personajes móviles (físicas, salto, patada, energía).
   - Player: Personaje controlado por el usuario mediante teclado (habilidad FireStrike).
   - BotAI: Oponente controlado por computadora con heurística defensiva y Teleport.
   - Ball: Físicas del balón (gravedad, rebotes elásticos, fricción y supertiro).
   - Goal: Arcos con travesaños y detección reglamentaria de gol.
   - Ability (Abstracta): Estrategia polimórfica para poderes especiales.
   - FireStrike, Teleport, GiantKick: Habilidades especiales concretas (OCP).
   - MatchManager: Fachada coordinadora del partido, reloj, marcador y colisiones.
   - MatchPersistence: Servicio de persistencia y serialización en disco (SRP).
   - HeadSoccerException: Excepción de dominio para validaciones y persistencia.
   - HeadSoccerTest: Batería de pruebas unitarias automatizadas con JUnit 4.

2. Paquete "presentacion":
   - GameWindow: Ventana principal JFrame que integra el HUD, lienzo FieldCanvas y controladores.
   - FieldCanvas: Lienzo gráfico personalizado con renderizado procedural (estadio, gradas,
     público, arcos, personajes y balón animado).
========================================================================
