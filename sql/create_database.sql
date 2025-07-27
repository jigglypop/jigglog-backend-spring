-- Jigglog 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS jigglog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jigglog;

-- User 테이블
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    email VARCHAR(100),
    hashedPassword VARCHAR(255),
    imageUrl VARCHAR(255),
    githubUrl VARCHAR(255),
    summary TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Category 테이블
CREATE TABLE IF NOT EXISTS category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100),
    thumbnail VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Post 테이블
CREATE TABLE IF NOT EXISTS post (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    summary TEXT,
    content LONGTEXT,
    images TEXT,
    viewcount INT DEFAULT 0,
    site VARCHAR(100),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    userId INT,
    categoryId INT,
    FOREIGN KEY (userId) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (categoryId) REFERENCES category(id) ON DELETE SET NULL,
    INDEX idx_userId (userId),
    INDEX idx_categoryId (categoryId),
    INDEX idx_createdAt (createdAt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tag 테이블
CREATE TABLE IF NOT EXISTS tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Post_to_Tag 중간 테이블
CREATE TABLE IF NOT EXISTS post_to_tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postId INT NOT NULL,
    tagId INT NOT NULL,
    FOREIGN KEY (postId) REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY (tagId) REFERENCES tag(id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_tag (postId, tagId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment 테이블
CREATE TABLE IF NOT EXISTS comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    userId INT,
    postId INT,
    FOREIGN KEY (userId) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (postId) REFERENCES post(id) ON DELETE CASCADE,
    INDEX idx_postId (postId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ReComment (대댓글) 테이블
CREATE TABLE IF NOT EXISTS recomment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    userId INT,
    commentId INT,
    FOREIGN KEY (userId) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (commentId) REFERENCES comment(id) ON DELETE CASCADE,
    INDEX idx_commentId (commentId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Image_url 테이블
CREATE TABLE IF NOT EXISTS image_url (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    postId INT,
    FOREIGN KEY (postId) REFERENCES post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IconSet 테이블
CREATE TABLE IF NOT EXISTS icon_set (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100),
    postId INT,
    FOREIGN KEY (postId) REFERENCES post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Upload 테이블
CREATE TABLE IF NOT EXISTS upload (
    id INT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255),
    filepath VARCHAR(500),
    filesize BIGINT,
    mimetype VARCHAR(100),
    uploadedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 