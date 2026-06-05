--
-- PostgreSQL database dump
--

\restrict rtLU1ynBfbdBh2tdA4aN9mlaCOjPc6c69iH21eteWvruavVO6260JiBSx1Y7iJC

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

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
-- Name: send_bill_notification(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.send_bill_notification(p_bill_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_customer_id BIGINT;
    v_customer_name VARCHAR(150);
    v_total_amount DECIMAL(12,2);
    v_month INT;
    v_year INT;
    v_month_name VARCHAR(20);
    v_message_text TEXT;
    v_month_year VARCHAR(20);
BEGIN
    SELECT b.customer_id, c.full_name, b.total_amount, b.billing_month, b.billing_year
    INTO v_customer_id, v_customer_name, v_total_amount, v_month, v_year
    FROM bills b
    INNER JOIN customers c ON b.customer_id = c.id
    WHERE b.id = p_bill_id;

    v_month_name := CASE v_month
        WHEN 1 THEN 'January' WHEN 2 THEN 'February' WHEN 3 THEN 'March'
        WHEN 4 THEN 'April' WHEN 5 THEN 'May' WHEN 6 THEN 'June'
        WHEN 7 THEN 'July' WHEN 8 THEN 'August' WHEN 9 THEN 'September'
        WHEN 10 THEN 'October' WHEN 11 THEN 'November' WHEN 12 THEN 'December'
    END;

    v_month_year := LPAD(v_month::TEXT, 2, '0') || '-' || v_year::TEXT;

    v_message_text := 'Dear ' || v_customer_name ||
        ', Your ' || v_month_name || '/' || v_year ||
        ' utility bill of ' || TO_CHAR(v_total_amount, 'FM999999999.00') ||
        ' FRW has been successfully processed.';

    INSERT INTO notifications (customer_id, message, month, year, month_year, sent_at)
    VALUES (v_customer_id, v_message_text, v_month, v_year, v_month_year, CURRENT_TIMESTAMP);
END;
$$;


--
-- Name: send_bill_notification_trigger(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.send_bill_notification_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM send_bill_notification(NEW.id);
    RETURN NEW;
END;
$$;


--
-- Name: send_bill_paid_notification_trigger(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.send_bill_paid_notification_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF OLD.status <> 'PAID' AND NEW.status = 'PAID' THEN
        PERFORM send_payment_notification(NEW.id);
    END IF;
    RETURN NEW;
END;
$$;


--
-- Name: send_payment_notification(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.send_payment_notification(p_bill_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_customer_id BIGINT;
    v_customer_name VARCHAR(150);
    v_total_amount DECIMAL(12,2);
    v_month INT;
    v_year INT;
    v_month_name VARCHAR(20);
    v_message_text TEXT;
    v_month_year VARCHAR(20);
BEGIN
    SELECT b.customer_id, c.full_name, b.total_amount, b.billing_month, b.billing_year
    INTO v_customer_id, v_customer_name, v_total_amount, v_month, v_year
    FROM bills b
    INNER JOIN customers c ON b.customer_id = c.id
    WHERE b.id = p_bill_id;

    v_month_name := CASE v_month
        WHEN 1 THEN 'January' WHEN 2 THEN 'February' WHEN 3 THEN 'March'
        WHEN 4 THEN 'April' WHEN 5 THEN 'May' WHEN 6 THEN 'June'
        WHEN 7 THEN 'July' WHEN 8 THEN 'August' WHEN 9 THEN 'September'
        WHEN 10 THEN 'October' WHEN 11 THEN 'November' WHEN 12 THEN 'December'
    END;

    v_month_year := LPAD(v_month::TEXT, 2, '0') || '-' || v_year::TEXT;

    v_message_text := 'Dear ' || v_customer_name ||
        ', Your ' || v_month_name || '/' || v_year ||
        ' utility bill of ' || TO_CHAR(v_total_amount, 'FM999999999.00') ||
        ' FRW has been fully paid. Thank you.';

    INSERT INTO notifications (customer_id, message, month, year, month_year, sent_at)
    VALUES (v_customer_id, v_message_text, v_month, v_year, v_month_year, CURRENT_TIMESTAMP);
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: bills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bills (
    id bigint NOT NULL,
    amount_paid numeric(12,2) NOT NULL,
    balance numeric(12,2) NOT NULL,
    billing_month integer NOT NULL,
    billing_year integer NOT NULL,
    consumption numeric(12,2) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    fixed_charge_amount numeric(12,2) NOT NULL,
    penalty_amount numeric(12,2) NOT NULL,
    reference character varying(50) NOT NULL,
    status character varying(20) NOT NULL,
    tariff_amount numeric(12,2) NOT NULL,
    tax_amount numeric(12,2) NOT NULL,
    total_amount numeric(12,2) NOT NULL,
    updated_at timestamp(6) without time zone,
    customer_id bigint NOT NULL,
    meter_id bigint NOT NULL,
    meter_reading_id bigint NOT NULL,
    CONSTRAINT bills_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PARTIALLY_PAID'::character varying, 'PAID'::character varying, 'OVERDUE'::character varying])::text[])))
);


--
-- Name: bills_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.bills ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.bills_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: customer_notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customer_notifications (
    id bigint NOT NULL,
    message character varying(2000) NOT NULL,
    month integer NOT NULL,
    month_year character varying(20) NOT NULL,
    sent_at timestamp(6) without time zone NOT NULL,
    year integer NOT NULL,
    customer_id bigint NOT NULL,
    billing_month integer NOT NULL,
    billing_year integer NOT NULL
);


--
-- Name: customer_notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.customer_notifications ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.customer_notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: customers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customers (
    id bigint NOT NULL,
    address character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(150) NOT NULL,
    full_name character varying(150) NOT NULL,
    national_id character varying(20) NOT NULL,
    phone character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT customers_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);


--
-- Name: customers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.customers ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.customers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: fixed_charges; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fixed_charges (
    id bigint NOT NULL,
    amount numeric(12,2) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    effective_from date NOT NULL,
    name character varying(100) NOT NULL
);


--
-- Name: fixed_charges_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.fixed_charges ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.fixed_charges_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: meter_readings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.meter_readings (
    id bigint NOT NULL,
    billing_month integer NOT NULL,
    billing_year integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    current_reading numeric(12,2) NOT NULL,
    previous_reading numeric(12,2) NOT NULL,
    reading_date date NOT NULL,
    meter_id bigint NOT NULL
);


--
-- Name: meter_readings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.meter_readings ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.meter_readings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: meters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.meters (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    installation_date date NOT NULL,
    meter_number character varying(50) NOT NULL,
    status character varying(20) NOT NULL,
    type character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    customer_id bigint NOT NULL,
    CONSTRAINT meters_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[]))),
    CONSTRAINT meters_type_check CHECK (((type)::text = ANY ((ARRAY['WATER'::character varying, 'ELECTRICITY'::character varying])::text[])))
);


--
-- Name: meters_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.meters ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.meters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    message text NOT NULL,
    month integer NOT NULL,
    month_year character varying(20) NOT NULL,
    sent_at timestamp(6) without time zone NOT NULL,
    year integer NOT NULL,
    customer_id bigint NOT NULL
);


--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.notifications ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: otps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.otps (
    id bigint NOT NULL,
    attempts integer NOT NULL,
    code character varying(6) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(150) NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    purpose character varying(20) NOT NULL,
    verified boolean NOT NULL,
    CONSTRAINT otps_purpose_check CHECK (((purpose)::text = ANY ((ARRAY['SIGNUP'::character varying, 'LOGIN'::character varying])::text[])))
);


--
-- Name: otps_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.otps ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.otps_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id bigint NOT NULL,
    amount numeric(12,2) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    notes character varying(255),
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    bill_id bigint NOT NULL,
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


--
-- Name: payments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.payments ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.payments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: penalties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penalties (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    effective_from date NOT NULL,
    name character varying(100) NOT NULL,
    percentage numeric(5,2) NOT NULL
);


--
-- Name: penalties_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.penalties ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.penalties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tariff_tiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tariff_tiers (
    id bigint NOT NULL,
    from_units numeric(12,2) NOT NULL,
    rate_per_unit numeric(12,2) NOT NULL,
    to_units numeric(12,2),
    tariff_id bigint NOT NULL
);


--
-- Name: tariff_tiers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tariff_tiers ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tariff_tiers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tariffs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tariffs (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    effective_from date NOT NULL,
    flat_rate numeric(12,2),
    meter_type character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    tariff_type character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    version integer NOT NULL,
    CONSTRAINT tariffs_meter_type_check CHECK (((meter_type)::text = ANY ((ARRAY['WATER'::character varying, 'ELECTRICITY'::character varying])::text[]))),
    CONSTRAINT tariffs_tariff_type_check CHECK (((tariff_type)::text = ANY ((ARRAY['FLAT'::character varying, 'TIER'::character varying])::text[])))
);


--
-- Name: tariffs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tariffs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tariffs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: taxes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.taxes (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    effective_from date NOT NULL,
    name character varying(100) NOT NULL,
    percentage numeric(5,2) NOT NULL
);


--
-- Name: taxes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.taxes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.taxes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    country_code character varying(10) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(150) NOT NULL,
    full_name character varying(150) NOT NULL,
    password character varying(255) NOT NULL,
    phone_number character varying(20) NOT NULL,
    role character varying(30) NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    customer_id bigint,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ROLE_ADMIN'::character varying, 'ROLE_OPERATOR'::character varying, 'ROLE_FINANCE'::character varying, 'ROLE_CUSTOMER'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: bills; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.bills (id, amount_paid, balance, billing_month, billing_year, consumption, created_at, fixed_charge_amount, penalty_amount, reference, status, tariff_amount, tax_amount, total_amount, updated_at, customer_id, meter_id, meter_reading_id) FROM stdin;
1	4305.00	0.00	6	2026	25.00	2026-06-05 10:55:17.625471	500.00	175.00	BILL-F20DB697	PAID	3000.00	630.00	4305.00	2026-06-05 10:55:18.154446	1	2	1
\.


--
-- Data for Name: customer_notifications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.customer_notifications (id, message, month, month_year, sent_at, year, customer_id, billing_month, billing_year) FROM stdin;
\.


--
-- Data for Name: customers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.customers (id, address, created_at, email, full_name, national_id, phone, status, updated_at) FROM stdin;
1	Kigali, Gasabo, Remera	2026-06-05 10:44:05.908777	customer.demo@wasac.rw	Jean Uwimana	1199880077665544	0788123456	ACTIVE	2026-06-05 10:44:05.908777
\.


--
-- Data for Name: fixed_charges; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fixed_charges (id, amount, created_at, effective_from, name) FROM stdin;
1	500.00	2026-06-05 10:44:06.494329	2024-01-01	Monthly Service Fee
\.


--
-- Data for Name: meter_readings; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.meter_readings (id, billing_month, billing_year, created_at, current_reading, previous_reading, reading_date, meter_id) FROM stdin;
1	6	2026	2026-06-05 10:55:17.365307	25.00	0.00	2026-06-05	2
\.


--
-- Data for Name: meters; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.meters (id, created_at, installation_date, meter_number, status, type, updated_at, customer_id) FROM stdin;
1	2026-06-05 10:44:05.993536	2024-01-15	WTR-001-2024	ACTIVE	WATER	2026-06-05 10:44:05.993536	1
2	2026-06-05 10:44:06.025455	2024-02-01	ELC-001-2024	ACTIVE	ELECTRICITY	2026-06-05 10:44:06.025455	1
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.notifications (id, message, month, month_year, sent_at, year, customer_id) FROM stdin;
1	Dear Jean Uwimana, Your June/2026 utility bill of 4305.00 FRW has been successfully processed.	6	06-2026	2026-06-05 10:55:17.567989	2026	1
2	Dear Jean Uwimana, Your June/2026 utility bill of 4305.00 FRW has been fully paid. Thank you.	6	06-2026	2026-06-05 10:55:18.152782	2026	1
\.


--
-- Data for Name: otps; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.otps (id, attempts, code, created_at, email, expires_at, purpose, verified) FROM stdin;
\.


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.payments (id, amount, created_at, notes, status, updated_at, bill_id) FROM stdin;
1	2152.50	2026-06-05 10:55:17.992524	smoke partial	APPROVED	2026-06-05 10:55:18.040021	1
2	2152.50	2026-06-05 10:55:18.094312	smoke remainder	APPROVED	2026-06-05 10:55:18.154446	1
\.


--
-- Data for Name: penalties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.penalties (id, created_at, effective_from, name, percentage) FROM stdin;
1	2026-06-05 10:44:06.51123	2024-01-01	Late Payment Penalty	5.00
\.


--
-- Data for Name: tariff_tiers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tariff_tiers (id, from_units, rate_per_unit, to_units, tariff_id) FROM stdin;
1	0.00	280.00	10.00	3
2	10.00	420.00	\N	3
\.


--
-- Data for Name: tariffs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tariffs (id, created_at, effective_from, flat_rate, meter_type, name, tariff_type, updated_at, version) FROM stdin;
1	2026-06-05 10:44:06.471308	2024-01-01	350.00	WATER	Water Flat Rate v1	FLAT	2026-06-05 10:44:06.471308	1
2	2026-06-05 10:44:06.487083	2024-01-01	120.00	ELECTRICITY	Electricity Flat Rate v1	FLAT	2026-06-05 10:44:06.487083	1
3	2026-06-05 10:44:06.491689	2025-01-01	\N	WATER	Water Tiered Rate v1	TIER	2026-06-05 10:44:06.491689	2
\.


--
-- Data for Name: taxes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.taxes (id, created_at, effective_from, name, percentage) FROM stdin;
1	2026-06-05 10:44:06.502865	2024-01-01	VAT	18.00
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, country_code, created_at, email, full_name, password, phone_number, role, status, updated_at, customer_id) FROM stdin;
2	+250	2026-06-05 10:44:06.296739	operator@wasac.rw	Field Operator	$2a$10$gD5ATm7UJC0uSk76L4/51eOnoMquDEFxCbwZUTn/Jn.Udmvl5PSPq	0781000002	ROLE_OPERATOR	ACTIVE	2026-06-05 10:44:06.296739	\N
3	+250	2026-06-05 10:44:06.375926	finance@wasac.rw	Finance Officer	$2a$10$nnCukGOtuT1jY7Zbfdkyuel4tlc8KWp4L5P4hn6h/zcjyEEvXaJhq	0781000003	ROLE_FINANCE	ACTIVE	2026-06-05 10:44:06.375926	\N
4	+250	2026-06-05 10:44:06.455471	customer@wasac.rw	Jean Uwimana	$2a$10$AJ9kAPzMgEg4nAAFPi45F.xIy2dbyUEuPekohcGvOTuuFOAz9aSSO	0781000004	ROLE_CUSTOMER	ACTIVE	2026-06-05 10:44:06.455471	1
5	+250	2026-06-05 10:51:58.594193	operator.smoke@wasac.rw	Smoke Operator	$2a$10$BPy8vD4H/e43yKOLq.2Qd.70xZX6x7YXj2x1GDtlCCKPQBPoc5Cqu	0781999901	ROLE_OPERATOR	ACTIVE	2026-06-05 10:51:58.594193	\N
1	+250	2026-06-05 10:44:06.217343	ruyangearnold@gmail.com	System Admin	$2a$10$J53kAAYtr/TbGxNtMpzz..vLUtV/YAmdxlv9wvD0VSDlgmtTVR9tK	0781000001	ROLE_ADMIN	ACTIVE	2026-06-05 10:44:06.217343	\N
\.


--
-- Name: bills_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.bills_id_seq', 1, true);


--
-- Name: customer_notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.customer_notifications_id_seq', 1, false);


--
-- Name: customers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.customers_id_seq', 1, true);


--
-- Name: fixed_charges_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.fixed_charges_id_seq', 1, true);


--
-- Name: meter_readings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.meter_readings_id_seq', 1, true);


--
-- Name: meters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.meters_id_seq', 2, true);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.notifications_id_seq', 2, true);


