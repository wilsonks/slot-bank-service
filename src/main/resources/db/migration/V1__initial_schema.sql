CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    player_uid VARCHAR(255) NOT NULL UNIQUE,
    balance BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    account_uid VARCHAR(255) NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    amount BIGINT NOT NULL,
    reference_id VARCHAR(255),
    egm_id VARCHAR(255),
    balance_after BIGINT NOT NULL,
    trans_by VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_account FOREIGN KEY (account_uid) REFERENCES accounts(player_uid)
);

CREATE INDEX idx_ledger_account_uid ON ledger_entries(account_uid);
CREATE INDEX idx_ledger_entry_type ON ledger_entries(entry_type);
CREATE INDEX idx_ledger_created_at ON ledger_entries(created_at);
CREATE INDEX idx_ledger_egm_id ON ledger_entries(egm_id);
CREATE UNIQUE INDEX idx_ledger_reference_id_type ON ledger_entries(reference_id, entry_type) WHERE reference_id IS NOT NULL;

CREATE TABLE spin_reservations (
    id BIGSERIAL PRIMARY KEY,
    spin_id VARCHAR(255) NOT NULL UNIQUE,
    account_uid VARCHAR(255) NOT NULL,
    reserved_amount BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    balance_after_reserve BIGINT,
    balance_after_settle BIGINT,
    win_amount BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reservation_account FOREIGN KEY (account_uid) REFERENCES accounts(player_uid)
);

CREATE INDEX idx_spin_reservations_account_uid ON spin_reservations(account_uid);
CREATE INDEX idx_spin_reservations_status ON spin_reservations(status);
