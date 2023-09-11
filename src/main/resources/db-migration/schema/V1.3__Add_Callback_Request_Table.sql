CREATE TABLE IF NOT EXISTS uep_callback_request(
    id bigint NOT NULL,
    start_date_time timestamp with time zone,
    end_date_time timestamp with time zone,
    mobile_number character varying(255),
    customer_id character varying(255),
    callback_request_status character varying(255),
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    updated_by varchar(255) NULL,

    PRIMARY KEY(id)
);

CREATE SEQUENCE IF NOT EXISTS uep_callback_request_id_primary_key_sequence START 1;