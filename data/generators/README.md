# Synthetic Dataset Generators

This directory contains Python scripts used to generate the synthetic financial dataset for Phase 2.

## Structure
- `generate_dataset.py`: The main script that generates `orders.csv`, `payments.csv`, `settlements.csv`, `bank_transactions.csv`, and `ground_truth.csv`.
- Scripts inject deliberate exceptions into the dataset such as `AMOUNT_MISMATCH`, `MISSING_SETTLEMENT`, `DUPLICATE_TRANSACTION`, `DATE_ANOMALY`, and `STATUS_MISMATCH`.
- These datasets will be used to test the deterministic reconciliation engine and the AI Finance Agent.

## Usage

```bash
pip install -r requirements.txt
python generate_dataset.py
```

The generated CSV files will be saved in the `data/` directory and a summary will be printed.
