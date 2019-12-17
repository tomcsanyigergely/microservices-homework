CREATE TABLE accounts (
  username VARCHAR(30) PRIMARY KEY,
  balance INT NOT NULL
);

CREATE TABLE transactions (
  id VARCHAR(50) PRIMARY KEY,
  amount INT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  username VARCHAR(30) REFERENCES accounts(username)
);
