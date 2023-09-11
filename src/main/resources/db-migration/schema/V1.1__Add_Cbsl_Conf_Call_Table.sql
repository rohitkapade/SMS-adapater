CREATE TABLE IF NOT EXISTS uep_cbsl_conf_call_response(
    sid character varying(255),
    from_phone_number character varying(255),
    initiated_start_time timestamp with time zone,
    status character varying(255),
    dial_call_duration character varying(255),
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    recording_url text,
    PRIMARY KEY(sid)
);
