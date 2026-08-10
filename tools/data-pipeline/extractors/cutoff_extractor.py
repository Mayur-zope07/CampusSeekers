import logging
import re
import sys
import time
from pathlib import Path
from typing import Union
import pandas as pd

try:
    import pdfplumber
except ImportError:
    pdfplumber = None

try:
    import camelot
except ImportError:
    camelot = None

logger = logging.getLogger("data_pipeline.extractors.cutoff")

# Compile regular expressions for matching headers and cutoff values
COLLEGE_PATTERN = re.compile(r"^(\d{5})\s*-\s*(.+)$")
BRANCH_PATTERN = re.compile(r"^(\d{9,10}[a-zA-Z]?)\s*-\s*(.+)$")
CUTOFF_PATTERN = re.compile(r"^(\d+)\s*\(\s*([\d\.]+)\s*\)$")

def parse_category(seat_type: str) -> str:
    """
    Derives the database Category enum string from a seat type header.
    """
    s = seat_type.upper().strip().replace("\n", "").replace(" ", "")
    
    # Check special categories
    if "TFWS" in s:
        return "TFWS"
    if "EWS" in s:
        return "EWS"
    if "PWD" in s:
        return "PWD"
    if "DEF" in s:
        return "DEFENCE"
    if "ORPHAN" in s:
        return "ORPHAN"
    
    # Strip leading G/L
    if (s.startswith("G") or s.startswith("L")) and len(s) > 1:
        core = s[1:]
    else:
        core = s
        
    # Strip trailing S/H/O
    if core.endswith("S") or core.endswith("H") or core.endswith("O"):
        core = core[:-1]
        
    # Standardize remaining string
    if core == "OPEN":
        return "OPEN"
    if core == "OBC":
        return "OBC"
    if core == "SC":
        return "SC"
    if core == "ST":
        return "ST"
    if core in ("NT1", "NT-1", "NT_1"):
        return "NT1"
    if core in ("NT2", "NT-2", "NT_2"):
        return "NT2"
    if core in ("NT3", "NT-3", "NT_3"):
        return "NT3"
    if core == "SBC":
        return "SBC"
    if core == "SEBC":
        return "SEBC"
    if core in ("VJ", "DT"):
        return "OPEN"  # Fallback as VJ/DT is not in Category enum
    if core == "RSEBC":
        return "SEBC"
        
    # Generic fallbacks
    if "OPEN" in s: return "OPEN"
    if "OBC" in s: return "OBC"
    if "SC" in s: return "SC"
    if "ST" in s: return "ST"
    if "EWS" in s: return "EWS"
    if "SEBC" in s: return "SEBC"
    if "SBC" in s: return "SBC"
    if "NT" in s:
        if "1" in s: return "NT1"
        if "2" in s: return "NT2"
        if "3" in s: return "NT3"
        return "NT1"
        
    return "OPEN"

