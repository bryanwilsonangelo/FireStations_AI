@echo off
REM Contoh: run.bat maps\tiny\n20_acak_01.txt budget=5000 seed=0
cd /d "%~dp0"

if "%~1"=="" (
    echo Pemakaian: run.bat ^<file_peta^> [key=value ...]
    echo Contoh   : run.bat maps\tiny\n20_acak_01.txt budget=5000 seed=0
    pause
    exit /b 1
)

if not exist out (
    echo Folder out tidak ditemukan. Jalankan a.bat dulu.
    pause
    exit /b 1
)

java -cp out Main %*
echo.
pause
