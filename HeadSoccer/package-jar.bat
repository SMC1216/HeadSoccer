@echo off
echo ====================================================
echo         Generando Archivo Ejecutable .JAR
echo ====================================================
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Empaquetando mediante Maven...
    mvn clean package -DskipTests
    echo Archivo generado en target\head-soccer-1.0.0.jar
) else (
    echo Empaquetando mediante herramientas JDK...
    javac -encoding UTF-8 -d bin dominio/*.java presentacion/*.java
    jar cfe HeadSoccer.jar presentacion.GameWindow -C bin .
    echo Archivo ejecutable generado: HeadSoccer.jar
    echo Puedes ejecutarlo con doble clic o con: java -jar HeadSoccer.jar
)
pause
