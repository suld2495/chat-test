@echo off
echo ====================================
echo Supabase Connection Test
echo ====================================
echo.

echo [1] Basic Health Check...
curl -s http://localhost:8080/api/health
echo.
echo.

echo [2] Database Connection Test...
curl -s http://localhost:8080/api/health/db
echo.
echo.

echo [3] Tables Check...
curl -s http://localhost:8080/api/health/tables
echo.
echo.

echo ====================================
echo Test Complete
echo ====================================
pause
