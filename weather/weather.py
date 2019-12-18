from flask import Flask, jsonify, request
import mysql.connector
import requests
import time
import json

app = Flask(__name__)

def create_connection():
    connection = mysql.connector.connect(host = "weatherdb", port = 3306, user = "root", passwd = "microservices", db = "weather")
    connection.autocommit = False
    return connection

@app.route("/forecasts", methods=['GET'])
def get_all_forecasts():
    username = request.headers.get('X-USERNAME')
    if request.args and 'username' in request.args and username == request.args['username']:
      connection = create_connection()
      cursor = connection.cursor(dictionary=True)
      cursor.execute("SELECT id, latitude, longitude, temperature, date FROM forecasts WHERE username = (%s)", (username,))
      output = []
      for row in cursor:
        output.append({'id': row['id'], 'latitude': row['latitude'], 'longitude': row['longitude'], 'temperature': row['temperature'], 'date': row['date']})
      cursor.close()
      connection.close()
      return jsonify({'success': True, 'forecasts': output})
    else:
      return jsonify({'success': False, 'error': 'Forbidden'}), 403

@app.route("/forecasts/<id>", methods=['GET'])
def get_forecast(id):
    username = request.headers.get('X-USERNAME')
    connection = create_connection()
    cursor = connection.cursor(dictionary=True)
    cursor.execute("SELECT id, username, latitude, longitude, temperature, date FROM forecasts WHERE id = %s", (id,))
    row = cursor.fetchone()
    cursor.close()
    connection.close()
    if row is not None:
      if username == row['username']:
        return jsonify({'success': True, 'forecast': {'id': row['id'], 'latitude': row['latitude'], 'longitude': row['longitude'], 'temperature': row['temperature'], 'date': row['date']}})
      else:
        return jsonify({'success': False, 'error': 'Forbidden'}), 403
    else:
      return jsonify({'success': False, 'error': 'Forbidden'}), 403

@app.route("/forecasts/<id>", methods=['PUT'])
def put_forecast(id):
    if request.is_json:
      body = request.get_json()
      if (body['latitude'] and body['longitude'] and body['days'] and
          body['latitude'] >= -90 and body['latitude'] <= 90 and
          body['longitude'] >= -180 and body['longitude'] <= 180 and
          body['days'] >= 0):
        username = request.headers.get('X-USERNAME')
        connection = create_connection()
        cursor = connection.cursor(dictionary=True)
        cursor.execute("SELECT id, username, latitude, longitude, temperature, date FROM forecasts WHERE id = %s", (id,))
        row = cursor.fetchone()
        if row is not None:
          response = {'success': False, 'error': 'Forbidden'}
          status = 403
        else:
          account_response = requests.put("http://account:80/transactions/" + id, json='5', headers={'X-USERNAME': username})
          if account_response.status_code == 201 or account_response.status_code == 403:
            payload = {'latitude': body['latitude'], 'longitude': body['longitude'], 'days': body['days']}
            forecast_response = requests.get("http://forecast:80/weather", json=payload)
            if forecast_response.status_code == 200:
              content = forecast_response.json()
              insert_forecast_sql = ("INSERT INTO forecasts (id, username, latitude, longitude, temperature, date) VALUES (%s, %s, %s, %s, %s, DATE_ADD(NOW(), INTERVAL %s DAY))")
              cursor.execute(insert_forecast_sql, (id, username, body['latitude'], body['longitude'], content['temperature'], body['days']))
              response = {'success': True}
              status = 201
            else:
              response = {'success': True}
              status = 202
          elif account_response.status_code == 409:
            response = account_response.json()
            status = 409
          else:
            response = {'success': False, 'error': 'Service unavailable'}
            status = 503
        cursor.close()
        connection.close()
        return jsonify(response), status
      else:
        return jsonify({'success': False, 'error': 'Bad request'}), 400
    else:
      return jsonify({'success': False, 'error': 'Bad request'}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)
