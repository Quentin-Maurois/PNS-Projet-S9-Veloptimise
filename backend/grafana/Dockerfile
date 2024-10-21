# Dockerfile.grafana
FROM grafana/grafana:11.2.2

# Copier le fichier de configuration dans le conteneur
# COPY ./grafana.ini /etc/grafana/grafana.ini

# S'assurer que le dossier de sauvegarde appartient à l'utilisateur grafana
# RUN chown -R grafana:users /var/lib/grafana

# Utiliser l'utilisateur non-root
USER grafana

# Démarrer Grafana
CMD ["/run.sh"]