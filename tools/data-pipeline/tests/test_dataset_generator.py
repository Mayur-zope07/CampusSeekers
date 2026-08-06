import sys
import json
from pathlib import Path
import pytest
import pandas as pd

# Add parent folder to system path for importing modules
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from transformers.dataset_generator import DatasetGenerator

def create_mock_data(tmp_path):
    """
    Sets up mock input data structure in a temporary directory.
    """
    cutoffs_dir = tmp_path / "cutoffs"
    seat_matrix_dir = tmp_path / "seat_matrix"
    
    cutoffs_dir.mkdir(parents=True, exist_ok=True)
    seat_matrix_dir.mkdir(parents=True, exist_ok=True)
    
    # 1. Mock Cutoffs Input CSV
    # Row 1: Valid
    # Row 2: Valid (duplicate branch, duplicate college)
    # Row 3: Invalid College Code (4 digits) - should be skipped
    # Row 4: Invalid Branch Code (8 digits) - should be skipped
    # Row 5: Invalid Closing Rank (0) - should be skipped
    # Row 6: Invalid Closing Percentile (105.0) - should be skipped
    # Row 7: Valid but with duplicate row content (tests duplicate removal)
    # Row 8: Name normalization test (excessive spaces and poorly placed commas)
    cutoff_data = [
        ["12345", "Govt. College of Engineering, Pune", "123451010", "Computer Engineering", 2025, 1, "OPEN", "GOPENS", 100, 99.5],
        ["12345", "Govt. College of Engineering, Pune", "123451020", "Mechanical Engineering", 2025, 1, "OBC", "GOBCS", 200, 98.0],
        ["1234", "Invalid College Code", "123451010", "Computer Engineering", 2025, 1, "OPEN", "GOPENS", 100, 99.5],
        ["12345", "Govt. College of Engineering, Pune", "12345101", "Invalid Branch Code", 2025, 1, "OPEN", "GOPENS", 100, 99.5],
        ["12345", "Govt. College of Engineering, Pune", "123451010", "Computer Engineering", 2025, 1, "OPEN", "GOPENS", 0, 99.5],
        ["12345", "Govt. College of Engineering, Pune", "123451010", "Computer Engineering", 2025, 1, "OPEN", "GOPENS", 100, 105.0],
        ["12345", "Govt. College of Engineering, Pune", "123451010", "Computer Engineering", 2025, 1, "OPEN", "GOPENS", 100, 99.5], # Duplicate
        ["54321", "  COEP Technological    University   , Pune  ", "543211010", "  Information   Technology ,  Pune ", 2025, 1, "OPEN", "GOPENS", 50, 99.9]
    ]
    cutoff_cols = [
        "College Code", "College Name", "Branch Code", "Branch Name", 
        "Year", "Round", "Category", "Seat Type", "Closing Rank", "Closing Percentile"
    ]
    cutoff_df = pd.DataFrame(cutoff_data, columns=cutoff_cols)
    cutoff_df.to_csv(cutoffs_dir / "cutoffs_2025_final.csv", index=False)
    
    # 2. Mock Seat Matrix Input CSV
    # Row 1: Valid (matches Pune)
    # Row 2: Valid (new branch for Pune)
    # Row 3: Invalid Intake Capacity (0) - should be skipped
    # Row 4: Valid (matches COEP)
    # Row 5: Valid (new college not in cutoffs - tests referential integrity / warnings check)
    seat_matrix_data = [
        ["12345", "Govt. College of Engineering, Pune", "123451010", "Computer Engineering", 60],
        ["12345", "Govt. College of Engineering, Pune", "123451020", "Mechanical Engineering", 60],
        ["12345", "Govt. College of Engineering, Pune", "123451030", "Civil Engineering", 0], # Invalid capacity
        ["54321", "  COEP Technological    University   , Pune  ", "543211010", "  Information   Technology ,  Pune ", 40],
        ["99999", "Ref Integrity Broken College", "999991010", "Ref Integrity Branch", 20] # Valid in inputs, but we will use this to test relationships
    ]
    seat_matrix_cols = ["College Code", "College Name", "Branch Code", "Branch Name", "Intake Capacity"]
    seat_matrix_df = pd.DataFrame(seat_matrix_data, columns=seat_matrix_cols)
    seat_matrix_df.to_csv(seat_matrix_dir / "seat_matrix_2025_final.csv", index=False)

