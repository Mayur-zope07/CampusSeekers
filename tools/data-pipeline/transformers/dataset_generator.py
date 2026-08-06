import logging
import re
import sys
import time
import json
from pathlib import Path
from datetime import datetime, timezone
import pandas as pd
import numpy as np

from utils import setup_logging, ensure_directory
from config.config import settings

logger = logging.getLogger("data_pipeline.transformers.dataset_generator")

def normalize_name(name):
    if pd.isna(name) or name is None:
        return ""
    # Trim and collapse multiple spaces
    s = str(name).strip()
    s = re.sub(r"\s+", " ", s)
    # Normalize comma spacing: remove spaces around commas, then place one comma followed by a space
    s = re.sub(r"\s*,\s*", ", ", s)
    return s

class DatasetGenerator:
    def __init__(self, year: int, final_dir: Path = None):
        self.year = year
        self.final_dir = Path(final_dir) if final_dir is not None else Path(settings["final_directory"])
        self.cutoff_input_path = self.final_dir / "cutoffs" / f"cutoffs_{self.year}_final.csv"
        self.seat_matrix_input_path = self.final_dir / "seat_matrix" / f"seat_matrix_{self.year}_final.csv"

    def generate(self):
        start_time = time.time()
        
        # 1. Stop if either validated input is missing
        if not self.cutoff_input_path.exists():
            msg = f"Required validated input file not found: {self.cutoff_input_path}"
            logger.error(msg)
            raise FileNotFoundError(msg)
            
        if not self.seat_matrix_input_path.exists():
            msg = f"Required validated input file not found: {self.seat_matrix_input_path}"
            logger.error(msg)
            raise FileNotFoundError(msg)
            
        logger.info(f"Reading validated inputs for year {self.year}...")
        
        # Read datasets
        try:
            cutoff_df = pd.read_csv(self.cutoff_input_path, dtype={"College Code": str, "Branch Code": str})
        except Exception as e:
            logger.error(f"Failed to read cutoff file: {e}")
            raise
            
        try:
            seat_matrix_df = pd.read_csv(self.seat_matrix_input_path, dtype={"College Code": str, "Branch Code": str})
        except Exception as e:
            logger.error(f"Failed to read seat matrix file: {e}")
            raise
            
        # Metrics trackers
        rows_processed = {
            "cutoffs": len(cutoff_df),
            "seat_matrix": len(seat_matrix_df)
        }
        rows_skipped = {
            "cutoffs": 0,
            "seat_matrix": 0
        }
        duplicate_rows_removed = {}
        validation_errors = []
        
        # 2. Validation & Filtering of Input Records
        # Validation checks:
        # College Code: exactly 5 digits
        # Branch Code: length between 9 and 10
        # Closing Rank: > 0
        # Closing Percentile: between 0 and 100
        # Intake Capacity: > 0
        
        valid_cutoffs = []
        for idx, row in cutoff_df.iterrows():
            col_code = str(row.get("College Code", "")).strip()
            col_name = normalize_name(row.get("College Name", ""))
            br_code = str(row.get("Branch Code", "")).strip()
            br_name = normalize_name(row.get("Branch Name", ""))
            exam_name = "MHT_CET"
            year = row.get("Year")
            round_num = row.get("Round")
            category = str(row.get("Category", "")).strip()
            seat_type = str(row.get("Seat Type", "")).strip()
            closing_rank = row.get("Closing Rank")
            closing_percentile = row.get("Closing Percentile")
            
            is_valid = True
            if not re.match(r"^\d{5}$", col_code):
                is_valid = False
            if not (9 <= len(br_code) <= 10):
                is_valid = False
            try:
                if int(closing_rank) <= 0:
                    is_valid = False
            except (ValueError, TypeError):
                is_valid = False
            try:
                if not (0.0 <= float(closing_percentile) <= 100.0):
                    is_valid = False
            except (ValueError, TypeError):
                is_valid = False
                
            if is_valid:
                valid_cutoffs.append({
                    "college_code": col_code,
                    "college_name": col_name,
                    "branch_code": br_code,
                    "branch_name": br_name,
                    "exam_name": exam_name,
                    "year": int(year) if pd.notna(year) else self.year,
                    "round": int(round_num) if pd.notna(round_num) else 1,
                    "category": category,
                    "raw_seat_type": seat_type,
                    "closing_rank": int(closing_rank),
                    "closing_percentile": float(closing_percentile)
                })
            else:
                rows_skipped["cutoffs"] += 1
                
        valid_seat_matrix = []
        for idx, row in seat_matrix_df.iterrows():
            col_code = str(row.get("College Code", "")).strip()
            col_name = normalize_name(row.get("College Name", ""))
            br_code = str(row.get("Branch Code", "")).strip()
            br_name = normalize_name(row.get("Branch Name", ""))
            intake = row.get("Intake Capacity")
            
            is_valid = True
            if not re.match(r"^\d{5}$", col_code):
                is_valid = False
            if not (9 <= len(br_code) <= 10):
                is_valid = False
            try:
                if int(intake) <= 0:
                    is_valid = False
            except (ValueError, TypeError):
                is_valid = False
                
            if is_valid:
                valid_seat_matrix.append({
                    "college_code": col_code,
                    "college_name": col_name,
                    "branch_code": br_code,
                    "branch_name": br_name,
                    "intake_capacity": int(intake)
                })
            else:
                rows_skipped["seat_matrix"] += 1
                
        # 3. Create datasets and apply deduplication
        
        # Colleges dataset
        # One row per unique college.
        col_list = []
        for c in valid_cutoffs:
            col_list.append((c["college_code"], c["college_name"]))
        for s in valid_seat_matrix:
            col_list.append((s["college_code"], s["college_name"]))
            
        col_df_raw = pd.DataFrame(col_list, columns=["college_code", "name"])
        col_df_dedup = col_df_raw.drop_duplicates(subset=["college_code"], keep="first")
        duplicate_rows_removed["colleges"] = len(col_df_raw) - len(col_df_dedup)
        
        col_final_records = []
        for idx, row in col_df_dedup.iterrows():
            col_final_records.append({
                "college_code": row["college_code"],
                "name": row["name"],
                "college_type": "", # NULL
                "establishment_year": "", # NULL
                "city": "", # NULL
                "state": "", # NULL
                "website": "", # NULL
                "naac_grade": "", # NULL
                "nba_accredited": "", # NULL
                "campus_size": "", # NULL
                "logo_url": "", # NULL
                "status": "" # NULL
            })
        col_final_df = pd.DataFrame(col_final_records)
        
        # Branches dataset
        br_list = []
        for c in valid_cutoffs:
            br_list.append((c["branch_code"], c["branch_name"]))
        for s in valid_seat_matrix:
            br_list.append((s["branch_code"], s["branch_name"]))
            
        br_df_raw = pd.DataFrame(br_list, columns=["branch_code", "name"])
        br_df_dedup = br_df_raw.drop_duplicates(subset=["branch_code"], keep="first")
        duplicate_rows_removed["branches"] = len(br_df_raw) - len(br_df_dedup)
        br_final_df = br_df_dedup.copy()
        
        # College Branches dataset (Merge Seat Matrix + Colleges + Branches)
        cb_keys = set()
        cb_intake = {} # maps (college_code, branch_code) to intake_capacity
        
        for s in valid_seat_matrix:
            key = (s["college_code"], s["branch_code"])
            cb_keys.add(key)
            cb_intake[key] = s["intake_capacity"]
            
        for c in valid_cutoffs:
            key = (c["college_code"], c["branch_code"])
            cb_keys.add(key)
            
        cb_records = []
        for key in cb_keys:
            cc, bc = key
            intake = cb_intake.get(key, "") # empty/NULL if not in seat matrix
            cb_records.append({
                "college_code": cc,
                "branch_code": bc,
                "intake_capacity": intake,
                "fees_per_year": "", # NULL
                "duration_years": "" # NULL
            })
            
        cb_final_df = pd.DataFrame(cb_records)
        duplicate_rows_removed["college_branches"] = 0 # Built from unique set
        
        # Cutoffs dataset
        co_records = []
        for c in valid_cutoffs:
            co_records.append({
                "college_code": c["college_code"],
                "branch_code": c["branch_code"],
                "exam_name": c["exam_name"],
                "year": c["year"],
                "round": c["round"],
                "category": c["category"],
                "raw_seat_type": c["raw_seat_type"],
                "closing_rank": c["closing_rank"],
                "closing_percentile": c["closing_percentile"]
            })
        co_df_raw = pd.DataFrame(co_records)
        co_final_df = co_df_raw.drop_duplicates()
        duplicate_rows_removed["cutoffs"] = len(co_df_raw) - len(co_final_df)
        
        # Seat Matrix dataset
        sm_records = []
        for s in valid_seat_matrix:
            sm_records.append({
                "college_code": s["college_code"],
                "branch_code": s["branch_code"],
                "intake_capacity": s["intake_capacity"]
            })
        sm_df_raw = pd.DataFrame(sm_records)
        sm_final_df = sm_df_raw.drop_duplicates()
        duplicate_rows_removed["seat_matrix"] = len(sm_df_raw) - len(sm_final_df)
        
        # College Lookup dataset (Sorted alphabetically by name)
        cl_df = col_final_df[["college_code", "name"]].copy()
        cl_df = cl_df.sort_values(by="name").reset_index(drop=True)
        
        # 4. Referential Integrity Checks
        college_codes_set = set(col_final_df["college_code"])
        branch_codes_set = set(br_final_df["branch_code"])
        college_branches_set = set(zip(cb_final_df["college_code"], cb_final_df["branch_code"]))
        
        # Check every college branch references an existing college
        for idx, row in cb_final_df.iterrows():
            cc = row["college_code"]
            if cc not in college_codes_set:
                validation_errors.append(f"Referential integrity broken: CollegeBranch references college_code '{cc}' which does not exist in colleges.")
                
        # Check every college branch references an existing branch
        for idx, row in cb_final_df.iterrows():
            bc = row["branch_code"]
            if bc not in branch_codes_set:
                validation_errors.append(f"Referential integrity broken: CollegeBranch references branch_code '{bc}' which does not exist in branches.")
                
        # Check every cutoff references an existing college branch
        for idx, row in co_final_df.iterrows():
            cc = row["college_code"]
            bc = row["branch_code"]
            if (cc, bc) not in college_branches_set:
                validation_errors.append(f"Referential integrity broken: Cutoff references college_code '{cc}' and branch_code '{bc}' which does not exist in college_branches.")
                
        # Check every seat matrix record references an existing college branch
        for idx, row in sm_final_df.iterrows():
            cc = row["college_code"]
            bc = row["branch_code"]
            if (cc, bc) not in college_branches_set:
                validation_errors.append(f"Referential integrity broken: Seat matrix record references college_code '{cc}' and branch_code '{bc}' which does not exist in college_branches.")
                
        # 5. Export Datasets
        col_dir = self.final_dir / "colleges"
        br_dir = self.final_dir / "branches"
        cb_dir = self.final_dir / "college_branches"
        co_dir = self.final_dir / "cutoffs"
        sm_dir = self.final_dir / "seat_matrix"
        
        ensure_directory(col_dir)
        ensure_directory(br_dir)
        ensure_directory(cb_dir)
        ensure_directory(co_dir)
        ensure_directory(sm_dir)
        
        col_final_df.to_csv(col_dir / "colleges.csv", index=False, encoding="utf-8")
        br_final_df.to_csv(br_dir / "branches.csv", index=False, encoding="utf-8")
        cb_final_df.to_csv(cb_dir / "college_branches.csv", index=False, encoding="utf-8")
        co_final_df.to_csv(co_dir / "cutoffs.csv", index=False, encoding="utf-8")
        sm_final_df.to_csv(sm_dir / "seat_matrix.csv", index=False, encoding="utf-8")
        cl_df.to_csv(self.final_dir / "college_lookup.csv", index=False, encoding="utf-8")
        
        elapsed_time = time.time() - start_time
        
        # 6. Generate Metadata JSON
        metadata = {
            "generation_timestamp": datetime.now(timezone.utc).isoformat(),
            "input_files": [
                str(self.cutoff_input_path.relative_to(settings["final_directory"].parent.parent)) if self.cutoff_input_path.is_relative_to(settings["final_directory"].parent.parent) else str(self.cutoff_input_path.name),
                str(self.seat_matrix_input_path.relative_to(settings["final_directory"].parent.parent)) if self.seat_matrix_input_path.is_relative_to(settings["final_directory"].parent.parent) else str(self.seat_matrix_input_path.name)
            ],
            "rows_processed": rows_processed,
            "rows_skipped": rows_skipped,
            "duplicate_rows_removed": duplicate_rows_removed,
            "execution_time_seconds": elapsed_time,
            "final_counts": {
                "colleges": len(col_final_df),
                "branches": len(br_final_df),
                "college_branches": len(cb_final_df),
                "cutoffs": len(co_final_df),
                "seat_matrix": len(sm_final_df),
                "college_lookup": len(cl_df)
            },
            "validation_errors": validation_errors
        }
        
        metadata_path = self.final_dir / "dataset_metadata.json"
        with open(metadata_path, "w", encoding="utf-8") as f:
            json.dump(metadata, f, indent=2)
            
        logger.info(f"Metadata JSON generated at {metadata_path}")
        
        # Return results for printing
        summary = {
            "total_colleges": len(col_final_df),
            "total_branches": len(br_final_df),
            "total_college_branches": len(cb_final_df),
            "total_cutoffs": len(co_final_df),
            "total_seat_matrix_records": len(sm_final_df),
            "total_college_lookup": len(cl_df),
            "rows_skipped": sum(rows_skipped.values()),
            "duplicate_rows_removed": sum(duplicate_rows_removed.values()),
            "validation_errors_count": len(validation_errors),
            "execution_time": elapsed_time
        }
        return summary
