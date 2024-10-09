FROM influxdb:2.7.10-alpine

# Créer un utilisateur et un groupe non-root
# RUN addgroup -S influxdbgroup && adduser -S influxdbuser -G influxdbgroup

# Copie du fichier de configuration 
COPY ./influxdb.conf /etc/influxdb/influxdb.conf

# Assurer que l'utilisateur non-root a les droits sur le fichier
# RUN chown influxdbuser:influxdbgroup /etc/influxdb/influxdb.conf

# Utiliser l'utilisateur non-root
# USER influxdbuser

# Démarrer le service InfluxDB avec la configuration
CMD ["influxd", "-config", "/etc/influxdb/influxdb.conf"]