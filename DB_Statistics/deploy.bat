@echo off
echo ==========================================
echo   DB STATISTICS - AUTO DEPLOYMENT
echo ==========================================

:: --- KONFIGURATION ---
:: Die IP aus deinem HTTP-Request file
set VM_IP=193.174.103.35
:: Dein Benutzername auf der VM (bitte anpassen, falls nicht root)
set VM_USER=handke
:: Der Zielordner auf dem Server
set REMOTE_DIR=/db-project/db_statistics
:: Name des Docker Containers und Images
set APP_NAME=db_statistics
:: Pfad zur lokalen JAR (Maven Standard)
set LOCAL_JAR=target\db_statistics-0.0.1-SNAPSHOT.jar

:: --- 1. PROJEKT BAUEN ---
echo.
echo [1/4] Baue Java Projekt (Maven)...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo FEHLER: Build fehlgeschlagen!
    pause
    exit /b %errorlevel%
)

:: --- 2. ORDNER AUF VM ERSTELLEN ---
echo.
echo [2/4] Bereite Server vor...
ssh %VM_USER%@%VM_IP% "mkdir -p %REMOTE_DIR%"

:: --- 3. DATEIEN KOPIEREN (SCP) ---
echo.
echo [3/4] Kopiere Dateien auf die VM...
:: Wir benennen die JAR beim Kopieren direkt in "app.jar" um, damit das Dockerfile immer gleich bleibt
scp %LOCAL_JAR% %VM_USER%@%VM_IP%:%REMOTE_DIR%/app.jar
scp Dockerfile %VM_USER%@%VM_IP%:%REMOTE_DIR%/Dockerfile

:: --- 4. DOCKER NEUSTARTEN (SSH) ---
echo.
echo [4/4] Starte Docker Container neu...
ssh %VM_USER%@%VM_IP% "cd %REMOTE_DIR% && docker build -t %APP_NAME% . && docker stop %APP_NAME% || true && docker rm %APP_NAME% || true && docker run -d -p 8080:8080 --name %APP_NAME% --restart unless-stopped %APP_NAME%"

echo.
echo ==========================================
echo   DEPLOYMENT ERFOLGREICH!
echo   App laeuft unter: http://%VM_IP%:8080
echo ==========================================
pause