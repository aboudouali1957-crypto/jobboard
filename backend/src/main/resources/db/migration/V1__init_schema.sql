-- =====================================================================
-- JobBoard : schéma initial
-- =====================================================================

CREATE TABLE companies (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    website     VARCHAR(255),
    city        VARCHAR(100),
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT pk_companies PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE users (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    email            VARCHAR(255) NOT NULL,
    password_hash    VARCHAR(100) NOT NULL,
    role             VARCHAR(20)  NOT NULL,
    first_name       VARCHAR(80)  NOT NULL,
    last_name        VARCHAR(80)  NOT NULL,
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    phone            VARCHAR(30),
    city             VARCHAR(100),
    headline         VARCHAR(150),
    bio              TEXT,
    cv_original_name VARCHAR(255),
    cv_storage_key   VARCHAR(100),
    cv_uploaded_at   DATETIME(6),
    company_id       BIGINT,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT chk_users_role CHECK (role IN ('CANDIDATE', 'RECRUITER', 'ADMIN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_users_role ON users (role);

CREATE TABLE skills (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(60) NOT NULL,
    CONSTRAINT pk_skills PRIMARY KEY (id),
    CONSTRAINT uk_skills_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Compétences d'un candidat (many-to-many users <-> skills)
CREATE TABLE user_skills (
    user_id  BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    CONSTRAINT pk_user_skills PRIMARY KEY (user_id, skill_id),
    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE offers (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    title         VARCHAR(150) NOT NULL,
    description   TEXT         NOT NULL,
    city          VARCHAR(100) NOT NULL,
    contract_type VARCHAR(20)  NOT NULL,
    salary_min    INT,
    salary_max    INT,
    remote        BOOLEAN      NOT NULL DEFAULT FALSE,
    status        VARCHAR(20)  NOT NULL,
    company_id    BIGINT       NOT NULL,
    recruiter_id  BIGINT       NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT pk_offers PRIMARY KEY (id),
    CONSTRAINT fk_offers_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_offers_recruiter FOREIGN KEY (recruiter_id) REFERENCES users (id),
    CONSTRAINT chk_offers_contract CHECK (contract_type IN ('CDI', 'CDD', 'FREELANCE', 'STAGE', 'ALTERNANCE')),
    CONSTRAINT chk_offers_status CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT chk_offers_salary CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_max >= salary_min)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_offers_status_created ON offers (status, created_at);
CREATE INDEX idx_offers_city ON offers (city);
CREATE INDEX idx_offers_contract_type ON offers (contract_type);
CREATE INDEX idx_offers_recruiter ON offers (recruiter_id);

-- Compétences demandées par une offre (many-to-many offers <-> skills)
CREATE TABLE offer_skills (
    offer_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    CONSTRAINT pk_offer_skills PRIMARY KEY (offer_id, skill_id),
    CONSTRAINT fk_offer_skills_offer FOREIGN KEY (offer_id) REFERENCES offers (id) ON DELETE CASCADE,
    CONSTRAINT fk_offer_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE applications (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    offer_id          BIGINT      NOT NULL,
    candidate_id      BIGINT      NOT NULL,
    status            VARCHAR(20) NOT NULL,
    cover_letter      TEXT,
    recruiter_message VARCHAR(1000),
    created_at        DATETIME(6) NOT NULL,
    updated_at        DATETIME(6) NOT NULL,
    CONSTRAINT pk_applications PRIMARY KEY (id),
    CONSTRAINT uk_applications_offer_candidate UNIQUE (offer_id, candidate_id),
    CONSTRAINT fk_applications_offer FOREIGN KEY (offer_id) REFERENCES offers (id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_applications_status CHECK (status IN ('RECEIVED', 'INTERVIEW', 'ACCEPTED', 'REJECTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_applications_candidate ON applications (candidate_id);
CREATE INDEX idx_applications_offer_status ON applications (offer_id, status);
CREATE INDEX idx_applications_created ON applications (created_at);
