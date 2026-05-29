@echo off
title Dormitory System Launcher
echo ============================================
echo   Dormitory Management System Launcher
echo ============================================
echo.

echo [1/3] Starting backend (Spring Boot)...
start "Dormitory-Backend" cmd /c "cd /d %~dp0 && mvn spring-boot:run"
echo Backend starting on http://localhost:8080

echo [2/3] Starting frontend (Vue + Vite)...
start "Dormitory-Frontend" cmd /c "cd /d %~dp0frontend && npm run dev"
echo Frontend starting on http://localhost:5173

echo.
echo ============================================
echo   Startup complete!
echo.
echo   Frontend : http://localhost:5173
echo   Backend  : http://localhost:8080
echo.
echo   Demo accounts:
echo     admin       / 123456  (administrator)
echo     student     / 123456  (student)
echo     dormkeeper  / 123456  (dorm keeper)
echo ============================================
echo.
echo Press any key to close this window.
echo (Services will keep running in background)
pause >nul
