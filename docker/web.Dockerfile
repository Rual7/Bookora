FROM node:24 AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
# the public CAPTCHA key is baked into the page at build time
ARG VITE_TURNSTILE_SITE_KEY
RUN npm run build

FROM httpd:2.4
COPY docker/apache/httpd.conf /usr/local/apache2/conf/httpd.conf
COPY --from=build /app/dist /usr/local/apache2/htdocs/
COPY docker/apache/.htaccess /usr/local/apache2/htdocs/.htaccess
# Defaults for docker compose; a host such as Render sets its own PORT and BACKEND_HOST
ENV PORT=80 BACKEND_HOST=backend:8080
