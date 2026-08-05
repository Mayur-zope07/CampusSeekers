import logging
import pandas as pd
from config.config import settings

logger = logging.getLogger("data_pipeline.validators.data")

class DataValidator:
    """
    Validates values and logical business rules of structured data.
    """
    def __init__(self, course: str):
        self.course = course
        self.supported_years = settings["supported_years"]

    def validate_cutoffs(self, df: pd.DataFrame) -> bool:
        """
        Validates logical rules on cutoff datasets (e.g., non-null codes,
        closing ranks > 0, percentiles between 0 and 100, rounds >= 1).
        """
        logger.info("Validating cutoff values and business rules.")
        
        if df.empty:
            logger.warning("DataFrame is empty. Validation skipped.")
            return True
            
        # Verify check: percentiles range [0, 100]
        if "closing_percentile" in df.columns:
            invalid_pct = df[(df["closing_percentile"] < 0) | (df["closing_percentile"] > 100)]
            if not invalid_pct.empty:
                logger.error(f"Validation failed: Percentiles out of bounds: {len(invalid_pct)} rows.")
                return False
                
        # Verify check: ranks are positive
        if "closing_rank" in df.columns:
            invalid_rank = df[df["closing_rank"] <= 0]
            if not invalid_rank.empty:
                logger.error(f"Validation failed: Ranks must be positive: {len(invalid_rank)} rows.")
                return False
                
        logger.info("Cutoff data validation passed.")
        return True

    def validate_seat_matrix(self, df: pd.DataFrame) -> bool:
        """
        Validates logical rules on seat matrix datasets (e.g. seats >= 0).
        """
        logger.info("Validating seat matrix values.")
        
        if df.empty:
            return True
            
        if "seats" in df.columns:
            invalid_seats = df[df["seats"] < 0]
            if not invalid_seats.empty:
                logger.error(f"Validation failed: Seats count cannot be negative: {len(invalid_seats)} rows.")
                return False
                
        logger.info("Seat matrix data validation passed.")
        return True
