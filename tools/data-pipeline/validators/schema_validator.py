import logging
from pathlib import Path
import pandas as pd
from config.config import settings

logger = logging.getLogger("data_pipeline.validators.schema")

class SchemaValidator:
    """
    Validates structured DataFrames against the official CSV templates 
    located in the templates directory.
    """
    def __init__(self, template_name: str):
        self.template_dir = Path(settings["template_directory"])
        self.template_path = self.template_dir / template_name
        
        if not self.template_path.exists():
            raise FileNotFoundError(f"Template schema CSV file not found at: {self.template_path}")
            
        self.expected_columns = self._load_expected_columns()

    def _load_expected_columns(self) -> list:
        """Loads headers from the template file."""
        df_temp = pd.read_csv(self.template_path, nrows=0)
        return list(df_temp.columns)

    def validate(self, df: pd.DataFrame) -> bool:
        """
        Validates that the provided DataFrame matches the expected template schema.
        Returns True if schema is correct, False otherwise.
        """
        logger.info(f"Validating DataFrame schema against template: {self.template_path.name}")
        
        df_columns = list(df.columns)
        if df_columns != self.expected_columns:
            logger.error(
                f"Schema Mismatch! Expected: {self.expected_columns}, Received: {df_columns}"
            )
            return False
            
        logger.info("DataFrame schema validation passed successfully.")
        return True
