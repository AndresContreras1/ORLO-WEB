#!/bin/sh
# Render entrega DATABASE_URL como postgresql://usuario:clave@host[:puerto]/base y Spring necesita
# jdbc:postgresql://host[:puerto]/base. El usuario y la clave llegan aparte (DB_USER y DB_PASSWORD).
# Si DATABASE_URL ya viene en formato JDBC, se deja igual.
set -e

case "$DATABASE_URL" in
  postgres://*|postgresql://*)
    resto="${DATABASE_URL#*://}"
    export DATABASE_URL="jdbc:postgresql://${resto#*@}"
    ;;
esac

# MaxRAMPercentage: la JVM se ajusta a la memoria del contenedor (512 MB en el plan gratis)
exec java -XX:MaxRAMPercentage=75 -jar /app/app.jar