def test_missing_input_raises_error(tmp_path):
    """
    Verifies that the generator stops execution and raises FileNotFoundError if inputs are missing.
    """
    generator = DatasetGenerator(year=2025, final_dir=tmp_path)
    with pytest.raises(FileNotFoundError):
        generator.generate()

def test_dataset_generation_success(tmp_path):
    """
    Verifies successful end-to-end dataset generation, file creation,
    names normalization, validation filtering, and metadata generation.
    """
    create_mock_data(tmp_path)
    generator = DatasetGenerator(year=2025, final_dir=tmp_path)
    summary = generator.generate()
    
    # Check outputs exist
    assert (tmp_path / "colleges" / "colleges.csv").exists()
    assert (tmp_path / "branches" / "branches.csv").exists()
    assert (tmp_path / "college_branches" / "college_branches.csv").exists()
    assert (tmp_path / "cutoffs" / "cutoffs.csv").exists()
    assert (tmp_path / "seat_matrix" / "seat_matrix.csv").exists()
    assert (tmp_path / "college_lookup.csv").exists()
    assert (tmp_path / "dataset_metadata.json").exists()
    
    # Read generated files
    col_df = pd.read_csv(tmp_path / "colleges" / "colleges.csv")
    br_df = pd.read_csv(tmp_path / "branches" / "branches.csv")
    cb_df = pd.read_csv(tmp_path / "college_branches" / "college_branches.csv")
    co_df = pd.read_csv(tmp_path / "cutoffs" / "cutoffs.csv")
    sm_df = pd.read_csv(tmp_path / "seat_matrix" / "seat_matrix.csv")
    cl_df = pd.read_csv(tmp_path / "college_lookup.csv")
    
    # 1. No duplicate colleges or branches
    assert not col_df.duplicated(subset=["college_code"]).any()
    assert not br_df.duplicated(subset=["branch_code"]).any()
    
    # 2. Text Normalization check
    # Original: "  COEP Technological    University   , Pune  "
    # Expected: "COEP Technological University, Pune"
    coep_row = col_df[col_df["college_code"] == 54321]
    assert len(coep_row) == 1
    assert coep_row.iloc[0]["name"] == "COEP Technological University, Pune"
    
    # Original: "  Information   Technology ,  Pune "
    # Expected: "Information Technology, Pune"
    it_row = br_df[br_df["branch_code"] == 543211010]
    assert len(it_row) == 1
    assert it_row.iloc[0]["name"] == "Information Technology, Pune"
    
    # 3. Validation checks: check that invalid rows were skipped
    # 4 rows from cutoff should be skipped (1234 code, 12345101 branch code, 0 closing rank, 105.0 percentile)
    # 1 row from seat matrix should be skipped (0 capacity)
    # 1 duplicate row removed from cutoffs
    with open(tmp_path / "dataset_metadata.json", "r") as f:
        meta = json.load(f)
        
    assert meta["rows_skipped"]["cutoffs"] == 4
    assert meta["rows_skipped"]["seat_matrix"] == 1
    assert meta["duplicate_rows_removed"]["cutoffs"] == 1
    
    # 4. College Lookup generation check (Sorted alphabetically)
    assert len(cl_df) == len(col_df)
    names = list(cl_df["name"])
    assert names == sorted(names)
    
    # 5. Referential Integrity check: Verify relationships
    college_codes = set(col_df["college_code"])
    branch_codes = set(br_df["branch_code"])
    cb_combinations = set(zip(cb_df["college_code"], cb_df["branch_code"]))
    
    # Every CollegeBranch references an existing College and Branch
    for idx, row in cb_df.iterrows():
        assert row["college_code"] in college_codes
        assert row["branch_code"] in branch_codes
        
    # Every Cutoff references an existing CollegeBranch
    for idx, row in co_df.iterrows():
        assert (row["college_code"], row["branch_code"]) in cb_combinations
        
    # Every Seat Matrix record references an existing CollegeBranch
    for idx, row in sm_df.iterrows():
        assert (row["college_code"], row["branch_code"]) in cb_combinations
