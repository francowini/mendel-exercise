# Mendel Transaction Service

REST API for managing hierarchical transactions with parent-child relationships and transitive sum calculations.

## Quick Start

```bash
./gradlew bootRun
```

Runs on http://localhost:8080

### With Docker

```bash
docker-compose up
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| PUT | `/transactions/{id}` | Create/update a transaction |
| GET | `/transactions/types/{type}` | Get transaction IDs by type |
| GET | `/transactions/sum/{id}` | Get transitive sum |

### PUT /transactions/{id}

Create or update a transaction.

**Request:**
```json
{
  "amount": 5000.0,
  "type": "cars",
  "parent_id": 10
}
```

- `amount` (required): Transaction amount
- `type` (required): Category/type
- `parent_id` (optional): Link to parent transaction

**Response:**
```json
{ "status": "ok" }
```

### GET /transactions/types/{type}

Returns all transaction IDs matching the type.

**Response:**
```json
[10, 11, 12]
```

### GET /transactions/sum/{id}

Calculates the sum of transaction + all its descendants (children, grandchildren, etc).

**Response:**
```json
{ "sum": 20000.0 }
```

**Example:**
```
Transaction 10 ($5000)
    └── Transaction 11 ($10000)
            └── Transaction 12 ($5000)

GET /sum/10 => 20000 (5000 + 10000 + 5000)
GET /sum/11 => 15000 (10000 + 5000)
GET /sum/12 => 5000
```

## Usage Examples

### With curl

```bash
# Create transaction 10
curl -X PUT http://localhost:8080/transactions/10 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000, "type": "cars"}'

# Create transaction 11 with parent 10
curl -X PUT http://localhost:8080/transactions/11 \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "type": "shopping", "parent_id": 10}'

# Create transaction 12 with parent 11
curl -X PUT http://localhost:8080/transactions/12 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000, "type": "shopping", "parent_id": 11}'

# Get by type
curl http://localhost:8080/transactions/types/cars
# => [10]

curl http://localhost:8080/transactions/types/shopping
# => [11, 12]

# Get transitive sum
curl http://localhost:8080/transactions/sum/10
# => {"sum": 20000.0}
```

### With Bruno

[Bruno](https://www.usebruno.com/) is a lightweight API client (alternative to Postman).

1. Install Bruno: `brew install bruno` (macOS) or download from website
2. Open Bruno → "Open Collection" → select the `bruno/` folder
3. Run requests with the play button

Pre-configured requests are included for all endpoints.

## Design Decisions

**In-memory storage**: Uses `ConcurrentHashMap` for thread-safe operations without a database. Fast lookups, no persistence needed.

**Parent-child indexing**: Maintains a children index (`parentId -> Set<childIds>`) for O(1) child lookups instead of scanning all transactions.

**Transitive sum with BFS**: Uses breadth-first search with a visited set to traverse the transaction tree and prevent cycles.

## Running Tests

```bash
# All tests
./gradlew test

# Integration tests
./gradlew test --tests "*IT"
```

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Gradle
