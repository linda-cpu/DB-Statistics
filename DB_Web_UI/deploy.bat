@echo off
:: --- KONFIGURATION ---
set IMAGE_NAME=db-frontend
set TAR_NAME=db-frontend.tar
set VM_USER=handke
set VM_IP=193.174.103.35
set VM_PATH=~/db-projekt/DB_Web_UI
:: ---------------------

echo [1/4] Baue Docker Image lokal...
docker build -t %IMAGE_NAME% .

echo.
echo [2/4] Speichere Image als %TAR_NAME%...
docker save -o %TAR_NAME% %IMAGE_NAME%

echo.
echo [3/4] Kopiere Datei auf die VM (Passwort erforderlich)...
scp %TAR_NAME% %VM_USER%@%VM_IP%:%VM_PATH%

echo.
echo [4/4] Remote Deployment (Passwort erforderlich)...
:: 1. Stoppen/Löschen Container
:: 2. Image laden
:: 3. Altes TAR löschen (Platz sparen)
:: 4. Neue Instanz starten
:: 5. "docker image prune" löscht die oben genannten "empty string" Images (Dangling)
ssh %VM_USER%@%VM_IP% "docker stop %IMAGE_NAME% 2>/dev/null || true && docker rm %IMAGE_NAME% 2>/dev/null || true && docker load -i %VM_PATH%/%TAR_NAME% && rm %VM_PATH%/%TAR_NAME% && docker run -d -p 8082:8082 --name %IMAGE_NAME% %IMAGE_NAME% && docker image prune -f"
echo.
echo === FERTIG! ===

echo Deine App ist nun unter http://%VM_IP%:8082 erreichbar.
pause
