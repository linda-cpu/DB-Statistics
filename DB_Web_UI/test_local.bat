@echo off
set IMAGE_NAME=db-frontend
set CONTAINER_NAME=db-frontend

echo [1/3] Stoppe alten Test-Container (falls vorhanden)...
docker stop %CONTAINER_NAME% 2>nul
docker rm %CONTAINER_NAME% 2>nul

echo [2/3] Baue Docker Image lokal...
docker build -t %IMAGE_NAME% .

echo [3/3] Starte Container auf http://localhost ...
docker run -d -p 80:80 --name %CONTAINER_NAME% %IMAGE_NAME%

echo.
echo ======================================================
echo ERFOLG! Dein lokaler Docker-Test laeuft.
echo Oeffne: http://localhost
echo ======================================================
echo.
echo Druecke eine Taste, um den Container wieder zu stoppen...
pause

echo Stoppe Test-Container...
docker stop %CONTAINER_NAME%
docker rm %CONTAINER_NAME%
echo Test beendet.
