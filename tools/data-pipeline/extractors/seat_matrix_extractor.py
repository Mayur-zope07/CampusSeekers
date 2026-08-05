import logging
import re
import sys
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Union, Dict, Any, Tuple
import pandas as pd

try:
    import pdfplumber
except ImportError:
    pdfplumber = None

try:
    import camelot
except ImportError:
    camelot = None

logger = logging.getLogger("data_pipeline.extractors.seat_matrix")

# Compile regular expressions for matching headers and codes
COLLEGE_PATTERN = re.compile(r"^(\d{5})\s*-\s*(.+)$")
BRANCH_CODE_PATTERN = re.compile(r"^(\d{9,10}[A-Z]*)$")

class SeatMatrixExtractor:
    """
    Extractor class designed to parse seat availability matrices from official admission PDFs.
    Supports extraction, cleaning, validation, and metadata generation.
    """
    def __init__(self, pdf_path: Union[str, Path]):
        self.pdf_path = Path(pdf_path)
        if not self.pdf_path.exists():
            raise FileNotFoundError(f"Target PDF file not found at: {self.pdf_path}")
        if pdfplumber is None:
            raise ImportError("pdfplumber is required but not installed.")
        self.summary = {}

    def extract(self, max_pages: int = None) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame, Dict[str, Any]]:
        """
        Parses pages in the PDF document to extract the distribution of seats.
        Performs extraction, cleaning, and validation.
        
        Returns:
            raw_df: Original extracted data without modifications.
            clean_df: Data after cleaning (whitespace trimmed, names normalized, intake cast to int).
            final_df: Data after applying validation checks (malformed rows skipped).
            metadata: Metadata dictionary detailing processing metrics.
        """
        start_time = time.time()
        logger.info(f"PDF Loaded: {self.pdf_path.name}")
        print(f"PDF Loaded: {self.pdf_path.name}")
        
        raw_records = []
        pages_processed = 0
        
        current_college_code = None
        current_college_name = None
        
        with pdfplumber.open(self.pdf_path) as pdf:
            pages = pdf.pages[:max_pages] if max_pages is not None else pdf.pages
            total_pages = len(pages)
            logger.info(f"Total pages to process: {total_pages}")
            
            for page_idx, page in enumerate(pages):
                page_num = page_idx + 1
                logger.info(f"Processing Page {page_num}/{total_pages}")
                print(f"Current Page: {page_num}/{total_pages}", end="\r")
                pages_processed += 1
                
                page_records = []
                tables = []
                
                # 1. Primary table extraction
                try:
                    tables = page.extract_tables()
                except Exception as e:
                    logger.warning(f"pdfplumber extract_tables failed on page {page_num}: {e}. Trying Camelot fallback.")
                    if camelot:
                        try:
                            # Camelot pages are 1-indexed
                            camelot_tables = camelot.read_pdf(str(self.pdf_path), pages=str(page_num))
                            tables = [ct.df.values.tolist() for ct in camelot_tables]
                        except Exception as ce:
                            logger.error(f"Camelot fallback failed on page {page_num}: {ce}")
                
                if tables:
                    for table in tables:
                        si_col_idx = 7
                        course_col_idx = 1
                        
                        local_college_code = current_college_code
                        local_college_name = current_college_name
                        
                        for row in table:
                            if not row or len(row) < 2:
                                continue
                            
                            col_0 = row[0]
                            if not col_0:
                                continue
                            
                            col_0_str = str(col_0).strip()
                            
                            # Check for College Header
                            m_col = COLLEGE_PATTERN.match(col_0_str)
                            if m_col:
                                local_college_code = m_col.group(1).strip()
                                local_college_name = m_col.group(2).strip()
                                continue
                                
                            # Check if row defines column positions
                            if 'Choice Code' in col_0_str:
                                try:
                                    course_col_idx = [i for i, cell in enumerate(row) if cell and 'Course Name' in str(cell)][0]
                                except IndexError:
                                    course_col_idx = 1
                                try:
                                    si_col_idx = [i for i, cell in enumerate(row) if cell and 'SI' in str(cell)][0]
                                except IndexError:
                                    si_col_idx = 7
                                continue
                                
                            # Check Choice Code (Branch Code)
                            m_br = BRANCH_CODE_PATTERN.match(col_0_str)
                            if m_br:
                                branch_code = m_br.group(1)
                                branch_name_val = row[course_col_idx] if course_col_idx < len(row) else None
                                si_val = row[si_col_idx] if si_col_idx < len(row) else None
                                
                                page_records.append({
                                    "College Code": local_college_code,
                                    "College Name": local_college_name,
                                    "Branch Code": branch_code,
                                    "Branch Name": branch_name_val,
                                    "Intake Capacity": si_val
                                })
                                
                        if local_college_code:
                            current_college_code = local_college_code
                            current_college_name = local_college_name
                            
                # 2. Text fallback extraction if table yielded nothing
                if not page_records:
                    logger.info(f"Using text-based fallback on page {page_num}")
                    try:
                        text = page.extract_text()
                        if text:
                            lines = text.split("\n")
                            local_college_code = current_college_code
                            local_college_name = current_college_name
                            
                            for idx, line in enumerate(lines):
                                line_str = line.strip()
                                m_col = COLLEGE_PATTERN.match(line_str)
                                if m_col:
                                    local_college_code = m_col.group(1).strip()
                                    local_college_name = m_col.group(2).strip()
                                    continue
                                    
                                m_br = re.match(r"^(\d{9,10}[A-Z]*)\s+(.+)$", line_str)
                                if m_br:
                                    branch_code = m_br.group(1)
                                    remainder = m_br.group(2).strip()
                                    tokens = remainder.split()
                                    if len(tokens) >= 6:
                                        course_name_part = " ".join(tokens[:-6])
                                        intake_capacity_val = tokens[-6]
                                        
                                        # Lookahead for course name continuation
                                        course_name = course_name_part
                                        j = idx + 1
                                        while j < len(lines):
                                            next_line = lines[j].strip()
                                            if not next_line:
                                                j += 1
                                                continue
                                            if next_line.startswith("Category") or next_line.startswith("General") or next_line.startswith("State Level") or COLLEGE_PATTERN.match(next_line):
                                                break
                                            course_name = f"{course_name} {next_line}"
                                            j += 1
                                            
                                        page_records.append({
                                            "College Code": local_college_code,
                                            "College Name": local_college_name,
                                            "Branch Code": branch_code,
                                            "Branch Name": course_name,
                                            "Intake Capacity": intake_capacity_val
                                        })
                                        
                            if local_college_code:
                                current_college_code = local_college_code
                                current_college_name = local_college_name
                    except Exception as te:
                        logger.error(f"Text fallback failed on page {page_num}: {te}")
                        
                raw_records.extend(page_records)
                
        # 1. Create RAW DataFrame
        raw_df = pd.DataFrame(raw_records, columns=["College Code", "College Name", "Branch Code", "Branch Name", "Intake Capacity"])
        rows_extracted = len(raw_df)
        
        # 2. Count raw duplicates (exactly identical rows before cleaning)
        duplicates_removed = len(raw_df) - len(raw_df.drop_duplicates())
        
        # 3. Clean Data
        clean_records = []
        seen = set()
        
        for idx, row in raw_df.iterrows():
            col_code = row["College Code"]
            col_name = row["College Name"]
            br_code = row["Branch Code"]
            br_name = row["Branch Name"]
            intake = row["Intake Capacity"]
            
            # Check if all elements are None or blank (blank row)
            if not any([col_code, col_name, br_code, br_name, intake]):
                continue
                
            # Remove repeated headers (if fields match header labels)
            if str(col_code).strip() == "College Code" or str(br_code).strip() == "Choice Code":
                continue
                
            # Trim whitespace and normalize spaces
            def clean_str(val):
                if pd.isna(val) or val is None:
                    return ""
                s = str(val).strip()
                s = re.sub(r"\s+", " ", s)
                return s
                
            col_code_clean = clean_str(col_code)
            col_name_clean = clean_str(col_name)
            br_code_clean = clean_str(br_code)
            br_name_clean = clean_str(br_name)
            
            # Remove page numbers
            page_pat = re.compile(r"\bPage\s+\d+\s+of\s+\d+\b|\bPage\s+\d+\b", re.IGNORECASE)
            col_name_clean = page_pat.sub("", col_name_clean).strip()
            br_name_clean = page_pat.sub("", br_name_clean).strip()
            
            # Convert Intake Capacity to Integer
            intake_clean = None
            if intake is not None and not pd.isna(intake):
                intake_str = str(intake).strip()
                if intake_str.isdigit():
                    intake_clean = int(intake_str)
                else:
                    try:
                        intake_clean = int(float(intake_str))
                    except ValueError:
                        intake_clean = None
            
            # Ensure unique rows in clean dataset
            record_tuple = (col_code_clean, col_name_clean, br_code_clean, br_name_clean, intake_clean)
            if record_tuple in seen:
                continue
            seen.add(record_tuple)
            
            # Avoid empty rows
            if not col_code_clean and not col_name_clean and not br_code_clean and not br_name_clean:
                continue
                
            clean_records.append({
                "College Code": col_code_clean,
                "College Name": col_name_clean,
                "Branch Code": br_code_clean,
                "Branch Name": br_name_clean,
                "Intake Capacity": intake_clean
            })
            
        clean_df = pd.DataFrame(clean_records, columns=["College Code", "College Name", "Branch Code", "Branch Name", "Intake Capacity"])
        
        # 4. Validate Data
        final_records = []
        rows_skipped = 0
        
        for idx, row in clean_df.iterrows():
            col_code = row["College Code"]
            col_name = row["College Name"]
            br_code = row["Branch Code"]
            br_name = row["Branch Name"]
            intake = row["Intake Capacity"]
            
            reasons = []
            
            # Validation Rule 1: College Code must contain exactly 5 digits
            if not col_code or not re.match(r"^\d{5}$", str(col_code)):
                reasons.append(f"College Code '{col_code}' is not exactly 5 digits")
                
            # Validation Rule 2: Branch Code must be 9-10 characters
            if not br_code or not (9 <= len(str(br_code)) <= 10):
                reasons.append(f"Branch Code '{br_code}' length {len(str(br_code)) if br_code else 0} is not between 9 and 10")
                
            # Validation Rule 3: College Name cannot be empty
            if not col_name or not str(col_name).strip():
                reasons.append("College Name is empty")
                
            # Validation Rule 4: Branch Name cannot be empty
            if not br_name or not str(br_name).strip():
                reasons.append("Branch Name is empty")
                
            # Validation Rule 5: Intake Capacity must be greater than zero
            if intake is None or pd.isna(intake):
                reasons.append("Intake Capacity is missing or non-numeric")
            else:
                try:
                    intake_val = int(intake)
                    if intake_val <= 0:
                        reasons.append(f"Intake Capacity {intake_val} must be greater than zero")
                except ValueError:
                    reasons.append(f"Intake Capacity '{intake}' is not a valid integer")
                    
            if reasons:
                rows_skipped += 1
                logger.warning(f"Row skipped due to: {', '.join(reasons)}. Value: {row.to_dict()}")
            else:
                final_records.append({
                    "College Code": str(col_code).strip(),
                    "College Name": str(col_name).strip(),
                    "Branch Code": str(br_code).strip(),
                    "Branch Name": str(br_name).strip(),
                    "Intake Capacity": int(intake)
                })
                
        final_df = pd.DataFrame(final_records, columns=["College Code", "College Name", "Branch Code", "Branch Name", "Intake Capacity"])
        
        elapsed_time = time.time() - start_time
        
        # Build Metadata dictionary
        metadata = {
            "source": self.pdf_path.name,
            "year": int(datetime.now().year),  # Fallback, main CLI sets this accurately
            "pagesProcessed": int(pages_processed),
            "rowsExtracted": int(rows_extracted),
            "rowsSkipped": int(rows_skipped),
            "duplicatesRemoved": int(duplicates_removed),
            "finalRows": int(len(final_df)),
            "executionTimeSeconds": round(elapsed_time, 2),
            "generatedAt": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")
        }
        
        self.summary = metadata
        
        # Display logs
        logger.info(f"Pages Processed: {pages_processed}")
        logger.info(f"Rows Extracted: {rows_extracted}")
        logger.info(f"Duplicate Rows Removed: {duplicates_removed}")
        logger.info(f"Rows Skipped: {rows_skipped}")
        logger.info(f"Final Row Count: {len(final_df)}")
        logger.info(f"Execution Time: {elapsed_time:.2f} seconds")
        
        print("\n" + "="*50)
        print("EXTRACTION SUMMARY")
        print("="*50)
        print(f"Pages Processed:         {pages_processed}")
        print(f"Rows Extracted:          {rows_extracted}")
        print(f"Duplicate Rows Removed:  {duplicates_removed}")
        print(f"Rows Skipped (Failed):   {rows_skipped}")
        print(f"Final Row Count:         {len(final_df)}")
        print(f"Execution Time:          {elapsed_time:.2f} seconds")
        print("="*50)
        
        return raw_df, clean_df, final_df, metadata
