CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    publisher VARCHAR(255),
    isbn VARCHAR(20) UNIQUE,
    publication_date DATE,
    description TEXT
);