@echo off

taskkill /FI "WINDOWTITLE eq MC Test Server" /F >nul 2>&1

timeout /t 2 /nobreak >nul

start "MC Test Server" cmd /k "cd /d server && java -Xmx2G -jar server.jar nogui"