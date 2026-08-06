#!/bin/bash
set -e

echo "Enabling pgvector extension..."
psql -U postgres -d smartlocker -c "CREATE EXTENSION IF NOT EXISTS vector;"

echo "Restoring backup..."
pg_restore -U postgres -d smartlocker /docker-entrypoint-initdb.d/02-backup.sql || true

echo "Database initialization complete!"
