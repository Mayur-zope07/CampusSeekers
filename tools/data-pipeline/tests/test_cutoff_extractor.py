import sys
from pathlib import Path
import pytest
import pandas as pd

# Add parent folder to system path for importing modules
sys.path.append(str(Path(__file__).resolve().parent.parent))

from extractors.cutoff_extractor import CutoffExtractor

# Determine paths relative to this file
TEST_DIR = Path(__file__).resolve().parent
BASE_DIR = TEST_DIR.parent
REPO_ROOT = BASE_DIR.parent.parent
PDF_PATH = REPO_ROOT / "data" / "raw" / "engineering" / "2025" / "cutoffs" / "2025ENGG_CAP4_CutOff.pdf"

def test_pdf_exists():
    """
    Verifies that the target CAP round cutoff PDF file exists at the expected path.
    """
    assert PDF_PATH.exists(), f"Target PDF file not found at: {PDF_PATH}"

def test_extractor_dataframe_creation():
    """
    Verifies that the CutoffExtractor runs and creates a structured DataFrame.
    """
    assert PDF_PATH.exists()
    extractor = CutoffExtractor(PDF_PATH)
    
    # Extract only the first 2 pages for testing speed
    df = extractor.extract(max_pages=2)
        
    assert isinstance(df, pd.DataFrame)
    
    # Verify required columns exist
    required_columns = [
        "College Code", "College Name", "Branch Code", "Branch Name", 
        "Year", "Round", "Category", "Seat Type", "Closing Rank", "Closing Percentile"
    ]
    for col in required_columns:
        assert col in df.columns, f"Missing required column: {col}"
        
    # Check that it extracted some rows
    assert len(df) > 0, "No rows were extracted from the first two pages"

def test_csv_generation(tmp_path):
    """
    Verifies that the extracted data can be written to a CSV file successfully.
    """
    extractor = CutoffExtractor(PDF_PATH)
    
    # Extract only first page for speed
    df = extractor.extract(max_pages=1)
        
    temp_csv = tmp_path / "test_cutoff_output.csv"
    df.to_csv(temp_csv, index=False, encoding="utf-8")
    
    assert temp_csv.exists()
    # Read it back and check shape
    df_read = pd.read_csv(temp_csv)
    assert len(df_read) == len(df)
    assert list(df_read.columns) == list(df.columns)
