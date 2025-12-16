# Mendel Java Code Challenge

## Overview

Build a RESTful web service that stores transactions **in memory** and returns information about them.

### Transaction Properties

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `transaction_id` | `long` | Yes | Unique identifier (path parameter) |
| `amount` | `double` | Yes | Transaction amount |
| `type` | `string` | Yes | Transaction type/category |
| `parent_id` | `long` | No | Reference to parent transaction |

### Key Features

1. **Store transactions** with type and amount
2. **Query by type** - return all transaction IDs for a given type
3. **Sum linked transactions** - calculate total amount for all transactions transitively connected via `parent_id`

---

## API Specification

### PUT `/transactions/{transaction_id}`

Creates or updates a transaction.

**Request Body:**
```json
{
  "amount": 5000.0,
  "type": "cars",
  "parent_id": 10
}
```

**Response (Success):**
```json
{ "status": "ok" }
```

---

### GET `/transactions/types/{type}`

Returns all transaction IDs for the specified type.

**Response:**
```json
[10, 11, 12]
```

---

### GET `/transactions/sum/{transaction_id}`

Returns the sum of all transactions transitively linked to the specified transaction (including itself).

**Response:**
```json
{ "sum": 20000.0 }
```

---

## Examples

```bash
# Create transaction 10 (no parent)
PUT /transactions/10
{ "amount": 5000, "type": "cars" }
# => { "status": "ok" }

# Create transaction 11 with parent 10
PUT /transactions/11
{ "amount": 10000, "type": "shopping", "parent_id": 10 }

# Create transaction 12 with parent 11
PUT /transactions/12
{ "amount": 5000, "type": "shopping", "parent_id": 11 }

# Query by type
GET /transactions/types/cars
# => [10]

GET /transactions/types/shopping
# => [11, 12]

# Sum calculations (includes self + all children transitively)
GET /transactions/sum/10
# => { "sum": 20000 }  // 5000 + 10000 + 5000

GET /transactions/sum/11
# => { "sum": 15000 }  // 10000 + 5000

GET /transactions/sum/12
# => { "sum": 5000 }   // 5000 (no children)
```

---

## Technical Requirements

| Requirement | Details |
|-------------|---------|
| Framework | Spring Boot (Java or Kotlin) |
| Java Version | 11 or higher |
| Database | **None** - in-memory storage only |
| Timeline | 3 consecutive days max |

---

## Evaluation Criteria

### Required

- [ ] Integration tests
- [ ] Dockerized application
- [ ] Code clarity
- [ ] Correct architecture design

### Bonus Points

- [ ] TDD approach
- [ ] Incremental development via commits
- [ ] SOLID principles
- [ ] Documentation

---

## Transaction Linking (Parent-Child Relationship)

```
Transaction 10 (cars, $5000)
    └── Transaction 11 (shopping, $10000)
            └── Transaction 12 (shopping, $5000)

sum(10) = 5000 + 10000 + 5000 = 20000
sum(11) = 10000 + 5000 = 15000
sum(12) = 5000
```

The sum is calculated **transitively**: it includes the transaction itself plus ALL descendants (children, grandchildren, etc.).
