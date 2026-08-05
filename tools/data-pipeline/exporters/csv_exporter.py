import logging
from pathlib import Path
import pandas as pd
from config.config import settings

logger = logging.getLogger("data_pipeline.exporters.csv")

class CSVExporter:
    """
    Handles export operations for parsed and validated datasets.
    """
    def __init__(self, target_subfolder: str):
        self.final_dir = Path(settings["final_directory"]) / target_subfolder
        
        # Verify target final directory exists
        if not self.final_dir.exists():
            logger.info(f"Target directory {self.final_dir} does not exist. Creating it.")
            self.final_dir.mkdir(parents=True, exist_ok=True)

    def export(self, df: pd.DataFrame, filename: str) -> Path:
        """
        Saves a clean DataFrame to a CSV file in the designated data/final subfolder.
        """
        output_filepath = self.final_dir / filename
        logger.info(f"Exporting clean dataset to: {output_filepath}")
        
        # Write to CSV
        df.to_csv(output_filepath, index=False, encoding="utf-8")
        
        logger.info(f"Export operation complete: {filename}")
        return output_filepath
