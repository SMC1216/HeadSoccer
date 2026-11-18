@echo off
setlocal
echo ====================================================
echo         Ejecutando Pruebas Unitarias
echo ====================================================

REM Si javac o java no estan en el PATH global, buscar en la ruta del JDK de BlueJ
where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\BlueJ\jdk\bin\javac.exe" (
        set "PATH=C:\Program Files\BlueJ\jdk\bin;%PATH%"
    ) else if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javac.exe" (
            set "PATH=%JAVA_HOME%\bin;%PATH%"
        )
    )
)

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Detectado Maven en el sistema. Ejecutando con reporte JaCoCo...
    mvn clean test jacoco:report
) else (
    echo Compilando y ejecutando suite JUnit 4...
    javac -encoding UTF-8 -d . -cp ".;C:\Program Files\BlueJ\lib\junit-4.12.jar;C:\Program Files\BlueJ\lib\hamcrest-core-1.3.jar" dominio/*.java presentacion/*.java
    java -cp ".;C:\Program Files\BlueJ\lib\junit-4.12.jar;C:\Program Files\BlueJ\lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore dominio.HeadSoccerTest
)
pause
