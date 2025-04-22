from flask import Flask, request, jsonify, send_file
from flask_restx import Api, Resource
import pymysql
import paho.mqtt.publish as publish
import json
import os
import hashlib

app = Flask(__name__)
api = Api(app)

UPLOAD_FOLDER = os.path.join(os.getcwd(), "uploads")
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER


def get_mysql_connection():
    connection = pymysql.connect(
        host="",
        user="",
        password="",
        db="",
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )
    return connection


@api.route("/raw_data")
class RawData(Resource):
    def get(self):
        connection = get_mysql_connection()
        try:
            with connection.cursor() as cursor:
                sql_result = "SELECT * FROM result_data"
                cursor.execute(sql_result)
                result_data = cursor.fetchall()

                sql_raw_data = "SELECT Locationx Locationy FROM raw_data"
                cursor.execute(sql_raw_data)
                raw_data = cursor.fetchall()

                sql_overpoint_data = "SELECT * FROM overpoint_data"
                cursor.execute(sql_overpoint_data)
                overpoint_data = cursor.fetchall()

                result = {
                    "result_data": result_data,
                    "raw_data": raw_data,
                    "overpoint_data": overpoint_data,
                }
                return jsonify(result)
        finally:
            connection.close()


@app.route("/upload", methods=["POST"])
def upload():
    print("here")
    if "file" not in request.files:
        return "No file part"

    print("herer")
    file = request.files["file"]
    if file.filename == "":
        return "No selected file"

    file_path = os.path.join(app.config["UPLOAD_FOLDER"], file.filename)
    file.save(file_path)

    file_name = file.filename
    sha256 = hashlib.sha256()
    with open(file_path, "rb") as f:
        for block in iter(lambda: f.read(4096), b""):
            sha256.update(block)

    checksum = sha256.hexdigest()
    payload = {"file": file_name, "checksum": checksum}

    payload_str = json.dumps(payload)
    print(checksum)
    publish.single("ota/update_possible", payload_str, hostname="localhost", port=1883)
    publish.single("ota/update", payload_str, hostname="localhost", port=1883)

    return "Upload success"


@app.route("/files/<filename>", methods=["GET"])
def download(filename):
    file_path = os.path.join(app.config["UPLOAD_FOLDER"], filename)
    if os.path.exists(file_path):
        return send_file(file_path, as_attachment=True)
    else:
        return "File not found", 404


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
