# Task Manager

A small task management app: a Spring Boot REST API, a React page, and MySQL, all
runnable with Docker Compose. It is a practice project for learning AI-assisted coding.

## Versions

- **v2 (current, `master` and `v2`):** adds register, login, logout with JWT and per-user tasks.
- **v1 (tag `v1.0`):** tasks API and React page, no login.

## Features

- Register, log in, and log out (JWT tokens, passwords hashed with Argon2id)
- Every user sees only their own tasks
- Create, list, edit, and delete tasks
- Task fields: title (required), description, status (`TODO`, `DOING`, `DONE`), due date, created time
- Filter the task list by status
- Input validation (a missing title returns 400) and 404 for unknown ids (or other users' tasks)
- Data is stored in MySQL and survives restarts

## Tech stack

| Part | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA, Maven |
| Frontend | React 19, Vite |
| Database | MySQL 8.4 |
| Run | Docker Compose (nginx serves the frontend and proxies `/api`) |

## Project layout

```
backend/             Spring Boot API (controller -> service -> repository, DTOs)
frontend/            React UI (Vite)
docker-compose.yml   mysql + backend + frontend
.env.example         names of the environment variables
```

## Setup (once)

Create a `.env` file in the project root from the template, then fill in the values:

```bash
cp .env.example .env
```

| Variable | Meaning |
| --- | --- |
| `DB_NAME` | database name |
| `DB_USER` | database user for the app |
| `DB_PASSWORD` | password for that user |
| `MYSQL_ROOT_PASSWORD` | MySQL root password |
| `MYSQL_HOST_PORT` | host port for MySQL (default `3307`) |
| `JWT_SECRET` | key that signs login tokens, at least 32 characters |
| `JWT_EXPIRATION_MINUTES` | optional token lifetime (default `60`) |

Generate a secret and put it in `.env`: `openssl rand -base64 48`

`.env` is git-ignored. Never commit real passwords.

## Run everything with Docker (recommended)

Needs Docker with Compose.

```bash
docker compose up --build
```

Then open <http://localhost:8081>. The API is also on <http://localhost:8080/api/tasks>.

Stop with `Ctrl+C`, or `docker compose down`. Your data stays in a Docker volume.
`docker compose down -v` deletes that volume and all tasks.

## Run for development (without Docker for the app)

Needs JDK 21, Maven, Node 22, and Docker (for MySQL only).

```bash
# 1. MySQL only (host port 3307)
docker compose up -d mysql

# 2. Backend (loads .env into the shell, then starts on :8080)
cd backend
set -a && . ../.env && set +a
mvn spring-boot:run

# 3. Frontend, in another terminal (http://localhost:5173, proxies /api to :8080)
cd frontend
npm install
npm run dev
```

## Authentication

1. `POST /api/auth/register` with `{"username","password"}` creates an account
   (username 3-50 letters, digits, `_`, `.`, `-`; password 8-128 characters).
2. `POST /api/auth/login` returns `{"token","username","expiresAt"}`.
3. Send `Authorization: Bearer <token>` on every `/api/tasks` call.
4. `POST /api/auth/logout` revokes the token on the server, so it stops working at once.

| Method | Path | Description | Success | Errors |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/register` | create an account | 201 | 400 invalid input, 409 username taken |
| `POST` | `/api/auth/login` | get a token | 200 | 401 wrong username or password |
| `POST` | `/api/auth/logout` | revoke the current token (needs a token) | 204 | 401 |

Tokens are stored by the React page in `localStorage`. Without a valid token, `/api/tasks`
returns 401.

## API

Base path: `/api/tasks`. All calls need a bearer token (see above).

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `POST` | `/api/tasks` | create a task | 201 |
| `GET` | `/api/tasks` | list tasks; optional `?status=TODO\|DOING\|DONE` | 200 |
| `GET` | `/api/tasks/{id}` | get one task | 200 (404 if unknown) |
| `PUT` | `/api/tasks/{id}` | replace title, description, due date; status is kept if omitted | 200 (404 if unknown) |
| `DELETE` | `/api/tasks/{id}` | delete a task | 204 (404 if unknown) |

Example:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"correct-horse"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"correct-horse"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')

curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Write README","status":"DOING","dueDate":"2026-12-01"}'

curl -H "Authorization: Bearer $TOKEN" 'http://localhost:8080/api/tasks?status=DOING'
```

## Tests and checks

```bash
cd backend && mvn -B test          # API and repository tests (in-memory H2, no MySQL needed)
cd frontend && npm run lint        # lint
cd frontend && npm run build       # production build
```

## Ports

| Service | Port |
| --- | --- |
| Frontend (Docker) | 8081 |
| Frontend (Vite dev) | 5173 |
| Backend | 8080 |
| MySQL (host) | 3307 |

If a port is taken, override it with `MYSQL_HOST_PORT`, `BACKEND_HOST_PORT`, or
`FRONTEND_HOST_PORT` in `.env`.

## Scope

Version 2 adds accounts and JWT login. Not included: password reset, email verification,
login rate limiting, refresh tokens. Tasks created in version 1 have no owner and are not
visible to any user. Treat all data as disposable.
