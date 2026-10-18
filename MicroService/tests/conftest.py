import sys
from pathlib import Path

# Lets the tests import app.py and analyzer.py from the MicroService folder.
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
