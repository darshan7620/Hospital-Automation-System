CREATE TABLE patient_number_sequence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE patients (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    patient_number VARCHAR(20)  NOT NULL,
    full_name      VARCHAR(150) NOT NULL,
    date_of_birth  DATE         NOT NULL,
    phone          VARCHAR(30)  NOT NULL,
    address        VARCHAR(255) NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_patients_patient_number UNIQUE (patient_number)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_patients_full_name ON patients (full_name);
CREATE INDEX idx_patients_phone ON patients (phone);
CREATE INDEX idx_patients_active ON patients (active);
