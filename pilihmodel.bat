@echo off
REM Merangkum hasil tahap 1 (rata-rata cost per model per peta).
REM Tidak memilih model. Keputusan model terbaik ada di tangan kelompok.
cd /d "%~dp0"
java -cp out experiment.PilihModel results\tahap1.csv
pause
