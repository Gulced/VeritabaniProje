-- =====================================================
-- TRIGGER: Rezervasyon eklendiğinde Listing.updated_at güncellenir
-- Amaç: Yeni rezervasyon → ilan güncellendi olarak işaretlenir
-- =====================================================

-- 1. FUNCTION OLUŞTUR (Trigger'ın çalıştıracağı fonksiyon)
CREATE OR REPLACE FUNCTION update_listing_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE listings
    SET updated_at = NOW()
    WHERE id = NEW.listing_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 2. TRIGGER OLUŞTUR
-- Rezervasyon eklenince otomatik tetiklenir
-- =====================================================
CREATE TRIGGER reservation_after_insert
AFTER INSERT ON reservations
FOR EACH ROW
EXECUTE FUNCTION update_listing_timestamp();
