CREATE TABLE IF NOT EXISTS PUBLIC.Email_Logs (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    user_id UUID,
    status_message VARCHAR,
    email_sent_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT email_log_id_PK PRIMARY KEY (id)
);