CREATE TABLE IF NOT EXISTS public.customer_feedback (
	feedback_id bigserial NOT NULL,
	mobile_number varchar(25),
	category varchar(25),
	feedback_sentiment varchar(25),
	group_name varchar(20),
	created_at timestamp with time zone,
	is_user_defined boolean,
	feedback_context jsonb,
	CONSTRAINT customer_feedback_pk PRIMARY KEY (feedback_id)
);
