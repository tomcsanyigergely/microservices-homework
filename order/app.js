var express = require('express');
const request_sender = require('request');
const waitSync = require('wait-sync');
var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://user:microservices@orderdb:27017/";
const WAITING_TIME = Number(process.env.WAITING_TIME || '0')

var app = express();
bodyParser = require('body-parser');
app.use(bodyParser.json());

app.listen(80, () => {
 console.log("Server started listening on port 80");
});

app.put("/orders/:id", (request, response) => {
  console.log("PUT /orders/" + request.params.id)
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

  MongoClient.connect(url, (err, connection) => {
    if (err) {
      console.log(err);
      response.status(503).send({success: false, error: 'Service unavailable'});
      return;
    }

    var dbo = connection.db('order');
    var query = { request_id: request_id }
    var projection = { _id: 0, state: 1 }
    dbo.collection('orders').find(query, {projection: projection}).toArray(function(err, res) {
      if (err) {
        console.log(err);
        response.status(503).send({success: false, error: 'Service unavailable'});
        connection.close();
        return;
      }

      var make_order = function() {
        waitSync(WAITING_TIME);
        request_sender.put({
          url: 'http://inventory:80/changes/' + request_id,
          json: true,
          body: {
            items: items
          }
        }, function(err, res, body) {
          if (err) {
            console.log(err);
            connection.close();
            response.status(503).send({success: false, error: 'Service unavailable'});
            return;
          }
          console.log('INVENTORY SERVICE: ' + res.statusCode);
          console.log(body);
          if (res && (res.statusCode == 201 || res.statusCode == 403)) {
            dbo.collection('orders').updateOne(query, { $set: { price: body.price } }, function(err, res) {
              if (err) {
                console.log(err);
                connection.close();
                response.status(503).send({success: false, error: 'Service unavailable'});
                return;
              }
              waitSync(WAITING_TIME);
              request_sender.put({
                url: 'http://account:80/transactions/' + request_id,
                headers: {'X-USERNAME': username},
                json: true,
                body: body.price
              }, function(err, res, body) {
                if (err) {
                  console.log(err);
                  connection.close();
                  response.status(503).send({success: false, error: 'Service unavailable'});
                  return;
                }
                console.log('ACCOUNT SERVICE: ' + res.statusCode);
                console.log(body);
                if (res && res.statusCode == 403 || res.statusCode == 201) {
                  dbo.collection('orders').updateOne(query, { $set: { state: 'completed', timestamp: { type: Date, default: Date.now} } }, function(err, res) {
                    if (err) {
                      console.log(err);
                      connection.close();
                      response.status(503).send({success: false, error: 'Service unavailable'});
                      return;
                    }

                    //SUCCESSFUL TRANSACTION!!!
                    response.status(201).send({success: true});
                    connection.close();
                  });
                } else {
                  //NOT ENOUGH MONEY ON ACCOUNT OR ACCOUNT SERVICE UNAVAILABLE
                  if (res && res.statusCode == 409) {
                    response.status(409).send({success: false, error: body.error});
                  }
                  waitSync(WAITING_TIME);
                  request_sender.delete({
                    url: 'http://inventory:80/changes/' + request_id
                  }, function(err, res, body) {
                    if (err) {
                      console.log(err);
                      return;
                    }
                    console.log('INVENTORY SERVICE: ' + res.statusCode);
                    console.log(body);
                  });

                  dbo.collection('orders').deleteOne(query, function(err, db) {
                    if (err) {
                      console.log(err);
                    }
                    connection.close();
                  });
                }
              });
            });
          } else {
            // NOT ENOUGH ITEMS OR INVENTORY SERVICE UNAVAILABLE
            if (res && res.statusCode == 409) {
              response.status(409).send({success: false, error: body.error});
            } else {
              response.status(503).send({success: false, error: 'Service unavailable'});
            }

            dbo.collection('orders').deleteOne(query, function(err, db) {
              if (err) {
                console.log(err);
              }
              connection.close();
            });
          }
        });
      };

      if (res.length == 0) {
        var order = { request_id: request_id, state: 'pending', username: username, items: items, timestamp: { type: Date, default: Date.now} };
        dbo.collection('orders').insertOne(order, function(err, res) {
          if (err) {
            console.log(err);
            response.status(503).send({success: false, error: 'Service unavailable'});
            connection.close();
            return;
          }
          make_order();
        });
      } else {
        if (res[0].state == 'completed') {
          response.status(403).send({success: false, error: 'Already processed'});
          connection.close();
          return;
        } else {
          make_order();
        }
      }
    });
  });
});

app.get("/orders", (request, response) => {
  console.log("GET /orders")
  MongoClient.connect(url, (err, connection) => {
    if (err) {
      response.status(503).send({success: false, error: 'Service unavailable'});
      return;
    }

    var dbo = connection.db('order');
    var projection = { _id: 0 };
    dbo.collection('orders').find({}, {projection: projection}).toArray(function(err, res) {
      if (err) {
        response.status(503).send({success: false, error: 'Service unavailable'});
        connection.close();
        return;
      }
      response.send({success: true, orders: res});
      connection.close();
    });
  });
});
