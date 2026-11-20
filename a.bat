@echo off
REM Kompilasi seluruh source di folder src ke folder out.
REM Aman untuk path yang mengandung spasi.
setlocal EnableDelayedExpansion
cd /d "%~dp0"

if not exist out mkdir out
if exist sources.txt del sources.txt

REM javac membaca backslash sebagai escape di file argumen,
REM jadi semua "\" diganti "/" sebelum ditulis.
for /r src %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!">>sources.txt
)

javac -encoding UTF-8 -d out @sources.txt
set HASIL=%ERRORLEVEL%

del sources.txt

if %HASIL% NEQ 0 (
    echo.
    echo Kompilasi GAGAL. Periksa pesan error di atas.
    pause
    exit /b 1
)

echo.
echo Kompilasi selesai.
pause
