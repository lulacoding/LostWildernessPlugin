@echo off
cd /d "%~dp0"
java -Xms1G -Xmx2G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -jar paper-1.21.11-126.jar nogui
pause
