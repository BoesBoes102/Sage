@echo off

taskkill /FI "WINDOWTITLE eq MC Test Server" /F >nul 2>&1

ping 127.0.0.1 -n 3 >nul

start "MC Test Server" cmd /k "cd /d server && %USERPROFILE%\.jdks\ms-25.0.2\bin\java.exe -Xmx3G -jar server.jar nogui"