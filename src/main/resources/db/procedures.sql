CREATE OR REPLACE PROCEDURE update_user(
    IN p_user_id BIGINT,
    IN p_username VARCHAR,
    IN p_email VARCHAR,
    IN p_password VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE users
    SET username = p_username,
        email = p_email,
        password = p_password
    WHERE id = p_user_id;
END;
$$;
