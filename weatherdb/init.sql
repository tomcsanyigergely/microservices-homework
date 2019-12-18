CREATE TABLE forecasts (
  id VARCHAR(50),
  username VARCHAR(50) NOT NULL,
  latitude DOUBLE(10,8) NOT NULL,
  longitude DOUBLE(10, 7) NOT NULL,
  temperature DOUBLE(10, 2) NOT NULL,
  date TIMESTAMP NOT NULL,

  PRIMARY KEY (id)
);
