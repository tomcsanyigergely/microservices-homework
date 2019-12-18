var express = require('express');
const request_sender = require('request');
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
  console.log('X-USERNAME is ' + username);
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
        request_sender.put({
          url: 'http://inventory:80/changes/' + request_id,
          json: true,
          body: {
            items: items
          }
        }, function(err, res, body) {
          console.log(body);
          if (res && (res.statusCode == 200 || res.statusCode == 201)) {
            // van elég tárgy
            dbo.collection('orders').updateOne(query, { $set: { price: body.price } }, function(err, res) {
              if (err) {
                db.close();
                response.status(503).send({success: false, error: 'Service unavailable'});
                return;
              }
              request_sender.put({
                url: 'http://account:80/transactions/' + request_id,
                headers: {'X-USERNAME': username},
                json: true,
                body: body.price
              }, function(err, res, body) {
                console.log(body);
                if (err) {
                  //// TODO:
                  return;
                }
                if (res && res.statusCode == 200 || res.statusCode == 201) {
                  dbo.collection('orders').updateOne(query, { $set: { state: 'completed' } }, function(err, res) {
                    if (err) {
                      //todo
                      return;
                    }
                    //SUCCESS!
                    response.status(200).send({success: true});
                  });
                } else {
                  // nincs elég pénz
                }
              });
            });
          } else {
            // nincs elég tárgy
          }
        });
      };

      if (res.length == 0) {
        var order = { request_id: request_id, state: 'pending', username: username, items: items };
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
  MongoClient.connect(url, (err, db) => {
    if (err) {
      response.status(503).send({success: false, error: 'Service unavailable'});
      return;
    }

    var dbo = db.db('order');
    var projection = { _id: 0 };
    dbo.collection('orders').find({}, {projection: projection}).toArray(function(err, res) {
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
