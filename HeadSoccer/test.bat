@echo off
echo ====================================================
echo         Ejecutando Pruebas Unitarias
echo ====================================================
REM Ejecuta las pruebas automatizadas del dominio
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Detectado Maven en el sistema. Ejecutando con reporte JaCoCo...
    mvn clean test jacoco:report
) else (
    echo Ejecutando con suite JUnit...
    javac -encoding UTF-8 -d . -cp ".;C:\Program Files\BlueJ\lib\junit-4.12.jar;C:\Program Files\BlueJ\lib\hamcrest-core-1.3.jar" dominio/*.java presentacion/*.java
    java -cp ".;C:\Program Files\BlueJ\lib\junit-4.12.jar;C:\Program Files\BlueJ\lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore dominio.HeadSoccerTest
)
pause