--
-- Name: otps_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.otps_id_seq', 5, true);


--
-- Name: payments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.payments_id_seq', 2, true);


--
-- Name: penalties_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.penalties_id_seq', 1, true);


--
-- Name: tariff_tiers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tariff_tiers_id_seq', 2, true);


--
-- Name: tariffs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tariffs_id_seq', 3, true);


--
-- Name: taxes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.taxes_id_seq', 1, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 5, true);


--
-- Name: bills bills_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT bills_pkey PRIMARY KEY (id);


--
-- Name: customer_notifications customer_notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_notifications
    ADD CONSTRAINT customer_notifications_pkey PRIMARY KEY (id);


--
-- Name: customers customers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (id);


--
-- Name: fixed_charges fixed_charges_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_charges
    ADD CONSTRAINT fixed_charges_pkey PRIMARY KEY (id);


--
-- Name: meter_readings meter_readings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meter_readings
    ADD CONSTRAINT meter_readings_pkey PRIMARY KEY (id);


--
-- Name: meters meters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meters
    ADD CONSTRAINT meters_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: otps otps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.otps
    ADD CONSTRAINT otps_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: penalties penalties_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penalties
    ADD CONSTRAINT penalties_pkey PRIMARY KEY (id);


--
-- Name: tariff_tiers tariff_tiers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tariff_tiers
    ADD CONSTRAINT tariff_tiers_pkey PRIMARY KEY (id);


