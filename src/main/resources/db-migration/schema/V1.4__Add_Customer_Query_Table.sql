CREATE TABLE IF NOT EXISTS public.customer_query (
	id bigserial NOT NULL,
	customer_id varchar(255) NULL,
	mobile_number varchar(255) NULL,
	query varchar NULL,
	image_url varchar NULL,
	status varchar(20),
	created_at timestamp with time zone,
	updated_at timestamp with time zone,
	updated_by varchar(255) NULL,
	CONSTRAINT customer_query_pk PRIMARY KEY (id)
);
