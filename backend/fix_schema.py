"""Fix the schema by removing duplicate columns."""

from sqlalchemy import create_engine, text

DATABASE_URL = "mysql+pymysql://root:2528@localhost:3306/prana_mesh"
engine = create_engine(DATABASE_URL)

def fix_schema():
    with engine.connect() as conn:
        # Check current columns
        result = conn.execute(text("DESCRIBE signals"))
        columns = [row[0] for row in result.fetchall()]
        print(f"Current columns: {columns}")

        # Remove the duplicate latitude/longitude columns we just added
        if "latitude" in columns:
            print("Dropping duplicate latitude column...")
            conn.execute(text("ALTER TABLE signals DROP COLUMN latitude"))

        if "longitude" in columns:
            print("Dropping duplicate longitude column...")
            conn.execute(text("ALTER TABLE signals DROP COLUMN longitude"))

        # Also clean up unused columns if they exist
        if "location" in columns:
            print("Dropping unused location column...")
            conn.execute(text("ALTER TABLE signals DROP COLUMN location"))

        if "id" in columns:
            print("Dropping unused id column...")
            conn.execute(text("ALTER TABLE signals DROP COLUMN id"))

        conn.commit()

        # Verify final schema
        result = conn.execute(text("DESCRIBE signals"))
        columns = [row[0] for row in result.fetchall()]
        print(f"Final columns: {columns}")
        print("Schema fixed successfully!")

if __name__ == "__main__":
    fix_schema()