--
-- Name: tariffs tariffs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tariffs
    ADD CONSTRAINT tariffs_pkey PRIMARY KEY (id);


--
-- Name: taxes taxes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.taxes
    ADD CONSTRAINT taxes_pkey PRIMARY KEY (id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: bills uk7mfkui2gvu5wb3elqk0ao7ta8; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT uk7mfkui2gvu5wb3elqk0ao7ta8 UNIQUE (reference);


--
-- Name: meter_readings ukdw8t581mbe52m1iu6b0oocxgy; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meter_readings
    ADD CONSTRAINT ukdw8t581mbe52m1iu6b0oocxgy UNIQUE (meter_id, billing_month, billing_year);


--
-- Name: customers uke9mc7sqi5m0vi278e2h2tmioe; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT uke9mc7sqi5m0vi278e2h2tmioe UNIQUE (national_id);


--
-- Name: meters ukk56j5m520o5me94hml3y3u772; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meters
    ADD CONSTRAINT ukk56j5m520o5me94hml3y3u772 UNIQUE (meter_number);


--
-- Name: bills ukm0x8u7tmggmrlchu9f6jdbjqr; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT ukm0x8u7tmggmrlchu9f6jdbjqr UNIQUE (meter_id, billing_month, billing_year);


--
-- Name: otps ukqoott6a73ha6gxjgr04s071b9; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.otps
    ADD CONSTRAINT ukqoott6a73ha6gxjgr04s071b9 UNIQUE (email, purpose);


--
-- Name: customers ukrfbvkrffamfql7cjmen8v976v; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT ukrfbvkrffamfql7cjmen8v976v UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: bills after_bill_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER after_bill_insert AFTER INSERT ON public.bills FOR EACH ROW EXECUTE FUNCTION public.send_bill_notification_trigger();


--
-- Name: bills after_bill_paid; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER after_bill_paid AFTER UPDATE ON public.bills FOR EACH ROW EXECUTE FUNCTION public.send_bill_paid_notification_trigger();


--
-- Name: notifications fk30dp6ycner3dgso3scgc9vghy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk30dp6ycner3dgso3scgc9vghy FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: customer_notifications fk3195hk1a47mqgstpj8jg696ot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_notifications
    ADD CONSTRAINT fk3195hk1a47mqgstpj8jg696ot FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: tariff_tiers fk90xk77nst2f05cmrod4d5q42h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tariff_tiers
    ADD CONSTRAINT fk90xk77nst2f05cmrod4d5q42h FOREIGN KEY (tariff_id) REFERENCES public.tariffs(id);


--
-- Name: payments fk9565r6579khpdjxnyla0l2ycd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fk9565r6579khpdjxnyla0l2ycd FOREIGN KEY (bill_id) REFERENCES public.bills(id);


--
-- Name: users fkchxdoybbydcaj5smgxe0qq5mk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkchxdoybbydcaj5smgxe0qq5mk FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: meters fkdgg79dhtsr0eumbce7ipw58lj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meters
    ADD CONSTRAINT fkdgg79dhtsr0eumbce7ipw58lj FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: bills fkfes5685l6y4urtsc0cq3cobo1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT fkfes5685l6y4urtsc0cq3cobo1 FOREIGN KEY (meter_id) REFERENCES public.meters(id);


--
-- Name: bills fkjktiv3utpgao93xx3homxrhuf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT fkjktiv3utpgao93xx3homxrhuf FOREIGN KEY (meter_reading_id) REFERENCES public.meter_readings(id);


--
-- Name: meter_readings fknalaulqjlf29g1dlukdeyg0g4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meter_readings
    ADD CONSTRAINT fknalaulqjlf29g1dlukdeyg0g4 FOREIGN KEY (meter_id) REFERENCES public.meters(id);


--
-- Name: bills fkoy9sc2dmxj2qwjeiiilf3yuxp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bills
    ADD CONSTRAINT fkoy9sc2dmxj2qwjeiiilf3yuxp FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- PostgreSQL database dump complete
--

\unrestrict rtLU1ynBfbdBh2tdA4aN9mlaCOjPc6c69iH21eteWvruavVO6260JiBSx1Y7iJC

