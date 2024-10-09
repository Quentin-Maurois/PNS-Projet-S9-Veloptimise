# Dockerfile.nodered
FROM nodered/node-red-docker:v10

# Copier le fichier de configuration dans le conteneur
# COPY ./data/settings.js /data/settings.js

# Utiliser l'utilisateur node-red créé par node red
USER node-red

# Démarrer Node-RED
CMD ["npm", "start", "--", "--userDir", "/data"]
