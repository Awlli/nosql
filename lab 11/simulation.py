import random
import hashlib
from collections import Counter

# Фиксируем seed для воспроизводимости результатов
random.seed(42)

# Исходные условия: 12 000 документов, 30 000 запросов, 3 шарда
N = 12000
Q = 30000
SHARDS = 3

# Шаг 1. Генерация синтетических данных (Вариант 4)
# courierId - уникальный ID курьера (высокая кардинальность)
# zoneId - зона доставки от 0 до 4 (низкая кардинальность)
# event_id - монотонный счетчик событий от 0 до 12000
rows = [
    dict(courierId=i % 2500, zoneId=i % 5, event_id=i) 
    for i in range(N)
]

def stable_hash(value):
    """Хеширование для имитации Hashed Sharding"""
    digest = hashlib.sha256(str(value).encode()).digest()
    return int.from_bytes(digest[:8], 'big')

# --- Стратегии распределения по шардам (возвращают номер шарда: 0, 1 или 2) ---

def low_card_strategy(row):
    """Стратегия A: Низкокардинальный ключ zoneId"""
    return row['zoneId'] % SHARDS

def hashed_strategy(row):
    """Стратегия B: Хешированный ключ courierId"""
    return stable_hash(row['courierId']) % SHARDS

def range_strategy(row):
    """Стратегия C: Диапазонный ключ event_id"""
    return min(row['event_id'] // (N // SHARDS), SHARDS - 1)

# --- Генерация профилей нагрузки (Запросы) ---

# 1. Равномерная нагрузка (Uniform)
uniform_queries = random.choices(rows, k=Q)

# 2. Неравномерная нагрузка (Hot Shard) - 60% запросов идут строго в zoneId == 0
hot_rows = [r for r in rows if r['zoneId'] == 0]
hot_queries = [
    random.choice(hot_rows) if random.random() < 0.6 else random.choice(rows)
    for _ in range(Q)
]

def analyze_and_print(name, assign_fn, queries_dataset):
    """Функция подсчета метрик распределения документов и запросов"""
    # Считаем документы на каждом шарде
    docs_counter = Counter(assign_fn(r) for r in rows)
    # Считаем запросы на каждом шарде
    queries_counter = Counter(assign_fn(q) for q in queries_dataset)
    
    d_list = [docs_counter[i] for i in range(SHARDS)]
    q_list = [queries_counter[i] for i in range(SHARDS)]
    
    # Расчет Max/Mean
    mean_q = sum(q_list) / SHARDS
    max_mean_q = round(max(q_list) / mean_q, 2)
    
    print(f"[{name}]")
    print(f"  Распределение документов (Shard 0/1/2): {d_list}")
    print(f"  Распределение запросов   (Shard 0/1/2): {q_list}")
    print(f"  Коэффициент Max/Mean (по запросам): {max_mean_q}")
    print("-" * 50)

# --- Запуск симуляции ---
print(" РЕЗУЛЬТАТЫ СИМУЛЯЦИИ (ВАРИАНТ 4: СЛУЖБА ДОСТАВКИ)\n" + "="*50)

analyze_and_print("A) zoneId / Uniform", low_card_strategy, uniform_queries)
analyze_and_print("A) zoneId / Hot", low_card_strategy, hot_queries)

analyze_and_print("B) hashed_courier / Uniform", hashed_strategy, uniform_queries)
analyze_and_print("B) hashed_courier / Hot", hashed_strategy, hot_queries)

analyze_and_print("C) range_event / Uniform", range_strategy, uniform_queries)
analyze_and_print("C) range_event / Hot", range_strategy, hot_queries)