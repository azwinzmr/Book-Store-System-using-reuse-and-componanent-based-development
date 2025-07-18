-- Table Creation

CREATE TABLE users (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE genres (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    genre_name VARCHAR(50) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE books (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    genre_id BIGINT NOT NULL,
    stock_quantity INT DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE orders (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    user_id BIGINT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Data Insertion

-- Insert sample genres (only once)
INSERT INTO genres (genre_name) VALUES 
('Fiction'),
('Non-Fiction'),
('Science & Technology'),
('Business & Finance'),
('Health & Wellness');

-- Insert sample users (only once)
INSERT INTO users (username, password, email, role) VALUES 
('admin', 'admin123', 'admin@bookstore.com', 'ADMIN'),
('user1', 'user123', 'user1@email.com', 'USER');

-- Insert sample books (combined all unique books)
-- Ensure genre_id matches the genres table IDs that will be generated (1-5 for the inserted genres)
INSERT INTO books (title, author, price, genre_id, stock_quantity) VALUES 
('The Great Adventure', 'John Smith', 25.99, 1, 10), -- Fiction
('Learning Java', 'Jane Doe', 45.50, 3, 15),       -- Science & Technology
('Business Strategy', 'Mike Johnson', 35.75, 4, 8), -- Business & Finance
('Healthy Living Guide', 'Sarah Wilson', 22.00, 5, 12), -- Health & Wellness
('History of Technology', 'Robert Brown', 28.90, 2, 6), -- Non-Fiction
('Mystery Novel', 'Alice Cooper', 19.99, 1, 8),     -- Fiction
('Programming Fundamentals', 'Bob Wilson', 52.00, 3, 20), -- Science & Technology
('Marketing Essentials', 'Carol Davis', 41.25, 4, 7), -- Business & Finance
('Fitness Guide', 'David Lee', 24.50, 5, 15),       -- Health & Wellness
('World History', 'Emma Stone', 33.75, 2, 9);       -- Non-Fiction
