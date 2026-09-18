@echo off
cd /d "%~dp0"
echo Compilando Coliseum...
javac -cp ".;lib\mysql-connector-j-26.7.0.jar" Coliseum.java
if %errorlevel% neq 0 (
    echo.
    echo Hubo un error al compilar. Revisa el mensaje de arriba.
    pause
    exit /b
)
echo Listo. Abriendo Coliseum...
java -cp ".;lib\mysql-connector-j-26.7.0.jar" Coliseum
pause