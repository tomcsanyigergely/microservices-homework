var express = require('express');
var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://user:microservices@orderdb:27017/";

var app = express();
bodyParser = require('body-parser');
app.use(bodyParser.json());

app.listen(80, () => {
 console.log("Server started listening on port 80");
});

app.post("/orders", (request, response) => {
  if (!(typeof request.body !== 'undefined' && request.body !== null)) {
    if (!(typeof request.body !== 'undefined')) {
      console.log('undefined');
    }
    if (!(request.body !== null)) {
      console.log('null');
    }
    response.status(400).send({success: false, error: 'Bad request'});

    return;
  }
  var username = request.header('X-USERNAME');
  var request_id = request.body.request_id;
  var items = request.body.items;
  if (!(typeof username !== 'undefined' && username !== null &&
      typeof request_id !== 'undefined' && request_id !== null &&
      typeof items !== 'undefined' && items !== null)) {
        console.log('There is an errror');
    response.status(400).send({success: false, error: 'Bad request'});
    return;
  }

  MongoClient.connect(url, (err, db) => {
    if (err) {
      response.status(503).send({success: false, error: 'Service unavailable'});
      return;
    }

    var dbo = db.db('order');
    var myobj = { request_id: request_id, username: username, items: items };
    dbo.collection('orders').insertOne(myobj, function(err, res) {
      if (err) {
        response.status(409).send({success: false, error: 'Already processed'});
        db.close();
        return;
      }
      console.log("1 document inserted");
      response.send({success: true});
      db.close();
    });
  });
});

app.get("/orders", (request, response) => {
  var username = request.header('X-USERNAME');
  if (!(typeof username !== 'undefined' && username !== null)) {
    response.status(400).send({success: false, error: 'Bad request'});
    return;
  }

  MongoClient.connect(url, (err, db) => {
    if (err) {
      response.status(503).send({success: false, error: 'Service unavailable'});
      return;
    }

    var dbo = db.db('order');
    var query = { username: username };
    var projection = { request_id: 1, items: 1, _id: 0 };
    dbo.collection('orders').find(query, {projection: projection}).toArray(function(err, res) {
      if (err) {
        response.status(503).send({success: false, error: 'Service unavailable'});
        db.close();
        return;
      }
      response.send({success: true, orders: res});
      db.close();
    });
  });
});
