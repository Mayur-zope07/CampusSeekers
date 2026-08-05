import logging
import pandas as pd

logger = logging.getLogger("data_pipeline.transformers.clean_data")

def clean_college_data(df: pd.DataFrame) -> pd.DataFrame:
    """
    Cleans raw college details (normalizes college types, formats names, 
    removes extra spaces, and fills missing non-critical columns).
    """
    logger.info("Running college data cleaning routine placeholder.")
    return df

def clean_cutoff_data(df: pd.DataFrame) -> pd.DataFrame:
    """
    Cleans raw cutoff tables (strips spaces, casts data types to numeric,
    standardizes exam names, and handles empty rows).
    """
    logger.info("Running cutoff data cleaning routine placeholder.")
    return df

def clean_seat_matrix_data(df: pd.DataFrame) -> pd.DataFrame:
    """
    Cleans raw seat matrix details (standardizes seat type labels, 
    verifies capacities, and parses integers).
    """
    logger.info("Running seat matrix data cleaning routine placeholder.")
    return df
