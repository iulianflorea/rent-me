-- RentIt Database Schema
-- V1: Initial schema

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    role            ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    kyc_status      ENUM('NONE','PENDING','VERIFIED','REJECTED') NOT NULL DEFAULT 'NONE',
    preferred_language VARCHAR(5) NOT NULL DEFAULT 'ro',
    preferred_theme ENUM('LIGHT','DARK','SYSTEM') NOT NULL DEFAULT 'SYSTEM',
    stripe_account_id VARCHAR(255),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    gdpr_signed     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  DATETIME NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_prt_token (token),
    INDEX idx_prt_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kyc_verifications (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,
    selfie_url          VARCHAR(500),
    id_front_url        VARCHAR(500),
    id_back_url         VARCHAR(500),
    id_series           VARCHAR(10),
    id_number           VARCHAR(20),
    cnp                 VARCHAR(13),
    birth_date          DATE,
    id_expiry_date      DATE,
    status              ENUM('PENDING','VERIFIED','REJECTED') NOT NULL DEFAULT 'PENDING',
    rejection_reason    TEXT,
    reviewed_by         BIGINT,
    reviewed_at         DATETIME,
    submitted_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_kyc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gdpr_agreements (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    signed_at       DATETIME NOT NULL,
    ip_address      VARCHAR(45) NOT NULL,
    pdf_url         VARCHAR(500),
    version         VARCHAR(20) NOT NULL DEFAULT '1.0',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_gdpr_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS listings (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id                BIGINT NOT NULL,
    title                   VARCHAR(255) NOT NULL,
    description             TEXT NOT NULL,
    category                ENUM('TOOLS','VEHICLES','REAL_ESTATE','ELECTRONICS','SPORTS','OTHER') NOT NULL,
    status                  ENUM('DRAFT','ACTIVE','RENTED','INACTIVE') NOT NULL DEFAULT 'DRAFT',
    price_per_day           DECIMAL(10,2) NOT NULL,
    price_per_week          DECIMAL(10,2),
    price_per_month         DECIMAL(10,2),
    address                 VARCHAR(500),
    city                    VARCHAR(100),
    county                  VARCHAR(100),
    latitude                DOUBLE,
    longitude               DOUBLE,
    category_attributes     JSON,
    views_count             INT NOT NULL DEFAULT 0,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_listings_owner (owner_id),
    INDEX idx_listings_category (category),
    INDEX idx_listings_status (status),
    INDEX idx_listings_city (city),
    INDEX idx_listings_geo (latitude, longitude),
    FULLTEXT INDEX ft_listings_search (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS listing_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id  BIGINT NOT NULL,
    url         VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    INDEX idx_li_listing (listing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS rentals (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id          BIGINT NOT NULL,
    tenant_id           BIGINT NOT NULL,
    owner_id            BIGINT NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    total_days          INT NOT NULL,
    price_per_day       DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    guarantee_amount    DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(10,2) NOT NULL,
    status              ENUM('PENDING_PAYMENT','PAID','READY_TO_PICKUP','ACTIVE','RETURNED','CANCELLED','DISPUTED') NOT NULL DEFAULT 'PENDING_PAYMENT',
    qr_code_token       VARCHAR(255) UNIQUE,
    reference_number    VARCHAR(20) NOT NULL UNIQUE,
    cancellation_reason TEXT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (listing_id) REFERENCES listings(id),
    FOREIGN KEY (tenant_id) REFERENCES users(id),
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_rentals_listing (listing_id),
    INDEX idx_rentals_tenant (tenant_id),
    INDEX idx_rentals_owner (owner_id),
    INDEX idx_rentals_status (status),
    INDEX idx_rentals_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_id               BIGINT NOT NULL UNIQUE,
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_charge_id        VARCHAR(255),
    amount                  DECIMAL(10,2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'RON',
    status                  ENUM('PENDING','HELD','RELEASED','REFUNDED','FAILED') NOT NULL DEFAULT 'PENDING',
    platform_fee            DECIMAL(10,2),
    stripe_fee              DECIMAL(10,2),
    owner_net_amount        DECIMAL(10,2),
    guarantee_held          DECIMAL(10,2) NOT NULL DEFAULT 0,
    guarantee_released_at   DATETIME,
    paid_at                 DATETIME,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (rental_id) REFERENCES rentals(id),
    INDEX idx_payments_rental (rental_id),
    INDEX idx_payments_stripe (stripe_payment_intent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wishlist_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    listing_id  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_wishlist (user_id, listing_id),
    INDEX idx_wishlist_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS saved_owners (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    owner_id    BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_saved_owner (user_id, owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_rooms (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_id   BIGINT UNIQUE,
    participant1_id BIGINT NOT NULL,
    participant2_id BIGINT NOT NULL,
    last_message_at DATETIME,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rental_id) REFERENCES rentals(id),
    FOREIGN KEY (participant1_id) REFERENCES users(id),
    FOREIGN KEY (participant2_id) REFERENCES users(id),
    INDEX idx_chat_rooms_p1 (participant1_id),
    INDEX idx_chat_rooms_p2 (participant2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_messages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id     BIGINT NOT NULL,
    sender_id   BIGINT NOT NULL,
    content     TEXT,
    file_url    VARCHAR(500),
    message_type ENUM('TEXT','IMAGE','ATTACHMENT','SYSTEM') NOT NULL DEFAULT 'TEXT',
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_cm_room (room_id),
    INDEX idx_cm_sender (sender_id),
    INDEX idx_cm_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_id   BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewed_id BIGINT NOT NULL,
    rating      TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rental_id) REFERENCES rentals(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (reviewed_id) REFERENCES users(id),
    UNIQUE KEY uk_review (rental_id, reviewer_id),
    INDEX idx_reviews_reviewed (reviewed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(255) NOT NULL,
    message     TEXT NOT NULL,
    type        VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS smtp_config (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    host                VARCHAR(255) NOT NULL,
    port                INT NOT NULL DEFAULT 587,
    security            ENUM('NONE','STARTTLS','SSL') NOT NULL DEFAULT 'STARTTLS',
    username            VARCHAR(255) NOT NULL,
    encrypted_password  VARCHAR(500) NOT NULL,
    display_name        VARCHAR(100) NOT NULL DEFAULT 'RentIt',
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
