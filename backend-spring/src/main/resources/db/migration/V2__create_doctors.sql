CREATE TABLE doctors (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    full_name        VARCHAR(150)  NOT NULL,
    specialization   VARCHAR(100)  NOT NULL,
    phone            VARCHAR(30)   NULL,
    email            VARCHAR(150)  NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    department_id    BIGINT        NOT NULL,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       DATETIME(6)   NOT NULL,
    updated_at       DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_doctors_email UNIQUE (email),
    CONSTRAINT fk_doctors_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_doctors_department_id ON doctors (department_id);
CREATE INDEX idx_doctors_active ON doctors (active);
CREATE INDEX idx_doctors_specialization ON doctors (specialization);
CREATE INDEX idx_doctors_full_name ON doctors (full_name);
