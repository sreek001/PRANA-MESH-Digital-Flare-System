"""Migration script to add latitude and longitude columns to signals table."""

from sqlalchemy import create_engine, text

DATABASE_URL = "mysql+pymysql://root:2528@localhost:3306/prana_mesh"
engine = create_engine(DATABASE_URL)

def migrate():
    with engine.connect() as conn:
        # Check if columns exist
        result = conn.execute(text("DESCRIBE signals"))
        columns = [row[0] for row in result.fetchall()]

        print(f"Current columns: {columns}")

        # Add latitude column if it doesn't exist
        if "latitude" not in columns:
            print("Adding latitude column...")
            conn.execute(text(
                "ALTER TABLE signals ADD COLUMN latitude FLOAT NOT NULL DEFAULT 0.0"
            ))
            print("latitude column added.")
        else:
            print("latitude column already exists.")

        # Add longitude column if it doesn't exist
        if "longitude" not in columns:
            print("Adding longitude column...")
            conn.execute(text(
                "ALTER TABLE signals ADD COLUMN longitude FLOAT NOT NULL DEFAULT 0.0"
            ))
            print("longitude column added.")
        else:
            print("longitude column already exists.")

        conn.commit()
        print("Migration completed successfully!")

if __name__ == "__main__":
    migrate()
