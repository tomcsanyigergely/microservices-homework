from flask import Flask, jsonify, request
import mysql.connector
import time

app = Flask(__name__)

def create_connection():
    connection = mysql.connector.connect(host = "inventorydb", port = 3306, user = "root", passwd = "microservices", db = "inventory")
    connection.autocommit = False
    return connection

@app.route("/items", methods=['GET'])
def get_all_items():
    connection = create_connection()
    cursor = connection.cursor(dictionary=True)
    cursor.execute("SELECT id, name, price, quantity FROM items")
    output = []
    for row in cursor:
      output.append({'id': row['id'], 'name': row['name'], 'price': row['price'], 'quantity': row['quantity']})
    cursor.close()
    connection.close()
    return jsonify({'success': True, 'items': output})

@app.route("/items/<int:id>", methods=['GET'])
def get_item(id):
    connection = create_connection()
    cursor = connection.cursor(dictionary=True)
    cursor.execute("SELECT id, name, price, quantity FROM items WHERE id = %s", (id,))
    row = cursor.fetchone()
    cursor.close()
    connection.close()
    if row is not None:
      return jsonify({'success': True, 'item': {'id': row['id'], 'name': row['name'], 'price': row['price'], 'quantity': row['quantity']}})
    else:
      return jsonify({'success': False, 'error': 'Not found'}), 404

@app.route("/items", methods=['POST'])
def create_item():
    if request.is_json:
      body = request.get_json()
      if body['request_id'] and body['name'] and body['price'] and body['quantity']:
        connection = create_connection()
        cursor = connection.cursor()
        cursor.execute("SELECT COUNT(*) FROM changes WHERE id = %s", (body['request_id'],))
        (count,) = cursor.fetchone();
        if count == 0:
          time.sleep(5)

          try:
            insert_item_sql = ("INSERT INTO items (name, price, quantity) VALUES (%s, %s, %s)")
            cursor.execute(insert_item_sql, (body['name'], body['price'], body['quantity'],))

            insert_change_sql = ("INSERT INTO changes (id) VALUES (%s)")
            cursor.execute(insert_change_sql, (body['request_id'],))

            connection.commit()
          except mysql.connector.IntegrityError:
            response = {'success': False, 'error': 'Already processed'}
            status = 409
          else:
            response = {'success': True}
            status = 200
        else:
          response = {'success': False, 'error': 'Already processed'}
          status = 409
        cursor.close()
        connection.close()
      else:
        response = {'success': False, 'error': 'Invalid JSON body'}
        status = 400
    else:
      response = {'success': False, 'error': 'Missing JSON body'}
      status = 400
    return jsonify(response), status

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8888)
