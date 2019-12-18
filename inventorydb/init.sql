CREATE TABLE items (
  id INT AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  price INT NOT NULL,
  quantity INT NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE changes (
  id VARCHAR(50),
  price INT,

  PRIMARY KEY (id)
);

INSERT INTO items (name, price, quantity) VALUES ('Csoki', 3, 50), ('Alma', 4, 20), ('Sajt', 7, 11);
