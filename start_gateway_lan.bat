@echo off
setlocal
cd /d "%~dp0"

title Smart Locker LAN Gateway
echo ===================================================
echo   SMART LOCKER - WEBSITE TREN MANG WIFI/LAN
echo ===================================================
echo.

python -m HardwareCode.raspberry_gateway.run_lan %*

if errorlevel 1 (
    echo.
    echo Gateway khong khoi dong duoc. Kiem tra Python va dependencies.
    pause
)
