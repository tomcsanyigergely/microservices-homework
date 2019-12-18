var express = require('express');

var app = express();
bodyParser = require('body-parser');
app.use(bodyParser.json());

app.listen(80, () => {
 console.log("Server started listening on port 80");
});

app.get("/weather", (request, response) => {
  console.log("GET /weather");

  if (!(typeof request.body !== 'undefined' && request.body !== null)) {
    response.status(400).send({success: false, error: 'Bad request'});
    return;
  }
  var longitude = request.body.longitude;
  var latitude = request.body.latitude;
  var days = request.body.days;
  if (!(typeof longitude !== 'undefined' && longitude !== null &&
        typeof latitude !== 'undefined' && latitude !== null &&
        typeof days !== 'undefined' && days !== null)) {
    response.status(400).send({success: false, error: 'Bad request'});
    return;
  }

  if (!(longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90 && days >= 0)) {
    response.status(400).send({success: false, error: 'Bad request'});
    return;
  }

  var temperature = Math.random() * (30 - (-10)) + (-10);
  response.status(200).send({success: true, temperature: temperature});
});
