@echo off
chcp 65001 >nul
setlocal
where node >nul 2>nul
if errorlevel 1 (
  echo Node.jsが見つかりません。
  echo READMEの「共通の事前準備」を確認してください。
  pause
  exit /b 1
)
node "%~dp0scripts\launcher.mjs" start
set "launcher_result=%errorlevel%"
pause
exit /b %launcher_result%
