CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(50) NOT NULL,
  balance INT NOT NULL
);

CREATE TABLE transactions (
  id SERIAL PRIMARY KEY,
  amount INT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  user_id INT REFERENCES users(id)
);

INSERT INTO users (username, password, balance) VALUES ('tagergo', 'password', 80), ('gergely', 'secretpass', 20);
