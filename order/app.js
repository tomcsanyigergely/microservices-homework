var express = require('express');
const request = require('request');
var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://user:microservices@orderdb:27017/";


var app = express();
bodyParser = require('body-parser');
app.use(bodyParser.json());

app.listen(80, () => {
 console.log("Server started listening on port 80");
});

app.put("/orders/:id", (request, response) => {
  if (!(typeof request.body !== 'undefined' && request.body !== null)) {
    response.status(400).send({success: false, error: 'Bad request'});
    return;
  }
  var request_id = request.params.id;
  var username = request.header('X-USERNAME');
  var items = request.body.items;
  if (!(typeof username !== 'undefined' && username !== null &&
        typeof items !== 'undefined' && items !== null)) {
    response.status(400).send({success: false, error: 'Bad request'});
    return;
  }

  MongoClient.connect(url, (err, db) => {
    if (err) {
      response.status(503).send({success: false, error: 'Service unavailable'});
      return;
    }

    var dbo = db.db('order');
    var query = { request_id: request_id }
    var projection = { _id: 0, state: 1 }
    dbo.collection('orders').find(query, {projection: projection}).toArray(function(err, res) {
      if (err) {
        response.status(503).send({success: false, error: 'Service unavailable'});
        db.close();
        return;
      }

      var make_order = function() {
        request.put({
          headers: {'Content-Type' : 'application/json'},
          url: 'http://inventory:80/' + request_id,
          body: items}, function(error, response, body){
            console.log(body);
          });
      };

      if (res.length == 0) {
        var order = { request_id: request_id, state: 'started', username: username, items: items };
        dbo.collection('orders').insertOne(order, function(err, res) {
          if (err) {
            response.status(503).send({success: false, error: 'Service unavailable'});
            db.close();
            return;
          }
          make_order();
        });
      } else {
        if (res[0].state == 'completed') {
          response.status(200).send({success: true, error: 'Already completed'});
          db.close();
          return;
        } else {
          make_order();
        }
      }
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
