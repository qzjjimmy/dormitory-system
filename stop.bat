@echo off
title Dormitory System - Stop
echo ============================================
echo   Stopping Dormitory Management System
echo ============================================
echo.

echo Stopping backend (Spring Boot)...
taskkill /fi "WINDOWTITLE eq Dormitory-Backend*" /f >nul 2>&1
taskkill /fi "WINDOWTITLE eq Dormitory-Frontend*" /f >nul 2>&1

echo Stopping Java processes on port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
)

echo Stopping Node processes on port 5173...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
)

echo.
echo ============================================
echo   All services stopped.
echo ============================================
echo.
timeout /t 2 >nul
