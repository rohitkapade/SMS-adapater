--
-- PostgreSQL database dump
--

-- Dumped from database version 12.3
-- Dumped by pg_dump version 14.2 (Debian 14.2-1.pgdg110+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;



--
-- Name: uep_product_line_vc_mapping; Type: TABLE; Schema: public; Owner: uep_cv_rw
--

CREATE TABLE IF NOT EXISTS public.uep_product_line_vc_mapping (
    id bigint NOT NULL,
    lob character varying(255),
    pl character varying(255),
    ppl character varying(255),
    vc_number character varying(255),
    CONSTRAINT uep_product_line_vc_mapping_pkey PRIMARY KEY (id)
);


--
-- Name: uep_file_events; Type: TABLE; Schema: public; Owner: uep_cv_rw
--

CREATE TABLE IF NOT EXISTS public.uep_file_events (
    unique_id character varying(255) NOT NULL,
    creation_date_time timestamp without time zone,
    event_name character varying(255),
    payload jsonb,
    phone_number character varying(255),
    s3url text,
    status character varying(255),
    CONSTRAINT uep_file_events_pkey PRIMARY KEY (unique_id)
);


--
-- Name: fileevent_status_index; Type: INDEX; Schema: public; Owner: uep_cv_rw
--

CREATE INDEX IF NOT EXISTS fileevent_status_index ON public.uep_file_events USING btree (status);



--
-- Name: uep_email_sqs_event; Type: TABLE; Schema: public; Owner: uep_cv_rw
--

CREATE TABLE IF NOT EXISTS public.uep_email_sqs_event (
    file_name character varying(255) NOT NULL,
    crm_transaction_number character varying(255),
    event_time character varying(255),
    md5attachment_hash character varying(255),
    sequencer character varying(255),
    CONSTRAINT uep_email_sqs_event_pkey PRIMARY KEY (file_name)
);


--
-- Name: md5index; Type: INDEX; Schema: public; Owner: uep_cv_rw
--

CREATE INDEX IF NOT EXISTS md5index ON public.uep_email_sqs_event USING btree (md5attachment_hash);



--
-- Name: uep_event_data_retrieval_history; Type: TABLE; Schema: public; Owner: uep_cv_rw
--

CREATE TABLE IF NOT EXISTS public.uep_event_data_retrieval_history (
    event_name character varying(255) NOT NULL,
    date_time timestamp without time zone,
    CONSTRAINT uep_event_data_retrieval_history_pkey PRIMARY KEY (event_name)
);



--
-- Name: uep_opportunities; Type: TABLE; Schema: public; Owner: uep_cv_rw
--

CREATE TABLE IF NOT EXISTS public.uep_opportunities (
    opty_id character varying(255) NOT NULL,
    conversation_id character varying(255),
    opty_creation_date_time timestamp without time zone,
    phone_number character varying(255),
    CONSTRAINT uep_opportunities_pkey PRIMARY KEY (opty_id)
);


--
-- PostgreSQL database dump complete
--

