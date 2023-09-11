CREATE TABLE IF NOT EXISTS uep_cv_opportunities(
    id bigint NOT NULL,
    phone_number character varying(255),
    request jsonb,
    opportunity_id character varying(255),
    date_time timestamp without time zone,
    PRIMARY KEY(id)
);


CREATE SEQUENCE IF NOT EXISTS opportunity_id_primary_key_sequence START 1;