from flask import Flask, jsonify, request
import mysql.connector
import time
import json

app = Flask(__name__)



if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)
