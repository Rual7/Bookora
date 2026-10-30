FROM node:24 AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

FROM httpd:2.4
COPY docker/apache/httpd.conf /usr/local/apache2/conf/httpd.conf
COPY --from=build /app/dist /usr/local/apache2/htdocs/
COPY docker/apache/.htaccess /usr/local/apache2/htdocs/.htaccess
