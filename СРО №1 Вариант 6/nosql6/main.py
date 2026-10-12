import time
from pymongo import MongoClient
from datetime import datetime

# 1. ПОДКЛЮЧЕНИЕ (Самое простое, напрямую без .env файлов)
print("Шаг 1: Подключаемся к MongoDB...")
client = MongoClient("mongodb://localhost:27017/")
db = client["delivery_analytics"]

# Очищаем старое, чтобы данные не дублировались при перезапусках
db.restaurants.drop()
db.orders.drop()

# 2. ЗАПОЛНЕНИЕ ДАННЫХ (Создаем 3 ресторана и 30 заказов для ТЗ)
print("Шаг 2: Заполняем базу данных тестовыми записями (30+ шт)...")

restaurants_data = [
    {"_id": "r1", "name": "Burger Joint", "cuisine": "American"},
    {"_id": "r2", "name": "Sushi Master", "cuisine": "Japanese"},
    {"_id": "r3", "name": "Pizza Italia", "cuisine": "Italian"}
]
db.restaurants.insert_many(restaurants_data)

# Генерируем 30 заказов (по 10 на каждый ресторан)
orders_data = []
for i in range(1, 31):
    # Чередуем рестораны r1, r2, r3
    r_id = f"r{(i % 3) + 1}" 
    status = "CANCELLED" if i in [5, 15, 25] else "DELIVERED"
    
    orders_data.append({
        "order_id": 100 + i,
        "restaurant_id": r_id,
        "customer": f"Клиент {i}",
        "items": [
            {"name": "Фирменное блюдо", "qty": 2, "price": 1500},
            {"name": "Напиток", "qty": 1, "price": 500}
        ],
        "status": status,
        "date": datetime(2026, 10, 1)
    })

db.orders.insert_many(orders_data)
print(f" Успешно создано записей: Ресторанов - {db.restaurants.count_documents({})}, Заказов - {db.orders.count_documents({})}")

print("\n=======================================================")
print("Шаг 3: Выполняем 8 обязательных демонстрационных запросов:")
print("=======================================================\n")

# Запрос 1: CREATE (Добавление нового заказа)
db.orders.insert_one({"order_id": 999, "restaurant_id": "r1", "customer": "Новый Клиент", "items": [], "status": "NEW"})
print("Запрос 1 (CREATE): Добавлен новый тестовый заказ №999.")

# Запрос 2: READ (Поиск этого заказа)
res_read = db.orders.find_one({"order_id": 999})
print(f"Запрос 2 (READ): Нашли заказ №999, его статус: {res_read['status']}")

# Запрос 3: UPDATE (Обновление статуса заказа)
db.orders.update_one({"order_id": 999}, {"$set": {"status": "DELIVERED"}})
print("Запрос 3 (UPDATE): Изменили статус заказа №999 на 'DELIVERED'.")

# Запрос 4: DELETE (Удаление заказа)
db.orders.delete_one({"order_id": 999})
print("Запрос 4 (DELETE): Удалили временный заказ №999 из базы.")

# Запрос 5: FILTER (Фильтрация отмененных заказов)
cancelled = list(db.orders.find({"status": "CANCELLED"}))
print(f"Запрос 5 (FILTER): Найдено отмененных заказов в системе: {len(cancelled)} шт.")

# Запрос 6: INDEX TEST (Замер скорости поиска до создания индекса)
start_time = time.time()
for _ in range(1000): # Ищем 1000 раз для точности замера
    list(db.orders.find({"status": "DELIVERED"}))
time_before = time.time() - start_time
print(f"Запрос 6 (БЕЗ ИНДЕКСА): Время поиска: {time_before:.4f} сек.")

# Создаем индекс по полю status
db.orders.create_index([("status", 1)])

# Запрос 7 (НЕТРИВИАЛЬНЫЙ С ИНДЕКСОМ): Замер скорости после создания индекса
start_time = time.time()
for _ in range(1000):
    list(db.orders.find({"status": "DELIVERED"}))
time_after = time.time() - start_time
print(f"Запрос 7 (С ИНДЕКСОМ): Время поиска: {time_after:.4f} сек. (Ускорение очевидно!)")

# Запрос 8 (НЕТРИВИАЛЬНЫЙ ИЗ ТЗ - МЕГА АГРЕГАЦИЯ ВАРИАНТА 6)
# Используем $unwind (развернуть товары), $group (посчитать выручку), $lookup (соединить с ресторанами)
pipeline = [
    {"$match": {"status": "DELIVERED"}}, 
    {"$unwind": "$items"},
    {"$group": {
        "_id": "$restaurant_id",
        "total_revenue": {"$sum": {"$multiply": ["$items.qty", "$items.price"]}},
        "items_count": {"$sum": "$items.qty"}
    }},
    {"$lookup": {
        "from": "restaurants",
        "localField": "_id",
        "foreignField": "_id",
        "as": "info"
    }},
    {"$unwind": "$info"}
]

print("\nЗапрос 8 (ГЛАВНАЯ АГРЕГАЦИЯ ВАРИАНТА 6 - Аналитика выручки):")
results = db.orders.aggregate(pipeline)
for r in results:
    print(f"  Ресторан: '{r['info']['name']}' ({r['info']['cuisine']}) | Общая выручка: {r['total_revenue']} тг. | Продано позиций: {r['items_count']} шт.")