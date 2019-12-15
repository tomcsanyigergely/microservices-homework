from flask import Flask, jsonify, make_response
import mysql.connector
import time

app = Flask(__name__)

connection = None
while connection is None:
  time.sleep(1)
  try:
    connection = mysql.connector.connect(
      host="inventorydb",
      port=3306,
      user="root",
      passwd="microservices",
      db="inventory")
  except:
    print("Failed connecting to InventoryDB. Retrying...")
print("Successfully connected to InventoryDB.")

get_all_items_query = ("SELECT * FROM items")
get_item_query = ("SELECT * FROM items WHERE id = %s")

@app.route("/items", methods=['GET'])
def get_all_items():
    cursor = connection.cursor(dictionary=True)
    cursor.execute(get_all_items_query)
    output = []
    for row in cursor:
      output.append({'id': row['id'], 'name': row['name'], 'price': row['price'], 'quantity': row['quantity']})
    return jsonify({'items': output})

@app.route("/items/<int:id>", methods=['GET'])
def hello_world(id):
    cursor = connection.cursor(dictionary=True)
    cursor.execute(get_item_query, (id,))
    row = cursor.fetchone()
    cursor.close()
    if row is not None:
      return jsonify({'id': row['id'], 'name': row['name'], 'price': row['price'], 'quantity': row['quantity']})
    else:
      return jsonify({'error': 'Not found'}), 404

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8888)
