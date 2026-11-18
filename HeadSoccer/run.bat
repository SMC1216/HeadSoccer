@echo off
echo ====================================================
echo         Iniciando Head Soccer - DOPO
echo ====================================================
javac -encoding UTF-8 -d . -cp . dominio/*.java presentacion/*.java
if %ERRORLEVEL% equ 0 (
    java -cp . presentacion.GameWindow
) else (
    echo Error durante la compilacion.
    pause
)
