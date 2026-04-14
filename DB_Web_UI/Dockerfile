FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Run
FROM nginx:alpine
# Kopiert die gebauten Dateien in den Nginx-Ordner
COPY --from=build /app/dist/DB_Web_UI/browser /usr/share/nginx/html
# Kopiert eine Nginx-Konfiguration (wichtig für Angular Routing!)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8082
CMD ["nginx", "-g", "daemon off;"]
