@echo off
cd /d "%~dp0"
echo Compilando Coliseum...
javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-26.7.0.jar" Coliseum.java
if %errorlevel% neq 0 (
    echo.
    echo Hubo un error al compilar. Revisa el mensaje de arriba.
    pause
    exit /b
)
echo Listo. Abriendo Coliseum...
rem "start ... javaw" abre la app sin depender de esta ventana: se puede cerrar y la app sigue en segundo plano.
start "" javaw -cp ".;lib\mysql-connector-j-26.7.0.jar" Coliseum
