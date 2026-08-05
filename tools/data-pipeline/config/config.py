import os
import yaml
from pathlib import Path

# Base directory for the data pipeline tool (CampusSeekers/tools/data-pipeline)
BASE_DIR = Path(__file__).resolve().parent.parent

# Path to the settings file
SETTINGS_PATH = BASE_DIR / "config" / "settings.yaml"

def load_settings():
    """
    Loads configuration settings from settings.yaml and converts relative
    data directory paths to resolved absolute path objects.
    """
    if not SETTINGS_PATH.exists():
        raise FileNotFoundError(f"Configuration file not found at {SETTINGS_PATH}")
    
    with open(SETTINGS_PATH, "r", encoding="utf-8") as f:
        config = yaml.safe_load(f)
    
    # Resolve relative directories to absolute paths
    dir_keys = [
        "raw_data_directory", 
        "processed_directory", 
        "cleaned_directory", 
        "final_directory", 
        "template_directory",
        "log_directory"
    ]
    for key in dir_keys:
        if key in config:
            raw_path = Path(config[key])
            # Resolve relative path against BASE_DIR
            if not raw_path.is_absolute():
                config[key] = (BASE_DIR / raw_path).resolve()
            else:
                config[key] = raw_path.resolve()
                
    return config

# Load settings as a module-level variable
try:
    settings = load_settings()
except Exception as e:
    # Failback default configuration
    settings = {
        "raw_data_directory": (BASE_DIR / "../../data/raw").resolve(),
        "processed_directory": (BASE_DIR / "../../data/processed").resolve(),
        "cleaned_directory": (BASE_DIR / "../../data/cleaned").resolve(),
        "final_directory": (BASE_DIR / "../../data/final").resolve(),
        "template_directory": (BASE_DIR / "../../data/templates").resolve(),
        "log_directory": (BASE_DIR / "../../data/logs").resolve(),
        "supported_years": [2023, 2024, 2025],
        "supported_courses": ["engineering", "medical", "mba", "mca", "pharmacy"],
        "supported_file_types": ["pdf", "csv"]
    }
