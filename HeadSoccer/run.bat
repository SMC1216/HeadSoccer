@echo off
set "PATH=C:\Program Files\BlueJ\jdk\bin;%PATH%"
javac -encoding UTF-8 -cp ".;C:\Program Files\BlueJ\lib\junit-4.12.jar" dominio/*.java presentacion/*.java
start java -cp . presentacion.GameWindow
