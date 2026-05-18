CREATE TABLE expenses (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL,
    amount           DECIMAL(19,4) NOT NULL,
    currency         VARCHAR(3) NOT NULL DEFAULT 'EUR',
    category         VARCHAR(50) NOT NULL,
    description      VARCHAR(500),
    expense_date     DATE NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_user_category ON expenses(user_id, category);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date DESC);