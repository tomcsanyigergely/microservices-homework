CREATE TABLE forecasts (
  id INT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  latitude DOUBLE(2,10) NOT NULL,
  longitude DOUBLE(3, 10) NOT NULL,
  temperature DOUBLE(2, 10) NOT NULL,
  date TIMESTAMP NOT NULL,

  PRIMARY KEY (id)
);
