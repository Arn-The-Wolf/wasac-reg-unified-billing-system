-- ================================================================
-- Task 6: Database Routines - Triggers for Bill Notifications
-- PostgreSQL - Run after application creates tables
-- Java layer sends HTML emails; triggers persist in-app notifications.
-- ================================================================

\c wasac_billing_db;

DROP TRIGGER IF EXISTS after_bill_insert ON bills;
DROP TRIGGER IF EXISTS after_bill_paid ON bills;
DROP FUNCTION IF EXISTS send_bill_notification_trigger();
DROP FUNCTION IF EXISTS send_bill_paid_notification_trigger();
DROP FUNCTION IF EXISTS send_bill_notification(BIGINT);
DROP FUNCTION IF EXISTS send_payment_notification(BIGINT);

CREATE OR REPLACE FUNCTION send_bill_notification(p_bill_id BIGINT)
RETURNS VOID AS $$
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

    v_month_year := v_month_name || '/' || v_year;

    v_message_text := 'Dear ' || v_customer_name ||
        ', Your ' || v_month_year ||
        ' utility bill of ' || TO_CHAR(v_total_amount, 'FM999999999.00') ||
        ' FRW has been successfully processed.';

    INSERT INTO customer_notifications (customer_id, message, billing_month, billing_year, month_year, sent_at)
    VALUES (v_customer_id, v_message_text, v_month, v_year, v_month_year, CURRENT_TIMESTAMP);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION send_payment_notification(p_bill_id BIGINT)
RETURNS VOID AS $$
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

    v_month_year := v_month_name || '/' || v_year;

    v_message_text := 'Dear ' || v_customer_name ||
        ', Your ' || v_month_year ||
        ' utility bill of ' || TO_CHAR(v_total_amount, 'FM999999999.00') ||
        ' FRW has been fully paid. Thank you for your payment.';

    INSERT INTO customer_notifications (customer_id, message, billing_month, billing_year, month_year, sent_at)
    VALUES (v_customer_id, v_message_text, v_month, v_year, v_month_year, CURRENT_TIMESTAMP);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION send_bill_notification_trigger()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM send_bill_notification(NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION send_bill_paid_notification_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status <> 'PAID' AND NEW.status = 'PAID' THEN
        PERFORM send_payment_notification(NEW.id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_bill_insert
AFTER INSERT ON bills
FOR EACH ROW
EXECUTE FUNCTION send_bill_notification_trigger();

CREATE TRIGGER after_bill_paid
AFTER UPDATE ON bills
FOR EACH ROW
EXECUTE FUNCTION send_bill_paid_notification_trigger();
