@echo off
rem Captures the 4 Play Store screens of one game from the connected phone or emulator into store\<module>\raw\.
rem Usage, from the repo root:  store\capture.bat crown-logic
if "%~1"=="" (echo Usage: store\capture.bat MODULE   e.g. store\capture.bat arrow-escape & exit /b 1)
if not exist "%~dp0%~1\" (echo No such game folder: %~dp0%~1 & exit /b 1)
adb get-state >nul 2>&1 || (echo No phone found. Connect it with USB debugging on and check "adb devices". & exit /b 1)
if not exist "%~dp0%~1\raw\" mkdir "%~dp0%~1\raw"
call :shot %1 1 "the HOME screen"
call :shot %1 2 "a board in the MIDDLE of a game"
call :shot %1 3 "the WIN screen with stars"
call :shot %1 4 "the DAILY streak screen (THEMES screen for block-puzzle, solitaire, merge-2048)"
echo.
echo Done: 4 captures in store\%1\raw
exit /b 0

:shot
echo.
echo On the phone, open %~3, then come back here.
pause
adb exec-out screencap -p > "%~dp0%~1\raw\%2.png"
exit /b 0
