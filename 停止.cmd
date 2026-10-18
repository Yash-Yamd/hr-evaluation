@echo off
chcp 65001 >nul
setlocal
where node >nul 2>nul
if errorlevel 1 (
  echo Node.jsが見つかりません。
  pause
  exit /b 1
)
node "%~dp0scripts\launcher.mjs" stop
set "launcher_result=%errorlevel%"
pause
exit /b %launcher_result%
