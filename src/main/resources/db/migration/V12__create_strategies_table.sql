CREATE TABLE IF NOT EXISTS strategies (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          user_id BIGINT NOT NULL,
                                          name VARCHAR(255) NOT NULL,
    template_id VARCHAR(50),
    description TEXT,
    params_json JSON,
    active BOOLEAN DEFAULT FALSE,
    total_trades INT DEFAULT 0,
    winning_trades INT DEFAULT 0,
    total_pnl DECIMAL(20, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_active (active),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;