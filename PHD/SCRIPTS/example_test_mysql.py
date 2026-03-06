import mysql.connector
from mysql.connector import Error

def test_mysql_connection():
    try:
        connection = mysql.connector.connect(
            host='108.61.185.63',
            port=3306,
            database='awaredb',
            user='awaredbuser',
            password='Bowdo1904!!'
        )

        if connection.is_connected():
            print("Connected to MySQL database")

            cursor = connection.cursor()
            # Simple test insert
            insert_query = """
            INSERT INTO phenotyping_data (avg_daily_distance_travelled_km)
            VALUES (1.23)
            """
            cursor.execute(insert_query)
            connection.commit()
            print(f"{cursor.rowcount} row inserted.")

            # Query back
            cursor.execute("SELECT id, avg_daily_distance_travelled_km FROM phenotyping_data ORDER BY id DESC LIMIT 1")
            result = cursor.fetchone()
            print("Latest row:", result)

    except Error as e:
        print("Error while connecting or executing query", e)
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()
            print("MySQL connection closed")

if __name__ == "__main__":
    test_mysql_connection()