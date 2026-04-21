from sqlalchemy import Column, String, Float, Integer, BigInteger, Text
from database import Base
from datetime import datetime


class Signal(Base):
    __tablename__ = "signals"

    device_id = Column(String(64), primary_key=True, index=True)
    latitude = Column("lat", Float, nullable=False)
    longitude = Column("lon", Float, nullable=False)
    status = Column(Integer, default=0)
    battery = Column(Integer, default=100)
    last_seen = Column("timestamp", BigInteger, nullable=False)
    sentinel_id = Column(String(64), nullable=True)
    ai_priority = Column(Integer, nullable=True)
    ai_analysis = Column(Text, nullable=True)

    @property
    def is_active(self):
        import time
        return (int(time.time()) - self.last_seen) < 600

    def get_status_label(self):
        labels = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}
        return labels.get(self.status, "UNKNOWN")  # type: ignore
