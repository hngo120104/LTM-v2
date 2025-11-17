CREATE DATABASE word_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE word_game;

-- Bảng users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    total_score INT DEFAULT 0,
    total_wins INT DEFAULT 0
);

create table match_history (
	id char(36) primary key,
    session_id char(36) not null,
    gamemode enum('GAME_MODE_1', 'GAME_MODE_2') not null,
    winner_id int null,
    loser_id int null,
    words_id int null,
    
    winner_scores int null,
    loser_scores int null,
    
	start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME NULL,
    
    -- FOREIGN KEY (words_id) REFERENCES match_words (id) ON DELETE CASCADE,
	FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (loser_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE match_words (
	id INT AUTO_INCREMENT PRIMARY KEY,
    match_id char(36) NOT NULL,
    player_id INT NOT NULL,
    word NVARCHAR(50) NOT NULL,
    score INT DEFAULT 1,
    FOREIGN KEY (match_id) REFERENCES match_history(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES users(id) ON DELETE CASCADE
);