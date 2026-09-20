#!/bin/bash
# ============================================================
# postgres-entrypoint.sh
# Initialises the PostgreSQL cluster on first run, then starts
# the server and applies the schema / seed data.
# ============================================================
set -euo pipefail

PGDATA="${PGDATA:-/var/lib/pgsql/data}"
DB_USER="${POSTGRESQL_USER:-ecomuser}"
DB_PASS="${POSTGRESQL_PASSWORD:?POSTGRESQL_PASSWORD must be set}"
DB_NAME="${POSTGRESQL_DATABASE:-ecomdb}"
INIT_FILE="/docker-entrypoint-initdb.d/schema.sql"

# ── 1. Initialise data directory if empty ─────────────────────
if [ ! -f "${PGDATA}/PG_VERSION" ]; then
    echo "[entrypoint] Initialising PostgreSQL cluster in ${PGDATA}..."
    initdb --pgdata="${PGDATA}" \
           --username=postgres \
           --auth-host=md5 \
           --auth-local=trust \
           --encoding=UTF8 \
           --locale=C.UTF-8

    # Allow connections from the app container (same pod/network)
    echo "host all all 0.0.0.0/0 md5"  >> "${PGDATA}/pg_hba.conf"
    echo "listen_addresses = '*'"       >> "${PGDATA}/postgresql.conf"

    # Start a temporary server for setup
    pg_ctl -D "${PGDATA}" -o "-c listen_addresses=''" -w start

    # Create the application role and database
    psql -v ON_ERROR_STOP=1 --username=postgres <<-EOSQL
        CREATE USER "${DB_USER}" WITH ENCRYPTED PASSWORD '${DB_PASS}';
        CREATE DATABASE "${DB_NAME}" OWNER "${DB_USER}";
        GRANT ALL PRIVILEGES ON DATABASE "${DB_NAME}" TO "${DB_USER}";
        \c "${DB_NAME}"
        GRANT ALL ON SCHEMA public TO "${DB_USER}";
EOSQL

    # Apply schema / seed data if provided
    if [ -f "${INIT_FILE}" ]; then
        echo "[entrypoint] Applying schema from ${INIT_FILE}..."
        psql -v ON_ERROR_STOP=1 --username="${DB_USER}" --dbname="${DB_NAME}" \
             -f "${INIT_FILE}"
    fi

    pg_ctl -D "${PGDATA}" -m fast -w stop
    echo "[entrypoint] Initialisation complete."
fi

# ── 2. Start the server (foreground) ──────────────────────────
echo "[entrypoint] Starting PostgreSQL..."
exec postgres -D "${PGDATA}"
