@echo off
set "PATH=C:\Program Files\BlueJ\jdk\bin;%PATH%"
javac -encoding UTF-8 -cp ".;C:\Program Files\BlueJ\lib\junit-4.12.jar;C:\Program Files\BlueJ\lib\hamcrest-core-1.3.jar" dominio/*.java
java -cp ".;C:\Program Files\BlueJ\lib\junit-4.12.jar;C:\Program Files\BlueJ\lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore dominio.HeadSoccerTest
pause
