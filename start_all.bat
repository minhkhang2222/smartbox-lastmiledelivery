@echo off
title Smart Locker System Starter
echo ===================================================
echo   KHOI CHAY TOAN BO HE THONG SMART LOCKER
echo ===================================================

echo.
echo 1. Dang khoi chay AI Microservice (Port 9001)...
start "AI Microservice" cmd /k "cd microservice\ApiLayer && python app.py"

echo.
echo 2. Dang khoi chay Spring Boot Backend (Port 8080)...
start "Spring Boot Backend" cmd /k "mvnw spring-boot:run"

echo.
echo 3. Dang khoi chay React Frontend (Port 5173)...
start "React Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo ===================================================
echo   Tat ca dich vu da duoc khoi chay trong cac cua so rieng!
echo ===================================================
pause