class CutoffExtractor:
    """
    Parser for Maharashtra CAP Engineering Cutoff PDF documents.
    Extracts ranks and percentiles matching seat types and maps them to colleges and branches.
    """
    def __init__(self, pdf_path: Union[str, Path]):
        self.pdf_path = Path(pdf_path)
        if not self.pdf_path.exists():
            raise FileNotFoundError(f"Target PDF file not found at: {self.pdf_path}")
        if pdfplumber is None:
            raise ImportError("pdfplumber is required but not installed.")
            
    def extract(self, max_pages: int = None) -> pd.DataFrame:
        """
        Parses pages of the cutoff PDF and extracts cutoff records.
        """
        start_time = time.time()
        logger.info(f"PDF Loaded: {self.pdf_path.name}")
        print(f"PDF Loaded: {self.pdf_path.name}")
        
        records = []
        pages_processed = 0
        invalid_rows_skipped = 0
        
        # State machine across pages
        current_college_code = None
        current_college_name = None
        current_branch_code = None
        current_branch_name = None
        
        with pdfplumber.open(self.pdf_path) as pdf:
            pages = pdf.pages[:max_pages] if max_pages is not None else pdf.pages
            total_pages = len(pages)
            logger.info(f"Total pages to process: {total_pages}")
            
            for page_idx, page in enumerate(pages):
                page_num = page_idx + 1
                logger.info(f"Processing Page {page_num}/{total_pages}")
                print(f"Current Page: {page_num}/{total_pages}", end="\r")
                pages_processed += 1
                
                # 1. Extract text and group words into lines with layout tops
                words = page.extract_words()
                lines = {}
                for w in words:
                    top = round(w['top'], 1)
                    found = False
                    for line_top in lines:
                        if abs(line_top - w['top']) < 3:
                            lines[line_top].append(w)
                            found = True
                            break
                    if not found:
                        lines[w['top']] = [w]
                
                # Identify headers with their positions on the page
                page_headers = []
                for top in sorted(lines.keys()):
                    line_words = sorted(lines[top], key=lambda x: x['x0'])
                    line_text = " ".join([w['text'] for w in line_words]).strip()
                    
                    # Match College Header
                    m_col = COLLEGE_PATTERN.match(line_text)
                    if m_col:
                        code, name = m_col.groups()
                        page_headers.append({
                            'type': 'college',
                            'code': code.strip(),
                            'name': name.strip(),
                            'top': top
                        })
                        
                    # Match Branch Header
                    m_br = BRANCH_PATTERN.match(line_text)
                    if m_br:
                        code, name = m_br.groups()
                        page_headers.append({
                            'type': 'branch',
                            'code': code.strip(),
                            'name': name.strip(),
                            'top': top
                        })
                
                # 2. Extract tables
                tables = []
                try:
                    tables = page.find_tables()
                except Exception as e:
                    logger.warning(f"pdfplumber failed on page {page_num}: {e}. Trying Camelot fallback.")
                    if camelot:
                        try:
                            # Camelot pages is 1-indexed
                            camelot_tables = camelot.read_pdf(str(self.pdf_path), pages=str(page_num))
                            page_height = page.height
                            for ct in camelot_tables:
                                x1, y1, x2, y2 = ct._bbox
                                ct_top = page_height - y2
                                class MockTable:
                                    def __init__(self, data, top):
                                        self.bbox = (0, top, 0, 0)
                                        self._data = data
                                    def extract(self):
                                        return self._data
                                tables.append(MockTable(ct.df.values.tolist(), ct_top))
                        except Exception as ce:
                            logger.error(f"Camelot fallback failed on page {page_num}: {ce}")
                            
                # Process each table on the page
                for t in tables:
                    table_top = t.bbox[1]
                    
                    # Search text lines backwards from table_top to find section context
                    section_suffix = ""
                    for top in sorted(lines.keys(), reverse=True):
                        if top < table_top:
                            line_words = sorted(lines[top], key=lambda x: x['x0'])
                            line_text = " ".join([w['text'] for w in line_words]).strip()
                            if "Seats Allotted to" in line_text or "State Level Seats" in line_text:
                                if "Allotted to Home University Candidates" in line_text:
                                    section_suffix = "-HU"
                                elif "Allotted to Other Than Home University Candidates" in line_text:
                                    section_suffix = "-OHU"
                                elif "State Level Seats" in line_text:
                                    section_suffix = "-SL"
                                break
                    
                    # Update local state machine for headers above this table on this page
                    # Find closest branch header above this table
                    assoc_branch = None
                    for h in reversed(page_headers):
                        if h['type'] == 'branch' and h['top'] < table_top:
                            assoc_branch = h
                            break
                            
                    # Find closest college header above the branch header (or table)
                    assoc_college = None
                    if assoc_branch:
                        for h in reversed(page_headers):
                            if h['type'] == 'college' and h['top'] < assoc_branch['top']:
                                assoc_college = h
                                break
                    else:
                        for h in reversed(page_headers):
                            if h['type'] == 'college' and h['top'] < table_top:
                                assoc_college = h
                                break
                                
                    # If headers are found on the page, update state. Otherwise carry over.
                    if assoc_college:
                        current_college_code = assoc_college['code']
                        current_college_name = assoc_college['name']
                    if assoc_branch:
                        current_branch_code = assoc_branch['code']
                        current_branch_name = assoc_branch['name']
                        
                    # Skip if we have no college/branch context yet (malformed PDF structure at top)
                    if not current_college_code or not current_branch_code:
                        logger.warning(f"Skipping table on page {page_num} due to lack of College/Branch context.")
                        continue
                        
                    # Extract table grid data
                    data = t.extract()
                    if not data or len(data) < 2:
                        continue
                        
                    headers = data[0]
                    for row in data[1:]:
                        if not row or len(row) < 2:
                            continue
                        
                        # First cell is row header (Stage)
                        row_header = row[0]
                        
                        for col_idx in range(1, len(row)):
                            if col_idx >= len(headers):
                                break
                            
                            seat_type = headers[col_idx]
                            cell_value = row[col_idx]
                            
                            if not seat_type or not cell_value:
                                continue
                                
                            cell_str = str(cell_value).strip()
                            if not cell_str:
                                continue
                                
                            # Normalize whitespace and newlines
                            cell_norm = cell_str.replace("\n", " ").replace(" ", "")
                            
                            m_cutoff = CUTOFF_PATTERN.match(cell_norm)
                            if m_cutoff:
                                rank_str, pct_str = m_cutoff.groups()
                                try:
                                    closing_rank = int(rank_str)
                                    closing_percentile = float(pct_str)
                                    category = parse_category(seat_type)
                                    stage_val = str(row_header).strip().replace("\n", " ") if row_header else "I"
                                    normalized_stage = stage_val + section_suffix
                                    
                                    records.append({
                                        "College Code": current_college_code,
                                        "College Name": current_college_name,
                                        "Branch Code": current_branch_code,
                                        "Branch Name": current_branch_name,
                                        "Year": 2025,
                                        "Round": 4,
                                        "Category": category,
                                        "Seat Type": seat_type.strip().replace("\n", ""),
                                        "Stage": normalized_stage,
                                        "Closing Rank": closing_rank,
                                        "Closing Percentile": closing_percentile
                                    })
                                except Exception as err:
                                    logger.warning(f"Error parsing row values: {err}. Value: {cell_str}")
                                    invalid_rows_skipped += 1
                            else:
                                # Not matching the rank/percentile pattern (e.g. empty or text)
                                invalid_rows_skipped += 1
                                
        # Create DataFrame
        df = pd.DataFrame(records)
        rows_extracted = len(df)
        
        # Clean data (remove duplicate rows)
        final_df = df.drop_duplicates()
        rows_removed = rows_extracted - len(final_df)
        
        elapsed_time = time.time() - start_time
        
        # Display logs
        logger.info(f"Rows Extracted: {rows_extracted}")
        logger.info(f"Rows Removed (Duplicates): {rows_removed}")
        logger.info(f"Final Row Count: {len(final_df)}")
        logger.info(f"Execution Time: {elapsed_time:.2f} seconds")
        
        print("\n" + "="*50)
        print("EXTRACTION SUMMARY")
        print("="*50)
        print(f"Pages Processed:      {pages_processed}")
        print(f"Raw Rows Extracted:   {rows_extracted}")
        print(f"Duplicate Rows Drop:  {rows_removed}")
        print(f"Invalid Rows Skipped: {invalid_rows_skipped}")
        print(f"Final Row Count:      {len(final_df)}")
        print(f"Execution Time:       {elapsed_time:.2f} seconds")
        print("="*50)
        
        # Store metadata for printing summary in CLI
        self.summary = {
            "pages_processed": pages_processed,
            "rows_extracted": len(final_df),
            "invalid_rows_skipped": invalid_rows_skipped,
            "execution_time": elapsed_time
        }
        
        return final_df
