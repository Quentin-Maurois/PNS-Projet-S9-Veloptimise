# Dockerfile.grafana
FROM grafana/grafana:11.2.2

# Créer un utilisateur et un groupe non-root
# RUN addgroup -S grafanagroup && adduser -S grafanauser -G grafanagroup

# Copier le fichier de configuration dans le conteneur
COPY ./grafana.ini /etc/grafana/grafana.ini

# Assurer que l'utilisateur non-root a les droits sur le fichier
# RUN chown grafanauser:grafanagroup /etc/grafana/grafana.ini

# Utiliser l'utilisateur non-root
# USER grafanauser

# Démarrer Grafana
CMD ["/run.sh"]