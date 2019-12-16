CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(30) UNIQUE NOT NULL,
  password VARCHAR(30) NOT NULL,
  balance INT NOT NULL
);

CREATE TABLE transactions (
  id VARCHAR(50) PRIMARY KEY,
  amount INT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  user_id INT REFERENCES users(id)
);

INSERT INTO users (id, username, password, balance) VALUES (1, 'tagergo', 'password', 80), (2, 'gergely', 'secretpass', 20);
