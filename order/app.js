var express = require("express");
var app = express();

app.listen(80, () => {
 console.log("Server started listening on port 80");
});

app.get("/hello", (req, res) => {
 res.send({success: true});
});
