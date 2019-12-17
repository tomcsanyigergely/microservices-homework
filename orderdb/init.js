db.auth('user', 'microservices')

db = db.getSiblingDB('order')

db.createCollection('orders')
db.orders.createIndex({"request_id": 1}, {unique: true})
