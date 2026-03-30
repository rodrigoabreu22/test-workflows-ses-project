# Backend (Spring Boot)

## Run with Docker (backend + MySQL)

From repository root:

```bash
docker compose up --build
```

Backend: `http://localhost:8080`

## API endpoints

### Pizzerias

- `POST /api/pizzerias`
- `GET /api/pizzerias`
- `GET /api/pizzerias/{id}`
- `PUT /api/pizzerias/{id}`
- `DELETE /api/pizzerias/{id}`

### Pizzas

- `POST /api/pizzas`
- `GET /api/pizzas`
- `GET /api/pizzas/{id}`
- `PUT /api/pizzas/{id}`
- `DELETE /api/pizzas/{id}`

Optional filter:

- `GET /api/pizzas?pizzeriaId={id}`
