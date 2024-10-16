FROM influxdb:2.7.10-alpine

# Créer le répertoire de données
RUN mkdir -p /var/lib/influxdb

# Définir les permissions (facultatif, si tu as besoin)
RUN chown -R influxdb:influxdb /var/lib/influxdb

# Utiliser l'utilisateur InfluxDB
USER influxdb

# Création par défaut du user et password et bucket 


# Créer un utilisateur et un groupe non-root
# RUN addgroup -S influxdbgroup && adduser -S influxdbuser -G influxdbgroup

# Copie du fichier de configuration 
# COPY ./influxdb.conf /etc/influxdb/influxdb.conf

# Assurer que l'utilisateur non-root a les droits sur le fichier
# RUN chown influxdbuser:influxdbgroup /etc/influxdb/influxdb.conf

# Utiliser l'utilisateur non-root
# USER influxdbuser

# Démarrer le service InfluxDB avec la configuration
# Setup de la variable d'environnement
ENV INFLUXD_CONFIG_PATH=/etc/influxdb/influxdb.conf
# CMD ["influxd", "-config", "/etc/influxdb/influxdb.conf"]
CMD ["influxd"]