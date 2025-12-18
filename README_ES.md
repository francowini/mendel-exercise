# Mendel Transaction Service

API REST para gestionar transacciones con relaciones padre-hijo y cálculo de sumas transitivas.

## Inicio Rápido

```bash
./gradlew bootRun
```

Corre en http://localhost:8080

### Con Docker

```bash
docker-compose up
```

## Endpoints de la API

| Método | Path | Descripción |
|--------|------|-------------|
| PUT | `/transactions/{id}` | Crear/actualizar transacción |
| GET | `/transactions/types/{type}` | Obtener IDs por tipo |
| GET | `/transactions/sum/{id}` | Obtener suma transitiva |

### PUT /transactions/{id}

Crea o actualiza una transacción.

**Request:**
```json
{
  "amount": 5000.0,
  "type": "cars",
  "parent_id": 10
}
```

- `amount` (requerido): Monto de la transacción
- `type` (requerido): Categoría/tipo
- `parent_id` (opcional): Enlace a transacción padre

**Response:**
```json
{ "status": "ok" }
```

### GET /transactions/types/{type}

Devuelve todos los IDs de transacciones del tipo especificado.

**Response:**
```json
[10, 11, 12]
```

### GET /transactions/sum/{id}

Calcula la suma de la transacción + todos sus descendientes (hijos, nietos, etc).

**Response:**
```json
{ "sum": 20000.0 }
```

**Ejemplo:**
```
Transacción 10 ($5000)
    └── Transacción 11 ($10000)
            └── Transacción 12 ($5000)

GET /sum/10 => 20000 (5000 + 10000 + 5000)
GET /sum/11 => 15000 (10000 + 5000)
GET /sum/12 => 5000
```

## Ejemplos de Uso

### Con curl

```bash
# Crear transacción 10
curl -X PUT http://localhost:8080/transactions/10 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000, "type": "cars"}'

# Crear transacción 11 con padre 10
curl -X PUT http://localhost:8080/transactions/11 \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "type": "shopping", "parent_id": 10}'

# Crear transacción 12 con padre 11
curl -X PUT http://localhost:8080/transactions/12 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000, "type": "shopping", "parent_id": 11}'

# Consultar por tipo
curl http://localhost:8080/transactions/types/cars
# => [10]

curl http://localhost:8080/transactions/types/shopping
# => [11, 12]

# Obtener suma transitiva
curl http://localhost:8080/transactions/sum/10
# => {"sum": 20000.0}
```

### Con Bruno

[Bruno](https://www.usebruno.com/) es un cliente API liviano (alternativa a Postman).

1. Instalar Bruno: `brew install bruno` (macOS) o descargar del sitio
2. Abrir Bruno → "Open Collection" → seleccionar carpeta `bruno/`
3. Ejecutar requests con el botón de play

Ya hay requests pre-configurados para todos los endpoints.

## Decisiones de Diseño

**Almacenamiento en memoria**: Usa `ConcurrentHashMap` para operaciones thread-safe sin base de datos. Búsquedas rápidas, sin persistencia.

**Índice padre-hijo**: Mantiene un índice de hijos (`parentId -> Set<childIds>`) para búsquedas O(1) en lugar de escanear todas las transacciones.

**Suma transitiva con BFS**: Usa búsqueda en anchura con un set de visitados para recorrer el árbol y prevenir ciclos.

## Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Tests de integración
./gradlew test --tests "*IT"
```

## Stack Técnico

- Java 17
- Spring Boot 3.2.5
- Gradle
