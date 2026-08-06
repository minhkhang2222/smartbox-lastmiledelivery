FROM pgvector/pgvector:pg18

# Copy init script and backup.sql into docker-entrypoint-initdb.d
COPY init-db.sh /docker-entrypoint-initdb.d/00-init-db.sh
COPY backup.sql /docker-entrypoint-initdb.d/02-backup.sql

# Grant executable permission for shell script
RUN chmod +x /docker-entrypoint-initdb.d/00-init-db.sh
