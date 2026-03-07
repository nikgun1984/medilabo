How to insert the sample patients into the project's PostgreSQL

This project runs PostgreSQL inside Docker Compose with the service name `postgres`.

By default the DB credentials (from docker-compose.yml) are:
- host: localhost (or inside compose network: postgres)
- port: 5432
- database: demographics_db
- user: medilabo
- password: medilabo123

Run the SQL script from your host against the containerized DB using one of these methods.

1) Using docker exec (recommended when the DB container is running):

```bash
# copy the SQL into the container and run it (works if container is running)
cat scripts/insert_patients.sql | docker exec -i medilabo-postgres psql -U medilabo -d demographics_db
```

2) Using psql from your host (if you have psql installed and port 5432 is forwarded):

```bash
psql "postgresql://medilabo:medilabo123@localhost:5432/demographics_db" -f scripts/insert_patients.sql
```

3) Connect with a GUI (pgAdmin, TablePlus) and run the SQL file.

Verification:

```sql
-- count rows
SELECT count(*) FROM patients;
-- show rows
SELECT id, last_name, first_name, date_of_birth, gender, address, phone FROM patients ORDER BY id;
```

Notes:
- The script uses CREATE TABLE IF NOT EXISTS so it is safe to run multiple times; duplicates will be inserted each run.
- If you'd rather avoid duplicates, wrap the INSERTs with logic (e.g., using ON CONFLICT) or clear the table first with TRUNCATE patients;
