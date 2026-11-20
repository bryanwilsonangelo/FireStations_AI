@echo off
REM Menjalankan eksperimen tahap 1. Hasil ke results\tahap1.csv
cd /d "%~dp0"
java -cp out experiment.ExperimentRunner tahap1 %*
pause
