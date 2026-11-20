@echo off
REM Memilih model terbaik tahap 1 dan menyimpan hasilnya ke results\keputusan_tahap1.csv
REM Pemakaian: keputusan.bat [rasio|rata] [jumlah_pilihan]
cd /d "%~dp0"
java -cp out experiment.KeputusanTahap1 results\rata_cost_tahap1.csv %1 %2
pause
