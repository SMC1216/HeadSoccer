@echo off
setlocal
echo ====================================================
echo         Iniciando Head Soccer - DOPO
echo ====================================================

REM Buscar javac en el sistema o en BlueJ
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

echo Compilando codigo fuente del videojuego...
javac -encoding UTF-8 -d . -cp . dominio/GameObject.java dominio/Bounds.java dominio/ICollidable.java dominio/Character.java dominio/Player.java dominio/BotAI.java dominio/Ball.java dominio/Goal.java dominio/Ability.java dominio/FireStrike.java dominio/Teleport.java dominio/GiantKick.java dominio/HeadSoccerException.java dominio/MatchPersistence.java dominio/MatchManager.java presentacion/*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilacion exitosa. Abriendo Head Soccer...
    start java -cp . presentacion.GameWindow
) else (
    echo.
    echo Error durante la compilacion.
    pause
)
