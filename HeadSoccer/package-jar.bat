@echo off
setlocal
echo ====================================================
echo         Generando Archivo Ejecutable .JAR
echo ====================================================

REM Si javac o jar no estan en el PATH global, buscar en la ruta del JDK de BlueJ
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
    echo Empaquetando mediante Maven...
    mvn clean package -DskipTests
    echo Archivo generado en target\head-soccer-1.0.0.jar
) else (
    echo Empaquetando mediante herramientas JDK...
    if not exist bin mkdir bin
    javac -encoding UTF-8 -d bin dominio/*.java presentacion/*.java
    jar cfe HeadSoccer.jar presentacion.GameWindow -C bin .
    echo Archivo ejecutable generado con exito: HeadSoccer.jar
    echo Puedes ejecutarlo con doble clic o con: java -jar HeadSoccer.jar
)
pause
