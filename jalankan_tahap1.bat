@echo off
REM Menjalankan tahap 1 otomatis lalu merangkum hasilnya.
REM Butuh Python dan JDK terpasang.
cd /d "%~dp0"
python jalankan_tahap1.py %*
pause
