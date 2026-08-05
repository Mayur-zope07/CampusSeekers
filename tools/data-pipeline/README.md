# CampusSeekers ETL Data Pipeline

This is a dedicated Data Engineering Workspace for the **CampusSeekers** platform. It operates as an independent developer tool designed to ingest, extract, clean, validate, and prepare admission datasets (e.g. cutoff percentiles, seat availability matrices, placements) from official Maharashtra CET PDFs into highly structured CSV files before importing them into PostgreSQL.

---

## Purpose

The Spring Boot backend requires structured data in PostgreSQL tables to deliver features like Admission Decision Support and Trend Analysis. However, raw data from the government is published as unstructured PDFs (often hundreds of pages). 

This pipeline bridges the gap by providing a scalable Python-based ETL workspace that transforms raw PDF documents into clean, validated CSV files matching the database schema.

---

## Workflow Diagram

The pipeline operates in standard sequential phases:

```
+-------------+      +------------+      +----------------+      +------------+      +------------+
|             |      |            |      |                |      |            |      |            |
|   Raw PDF   +----->+ Extraction +----->+ Transformation +----->+ Validation +----->+ CSV Export |
|             |      |            |      |                |      |            |      |            |
+-------------+      +------------+      +-------+--------+      +------------+      +-----+------+
                                                 |                                         |
                                                 v                                         v
                                         +-------+--------+                        +-------+--------+
                                         |                |                        |                |
                                         | Final Dataset  |<-----------------------+ Spring Boot    |
                                         |  (CSV Format)  |                        |  Bulk Import   |
                                         |                |                        |                |
                                         +----------------+                        +----------------+
```

1. **Raw PDF**: Official PDFs are dropped into course-specific raw data folders.
2. **Extraction**: `extractors` extract tabular data using libraries like `pdfplumber`, `camelot`, and `tabula-py`.
3. **Transformation**: `transformers` clean whitespace, normalize name spelling, parse data types, and map natural keys.
4. **Validation**: `validators` ensure CSV structure matches database column requirements and values satisfy semantic bounds (e.g., percentiles between 0 and 100).
5. **CSV Export**: `exporters` save the clean tables in final data folders.
6. **Spring Boot Bulk Import**: Ready CSV files are ingested by Spring Boot migration utilities or import endpoints to write straight to PostgreSQL.

---

## Folder Structure

```
tools/data-pipeline/
├── cli/                 # CLI interface and main entry points
│   └── main.py          # Command line runner routing pipeline commands
├── extractors/          # Parsers for specific PDF files (cutoffs, seats)
│   ├── cutoff_extractor.py
│   └── seat_matrix_extractor.py
├── transformers/        # Normalization and data cleaning routines
│   ├── clean_data.py
│   └── normalize_data.py
├── validators/          # Schemas and value checkers
│   ├── schema_validator.py
│   └── data_validator.py
├── exporters/           # Output formatting and export layers
│   └── csv_exporter.py
├── config/              # Pipeline run configurations
│   ├── settings.yaml    # Global parameters and directories mapping
│   └── config.py        # Path resolution and settings loading module
├── tests/               # Unit and regression test suite
│   ├── test_cutoff_extractor.py
│   └── test_seat_matrix_extractor.py
├── logs/                # Local runtime logs
├── output/              # Temporary staging folder for exported CSVs
├── utils.py             # Shared logging and directory helpers
├── requirements.txt     # Python environment requirements
├── README.md            # This documentation file
└── .gitignore           # File excludes for Python runtime and outputs
```

---

## Folder Directory Details

### `cli/`
Hosts the CLI interface (`main.py`). The script parses command-line arguments using `argparse`, orchestrating run pipelines per course/year (e.g. `python cli/main.py run-all --course engineering --year 2024`).

### `extractors/`
Contains Python modules leveraging low-level PDF parsing libraries to extract raw tables from PDFs.
*   `cutoff_extractor.py`: Extracts seat cutoff ranks and percentiles.
*   `seat_matrix_extractor.py`: Extracts college capacities and seat distribution numbers.

### `transformers/`
Applies transformations to the extracted pandas DataFrames:
*   `clean_data.py`: Strips text, fills empty fields, normalizes names, and filters out headers/footers.
*   `normalize_data.py`: Resolves codes (e.g., college codes, branch codes) and aligns text fields.

### `validators/`
Protects database integrity by asserting data quality before it leaves the pipeline.
*   `schema_validator.py`: Asserts structural equality (column matching) against templates.
*   `data_validator.py`: Evaluates data rules (non-negative capacities, percentiles bounds, non-null primary elements).

### `exporters/`
Generates output files:
*   `csv_exporter.py`: Standardizes column formatting and saves processed DataFrames.

### `config/`
Manages configuration and variables.
*   `settings.yaml`: Configures path relative offsets for root datasets (`raw`, `cleaned`, `final`, `templates`, `logs`), supported courses, and valid year ranges.
*   `config.py`: Automatically resolves YAML paths to absolute paths to prevent working-directory resolution errors.

### `tests/`
Pytest scripts to verify parser stability and logic routines on mock files.

---

## Setup & Running Guide

### 1. Set Up Virtual Environment
Initialize a virtual python environment:
```bash
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
```

### 2. Install Dependencies
Install dependencies defined in requirements file:
```bash
pip install -r requirements.txt
```

### 3. Running the pipeline
Use CLI commands to execute different pipeline components:

```bash
# Run full pipeline end-to-end
python cli/main.py run-all --course engineering --year 2024

# Extract cutoffs only
python cli/main.py extract --course engineering --year 2024

# Clean and transform
python cli/main.py transform --course engineering
```
