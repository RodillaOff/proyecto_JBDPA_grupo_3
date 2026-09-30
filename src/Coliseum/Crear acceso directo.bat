@echo off
cd /d "%~dp0"
if not exist "coliseum.ico" (
    echo No se encontro coliseum.ico en esta carpeta.
    pause
    exit /b
)
if not exist "Coliseum Launcher.bat" (
    echo No se encontro "Coliseum Launcher.bat" en esta carpeta.
    pause
    exit /b
)
powershell -NoProfile -ExecutionPolicy Bypass -Command "$dir='%~dp0'.TrimEnd('\'); $sh=New-Object -ComObject WScript.Shell; $lnk=$sh.CreateShortcut([Environment]::GetFolderPath('Desktop')+'\Coliseum.lnk'); $lnk.TargetPath=$dir+'\Coliseum Launcher.bat'; $lnk.WorkingDirectory=$dir; $lnk.IconLocation=$dir+'\coliseum.ico'; $lnk.WindowStyle=7; $lnk.Description='Coliseum'; $lnk.Save()"
if %errorlevel% neq 0 (
    echo Hubo un error al crear el acceso directo.
    pause
    exit /b
)
echo Listo: se creo el acceso directo "Coliseum" en el Escritorio, con el logo.
pause
