-- V1__create_transactions_table.sql

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS transactions (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            crypto_id VARCHAR(255) NOT NULL,
                                            user_id VARCHAR(255) NOT NULL,
                                            transaction_type VARCHAR(50) NOT NULL,
                                            quantity NUMERIC(18, 8) NOT NULL,
                                            total_spent NUMERIC(18, 2) NOT NULL,
                                            price_per_coin NUMERIC(18, 8) NOT NULL,
                                            status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                            saga_id VARCHAR(255),
                                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                            updated_at TIMESTAMP,
                                            failure_reason TEXT
);
