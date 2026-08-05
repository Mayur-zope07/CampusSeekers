import logging
import os
from pathlib import Path
from typing import Union
from config.config import settings

def setup_logging(logger_name: str = "data_pipeline") -> logging.Logger:
    """
    Sets up application-wide logging to output to both console and a log file.
    Logs are written to the log directory specified in settings.
    """
    log_dir = Path(settings.get("log_directory", Path(__file__).resolve().parent / "logs"))
    log_dir.mkdir(parents=True, exist_ok=True)
    
    log_file = log_dir / f"{logger_name}.log"
    
    logger = logging.getLogger(logger_name)
    logger.setLevel(logging.INFO)
    
    # Avoid duplicating handlers if logger is already set up
    if not logger.handlers:
        # Create formatter
        formatter = logging.Formatter(
            '[%(asctime)s] %(levelname)s [%(name)s.%(funcName)s:%(lineno)d] %(message)s',
            datefmt='%Y-%m-%d %H:%M:%S'
        )
        
        # File handler
        fh = logging.FileHandler(log_file, encoding='utf-8')
        fh.setLevel(logging.INFO)
        fh.setFormatter(formatter)
        logger.addHandler(fh)
        
        # Console handler
        ch = logging.StreamHandler()
        ch.setLevel(logging.INFO)
        ch.setFormatter(formatter)
        logger.addHandler(ch)
        
    return logger

def ensure_directory(path: Union[str, Path]) -> Path:
    """
    Ensures that a directory exists by creating it and any parent folders if missing.
    """
    p = Path(path)
    p.mkdir(parents=True, exist_ok=True)
    return p

def get_raw_filepath(course: str, year: int, filename: str) -> Path:
    """
    Constructs and returns the path to a raw input PDF file, checking if it is supported.
    """
    raw_dir = Path(settings["raw_data_directory"])
    supported_courses = settings["supported_courses"]
    supported_years = settings["supported_years"]
    
    if course not in supported_courses:
        raise ValueError(f"Unsupported course '{course}'. Supported: {supported_courses}")
        
    if year not in supported_years:
        raise ValueError(f"Unsupported year {year}. Supported: {supported_years}")
        
    filepath = raw_dir / course / str(year) / filename
    return filepath
