import argparse
import sys
import time
import json
from pathlib import Path

# Add the parent folder to the system path to allow importing sibling modules when running directly
sys.path.append(str(Path(__file__).resolve().parent.parent))

from utils import setup_logging, ensure_directory
from config.config import settings
from extractors.cutoff_extractor import CutoffExtractor
from extractors.seat_matrix_extractor import SeatMatrixExtractor

logger = setup_logging("cli_main")

def run_cutoff_extraction_2025():
    """
    Executes Phase 1 CAP Cutoff PDF Extractor for the year 2025.
    Loads data/raw/engineering/2025/cutoffs/2025ENGG_CAP4_CutOff.pdf,
    runs extraction, and exports to data/processed/cutoffs_2025_raw.csv.
    """
    base_dir = Path(__file__).resolve().parent.parent
    repo_root = base_dir.parent.parent
    
    pdf_path = repo_root / "data" / "raw" / "engineering" / "2025" / "cutoffs" / "2025ENGG_CAP4_CutOff.pdf"
    output_dir = repo_root / "data" / "processed"
    output_path = output_dir / "cutoffs_2025_raw.csv"
    
    if not pdf_path.exists():
        logger.error(f"Missing PDF: Target file not found at {pdf_path}")
        print(f"Error: Target PDF file not found at {pdf_path}")
        sys.exit(1)
        
    ensure_directory(output_dir)
    
    try:
        extractor = CutoffExtractor(pdf_path)
        df = extractor.extract()
        
        logger.info(f"Saving extracted rows to {output_path}")
        df.to_csv(output_path, index=False, encoding="utf-8")
        logger.info("CSV generation complete.")
        print(f"CSV generated successfully at: {output_path}")
        
    except Exception as e:
        logger.error(f"Table extraction failure: {str(e)}", exc_info=True)
        print(f"Error during extraction process: {str(e)}")
        sys.exit(1)

def run_seat_matrix_extraction(input_path: str, year: int):
    """
    Executes Phase 2 Seat Matrix PDF Extractor.
    Loads the PDF, extracts tables and text fallbacks, cleans, validates,
    and saves raw, clean, final CSVs and metadata JSON.
    """
    base_dir = Path(__file__).resolve().parent.parent
    repo_root = base_dir.parent.parent
    
    pdf_path = Path(input_path)
    if not pdf_path.is_absolute():
        pdf_path = repo_root / pdf_path
        
    if not pdf_path.exists():
        # Fallback to local cwd resolution
        pdf_path = Path(input_path).resolve()
        if not pdf_path.exists():
            logger.error(f"Missing PDF: Target file not found at {input_path}")
            print(f"Error: Target PDF file not found at {input_path}")
            sys.exit(1)
            
    processed_dir = repo_root / "data" / "processed"
    cleaned_dir = repo_root / "data" / "cleaned"
    final_dir = repo_root / "data" / "final" / "seat_matrix"
    
    raw_path = processed_dir / f"seat_matrix_{year}_raw.csv"
    clean_path = cleaned_dir / f"seat_matrix_{year}_clean.csv"
    final_path = final_dir / f"seat_matrix_{year}_final.csv"
    metadata_path = processed_dir / f"seat_matrix_{year}_metadata.json"
    
    ensure_directory(processed_dir)
    ensure_directory(cleaned_dir)
    ensure_directory(final_dir)
    
    try:
        extractor = SeatMatrixExtractor(pdf_path)
        raw_df, clean_df, final_df, metadata = extractor.extract()
        
        # Override the year in metadata with the supplied year argument
        metadata["year"] = int(year)
        metadata["source"] = pdf_path.name
        
        # Save output CSVs
        logger.info(f"Saving raw CSV to {raw_path}")
        raw_df.to_csv(raw_path, index=False, encoding="utf-8")
        
        logger.info(f"Saving clean CSV to {clean_path}")
        clean_df.to_csv(clean_path, index=False, encoding="utf-8")
        
        logger.info(f"Saving final CSV to {final_path}")
        final_df.to_csv(final_path, index=False, encoding="utf-8")
        
        # Save metadata JSON
        logger.info(f"Saving metadata JSON to {metadata_path}")
        with open(metadata_path, "w", encoding="utf-8") as mf:
            json.dump(metadata, mf, indent=2)
            
        print(f"\n[SUCCESS] Output Files Generated:")
        print(f"  Raw CSV:       {raw_path}")
        print(f"  Clean CSV:    {clean_path}")
        print(f"  Final CSV:    {final_path}")
        print(f"  Metadata JSON: {metadata_path}")
        
    except Exception as e:
        logger.error(f"Seat matrix ETL failure: {str(e)}", exc_info=True)
        print(f"Error during seat matrix extraction process: {str(e)}")
        sys.exit(1)

def run_extraction(args):
    """Subparser action handler for extract command."""
    if args.course == "engineering" and args.year == 2025:
        run_cutoff_extraction_2025()
    else:
        logger.warning("Only course 'engineering' and year '2025' are supported for PDF extraction currently.")
        print("Feature is only implemented for --course engineering --year 2025")

def main():
    parser = argparse.ArgumentParser(
        description="CampusSeekers Data Engineering Pipeline CLI tool."
    )
    
    # Support top-level optional flags for Phase 1 & 2 success criteria
    parser.add_argument("--type", choices=["cutoff", "seatmatrix", "seat_matrix"], help="Type of pipeline to run")
    parser.add_argument("--year", type=int, help="Target year for extraction")
    parser.add_argument("--input", type=str, help="Path to input PDF file (required for seat matrix)")
    
    # Keep subparsers for other flows
    subparsers = parser.add_subparsers(dest="command", help="Pipeline stages to run")
    
    # Extract Command subparser
    course_choices = settings.get("supported_courses", ["engineering"])
    year_choices = settings.get("supported_years", [2025])
    
    extract_parser = subparsers.add_parser("extract", help="Extract raw data from admission PDFs")
    extract_parser.add_argument("--course", choices=course_choices, required=True, help="Course type")
    extract_parser.add_argument("--year", type=int, choices=year_choices, required=True, help="Admission year")
    extract_parser.set_defaults(func=run_extraction)
    
    args = parser.parse_args()
    
    # Route execution based on inputs
    if args.type == "cutoff":
        if args.year == 2025:
            run_cutoff_extraction_2025()
        else:
            print("Error: For cutoff type, only year 2025 is currently supported.")
            sys.exit(1)
    elif args.type in ("seatmatrix", "seat_matrix"):
        if not args.input:
            print("Error: --input <pdf_path> is required when --type seatmatrix is specified.")
            sys.exit(1)
        if not args.year:
            print("Error: --year <year> is required when --type seatmatrix is specified.")
            sys.exit(1)
        run_seat_matrix_extraction(args.input, args.year)
    elif args.command:
        try:
            args.func(args)
        except AttributeError:
            parser.print_help()
            sys.exit(1)
    else:
        parser.print_help()
        sys.exit(1)

if __name__ == "__main__":
    main()
