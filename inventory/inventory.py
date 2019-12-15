from flask import Flask, jsonify, request
import mysql.connector
import time

app = Flask(__name__)

def create_connection():
    connection = mysql.connector.connect(host = "inventorydb", port = 3306, user = "root", passwd = "microservices", db = "inventory")
    connection.autocommit = False
    return connection

def not_processed(request_id, cursor):
    cursor.execute("SELECT COUNT(*) FROM changes WHERE id = %s", (request_id,))
    (count,) = cursor.fetchone()
    return count == 0

def decrease_item_quantities(items, cursor):
    cursor.execute("LOCK TABLES items WRITE")
    time.sleep(5)
    for item in items:
      # lazy solution: executing SQL statements one by one in loop is not recommended:
      cursor.execute("SELECT quantity FROM items WHERE id = %s", (item['id'],))
      (current_quantity,) = cursor.fetchone()
      if current_quantity >= item['quantity']:
        cursor.execute("UPDATE items SET quantity = quantity - %s WHERE id = %s", (item['quantity'], item['id'],))
      else:
        return False
    cursor.execute("UNLOCK TABLES")
    return True

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
        if not_processed(body['request_id'], cursor):
          time.sleep(5)
          try:
            insert_change_sql = ("INSERT INTO changes (id) VALUES (%s)")
            cursor.execute(insert_change_sql, (body['request_id'],))

            insert_item_sql = ("INSERT INTO items (name, price, quantity) VALUES (%s, %s, %s)")
            cursor.execute(insert_item_sql, (body['name'], body['price'], body['quantity'],))

            connection.commit()

            response = {'success': True}
            status = 200
          except mysql.connector.IntegrityError:
            response = {'success': False, 'error': 'Already processed'}
            status = 409
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

@app.route("/changes", methods=['POST'])
def consume_quantity():
    if request.is_json:
      body = request.get_json()
      valid_body = True
      if body['request_id']:
        for item in body['items']:
          if not (item['id'] and item['quantity']):
            valid_body = False
      else:
        valid_body = False
      if valid_body:
        connection = create_connection()
        cursor = connection.cursor()
        if not_processed(body['request_id'], cursor):
          time.sleep(3)
          try:
            insert_change_sql = ("INSERT INTO changes (id) VALUES (%s)")
            cursor.execute(insert_change_sql, (body['request_id'],))

            decreased = decrease_item_quantities(body['items'], cursor)
            if decreased:
              connection.commit()

              response = {'success': True}
              status = 200
            else:
              response = {'success': False, 'error': 'Not enough items'}
              status = 409
          except mysql.connector.IntegrityError:
            response = {'success': False, 'error': 'Already processed'}
            status = 409
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
