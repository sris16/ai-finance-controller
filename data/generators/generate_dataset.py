import pandas as pd
import numpy as np
import random
from datetime import datetime, timedelta
import os

# Ensure reproducibility
SEED = 42
random.seed(SEED)
np.random.seed(SEED)

NUM_RECORDS = 100
DATA_DIR = os.path.join(os.path.dirname(__file__), "..")

def generate_dataset():
    orders = []
    payments = []
    settlements = []
    bank_transactions = []
    ground_truth = []

    # Assign exception types
    exception_types = [
        "AMOUNT_MISMATCH",
        "MISSING_SETTLEMENT",
        "DUPLICATE_TRANSACTION",
        "DATE_ANOMALY",
        "STATUS_MISMATCH"
    ]
    
    # 20 exceptions, 4 of each type
    exception_assignments = []
    for ex in exception_types:
        exception_assignments.extend([ex] * 4)
    
    # Shuffle the assignments and pad with NONE
    exception_assignments.extend(["NONE"] * (NUM_RECORDS - len(exception_assignments)))
    random.shuffle(exception_assignments)

    base_time = datetime(2026, 1, 1, 10, 0, 0)

    for i in range(NUM_RECORDS):
        # Base deterministic IDs
        order_id = f"ORD{i+1:04d}"
        payment_id = f"PAY{i+1:04d}"
        customer_id = f"CUST{random.randint(1000, 9999)}"
        
        # Base Values
        base_amount = round(random.uniform(500.0, 15000.0), 2)
        order_date = base_time + timedelta(minutes=random.randint(1, 40000))
        payment_date = order_date + timedelta(minutes=random.randint(1, 15))
        settlement_date = payment_date + timedelta(days=2)
        transaction_date = settlement_date + timedelta(hours=random.randint(2, 12))
        
        order_status = "PAID"
        payment_status = "CAPTURED"
        settlement_status = "SETTLED"
        txn_status = "SUCCESS"
        
        gross_amount = base_amount
        fee = round(gross_amount * 0.02, 2)  # 2% fee
        net_amount = round(gross_amount - fee, 2)
        
        exception_type = exception_assignments[i]
        
        # Apply exception logic
        create_settlement = True
        create_bank_txn = True
        create_duplicate_bank_txn = False
        
        if exception_type == "AMOUNT_MISMATCH":
            # E.g., gateway settles less than the captured amount unexpectedly
            gross_amount = round(base_amount - 100.0, 2)
            if gross_amount < 0:
                gross_amount = round(base_amount + 100.0, 2)
            net_amount = round(gross_amount - fee, 2)
        
        elif exception_type == "MISSING_SETTLEMENT":
            create_settlement = False
            create_bank_txn = False
            
        elif exception_type == "DUPLICATE_TRANSACTION":
            create_duplicate_bank_txn = True
            
        elif exception_type == "DATE_ANOMALY":
            # Settlement date is before payment date
            settlement_date = payment_date - timedelta(days=1)
            transaction_date = settlement_date + timedelta(hours=1)
            
        elif exception_type == "STATUS_MISMATCH":
            payment_status = "FAILED"
            order_status = "PENDING"
            # But settlement and bank transaction happen anyway!

        # 1. Order
        orders.append({
            "order_id": order_id,
            "customer_id": customer_id,
            "order_date": order_date.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "amount": base_amount,
            "currency": "INR",
            "status": order_status
        })

        # 2. Payment
        payments.append({
            "payment_id": payment_id,
            "order_id": order_id,
            "payment_date": payment_date.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "amount": base_amount,
            "payment_method": random.choice(["UPI", "CARD", "NETBANKING"]),
            "status": payment_status
        })

        # 3. Settlement
        if create_settlement:
            settlement_id = f"SET{len(settlements)+1:04d}"
            settlements.append({
                "settlement_id": settlement_id,
                "payment_id": payment_id,
                "settlement_date": settlement_date.strftime("%Y-%m-%dT%H:%M:%SZ"),
                "gross_amount": gross_amount,
                "fee": fee,
                "net_amount": net_amount,
                "status": settlement_status
            })

        # 4. Bank Transaction
        if create_bank_txn:
            txn_id = f"TXN{len(bank_transactions)+1:04d}"
            bank_transactions.append({
                "transaction_id": txn_id,
                "payment_id": payment_id,
                "transaction_date": transaction_date.strftime("%Y-%m-%dT%H:%M:%SZ"),
                "amount": net_amount,
                "transaction_type": "CREDIT",
                "status": txn_status
            })
            
            if create_duplicate_bank_txn:
                txn_id_dup = f"TXN{len(bank_transactions)+1:04d}"
                bank_transactions.append({
                    "transaction_id": txn_id_dup,
                    "payment_id": payment_id,
                    "transaction_date": (transaction_date + timedelta(minutes=5)).strftime("%Y-%m-%dT%H:%M:%SZ"),
                    "amount": net_amount,
                    "transaction_type": "CREDIT",
                    "status": txn_status
                })

        # 5. Ground Truth
        ground_truth.append({
            "payment_id": payment_id,
            "expected_result": "MATCH" if exception_type == "NONE" else "EXCEPTION",
            "exception_type": exception_type
        })

    # Convert to DataFrames
    df_orders = pd.DataFrame(orders)
    df_payments = pd.DataFrame(payments)
    df_settlements = pd.DataFrame(settlements)
    df_bank_txns = pd.DataFrame(bank_transactions)
    df_ground_truth = pd.DataFrame(ground_truth)

    # Validation
    assert len(df_orders) == NUM_RECORDS, "Orders count mismatch"
    assert len(df_payments) == NUM_RECORDS, "Payments count mismatch"
    assert len(df_ground_truth) == NUM_RECORDS, "Ground truth count mismatch"
    assert df_orders["order_id"].nunique() == NUM_RECORDS, "Order IDs not unique"
    assert df_payments["payment_id"].nunique() == NUM_RECORDS, "Payment IDs not unique"
    
    # Monetary values validation
    assert (df_orders["amount"] >= 0).all(), "Negative order amounts found"
    assert (df_payments["amount"] >= 0).all(), "Negative payment amounts found"
    if not df_settlements.empty:
        assert (df_settlements["gross_amount"] >= 0).all(), "Negative gross amounts found"
        assert (df_settlements["fee"] >= 0).all(), "Negative fees found"
        assert (df_settlements["net_amount"] >= 0).all(), "Negative net amounts found"
    if not df_bank_txns.empty:
        assert (df_bank_txns["amount"] >= 0).all(), "Negative bank txn amounts found"

    # Currency validation
    assert (df_orders["currency"] == "INR").all(), "Non-INR currencies found"
    
    # Expected exception counts
    exception_counts = df_ground_truth["exception_type"].value_counts()
    
    # Save to CSV
    paths = {
        "Orders": os.path.join(DATA_DIR, "orders.csv"),
        "Payments": os.path.join(DATA_DIR, "payments.csv"),
        "Settlements": os.path.join(DATA_DIR, "settlements.csv"),
        "Bank transactions": os.path.join(DATA_DIR, "bank_transactions.csv"),
        "Ground truth": os.path.join(DATA_DIR, "ground_truth.csv")
    }
    
    df_orders.to_csv(paths["Orders"], index=False)
    df_payments.to_csv(paths["Payments"], index=False)
    df_settlements.to_csv(paths["Settlements"], index=False)
    df_bank_txns.to_csv(paths["Bank transactions"], index=False)
    df_ground_truth.to_csv(paths["Ground truth"], index=False)
    
    # Print Summary
    print("Dataset generation complete.\n")
    print(f"Orders: {len(df_orders)}")
    print(f"Payments: {len(df_payments)}")
    print(f"Settlements: {len(df_settlements)}")
    print(f"Bank transactions: {len(df_bank_txns)}\n")
    
    expected_matches = len(df_ground_truth[df_ground_truth["expected_result"] == "MATCH"])
    expected_exceptions = len(df_ground_truth[df_ground_truth["expected_result"] == "EXCEPTION"])
    
    print(f"Expected matches: {expected_matches}")
    print(f"Expected exceptions: {expected_exceptions}\n")
    
    print("Exception breakdown:")
    for ex_type in exception_types:
        count = exception_counts.get(ex_type, 0)
        print(f"{ex_type}: {count}")
    
    print("\nOutput paths:")
    for name, path in paths.items():
        print(f"- {name}: {os.path.abspath(path)}")

if __name__ == "__main__":
    generate_dataset()
