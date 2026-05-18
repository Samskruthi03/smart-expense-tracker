CREATE TABLE monthly_summaries (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL,
    year          INT NOT NULL,
    month         INT NOT NULL,
    category      VARCHAR(50) NOT NULL,
    total_amount  DECIMAL(19,4) NOT NULL DEFAULT 0,
    expense_count INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, year, month, category)
);

CREATE INDEX idx_summaries_user_year_month ON monthly_summaries(user_id, year, month);

CREATE TABLE budget_alerts (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL,
    year          INT NOT NULL,
    month         INT NOT NULL,
    budget_limit  DECIMAL(19,4) NOT NULL,
    spent_amount  DECIMAL(19,4) NOT NULL,
    alert_type    VARCHAR(20) NOT NULL,
    fired_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alerts_user ON budget_alerts(user_id, year, month);