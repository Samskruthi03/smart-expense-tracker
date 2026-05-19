# \# Smart Expense Tracker

# 

# A production-grade personal finance microservices application built with Spring Boot, Kafka, PostgreSQL, and React.

# 

\## Architecture
┌─────────────────────────────────────────────────────────────┐
===

# │                        AWS eu-west-1                        │

# │                                                             │

# │  ┌──────────┐    ┌─────────────────────────────────────┐   │

# │  │CloudFront│    │           ECS Fargate               │   │

# │  │  + S3    │    │  ┌────────────────────────────────┐ │   │

# │  │ (React)  │───▶│  │       API Gateway :8085        │ │   │

# │  └──────────┘    │  └──────┬──────┬──────┬───────┘   │ │   │

# │                  │         │      │      │            │ │   │

# │                  │  ┌──────▼─┐ ┌──▼───┐ ┌▼────────┐ │ │   │

# │                  │  │  User  │ │Expens│ │Analytics│ │ │   │

# │                  │  │Service │ │  e   │ │ Service │ │ │   │

# │                  │  │ :8081  │ │:8082 │ │  :8083  │ │ │   │

# │                  │  └────┬───┘ └──┬───┘ └────┬────┘ │ │   │

# │                  └───────┼────────┼───────────┼──────┘ │   │

# │                          │        │           │        │   │

# │  ┌───────────────────────▼────────▼───────────▼──────┐ │   │

# │  │              Amazon MSK (Kafka)                   │ │   │

# │  │         Topics: expense.created, audit.log        │ │   │

# │  └───────────────────────────────────────────────────┘ │   │

# │                                                         │   │

# │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │

# │  │  RDS users   │  │  RDS expense │  │ RDS analytics│  │   │

# │  │  PostgreSQL  │  │  PostgreSQL  │  │  PostgreSQL  │  │   │

# │  └──────────────┘  └──────────────┘  └──────────────┘  │   │

# │                                                         │   │

# │  ┌──────────────────────────────────────────────────┐   │   │

# │  │              ElastiCache Redis                   │   │   │

# │  │         Idempotency keys + rate limiting         │   │   │

# │  └──────────────────────────────────────────────────┘   │   │

└─────────────────────────────────────────────────────────┘

## Tech Stack
===

# 

# | Layer | Technology |

# |-------|-----------|

# | Backend | Java 21, Spring Boot 3.3, Spring Security |

# | Messaging | Apache Kafka (AWS MSK) |

# | Database | PostgreSQL 16 (AWS RDS), Flyway migrations |

# | Cache | Redis (AWS ElastiCache) |

# | API Gateway | Spring Cloud Gateway |

# | Frontend | React 18, TypeScript, Vite, TanStack Query, Recharts |

# | CI/CD | GitHub Actions, Docker, GitHub Container Registry |

# | Infrastructure | AWS ECS Fargate, Terraform |

# | Auth | JWT (HMAC-SHA512) |

# 

# \## Key Engineering Decisions

# 

# \### Database-per-service

# Each microservice owns its schema and runs independent Flyway migrations. Services cannot directly query each other's databases — all cross-service communication goes through Kafka events or REST APIs. This enables independent deployments and clear ownership boundaries.

# 

# \### Kafka for async communication

# When an expense is created, `expense-service` publishes an `expense.created` event. Both `analytics-service` and `notification-service` consume it independently. Adding a new consumer (e.g. fraud detection) requires zero changes to the producer — open/closed principle at the infrastructure level.

# 

# \### Idempotency keys

# `POST /api/v1/expenses` accepts an `Idempotency-Key` header. The service stores the key in Redis with a 24-hour TTL. Duplicate submissions (network retries, double-clicks) return the original response without creating duplicate records. This is standard practice in payment APIs.

# 

# \### Cursor-based pagination

# Expense listing uses cursor-based pagination (`createdAt` timestamp as cursor) rather than offset pagination. This avoids the "missing rows" problem on high-write tables and performs consistently regardless of dataset size.

# 

# \### Manual Kafka acknowledgement

# The analytics and notification consumers use `AckMode.MANUAL` — offsets are only committed after successful processing. This prevents message loss on consumer crashes and makes reprocessing explicit.

# 

# \### JWT with forwarded headers

# The API Gateway validates JWTs and forwards `X-User-Id` and `X-User-Email` headers to downstream services. Downstream services trust these headers rather than re-parsing the token, keeping JWT logic centralised.

# 

# \## Services

# 

# | Service | Port | Responsibility |

# |---------|------|----------------|

# | api-gateway | 8085 | Routing, JWT validation, rate limiting |

# | user-service | 8081 | Registration, login, JWT issuance |

# | expense-service | 8082 | Expense CRUD, Kafka producer, idempotency |

# | analytics-service | 8083 | Kafka consumer, monthly aggregations |

# | notification-service | 8084 | Kafka consumer, email notifications |

# 

# \## API Endpoints

# 

\### Auth (user-service)

POST /api/v1/auth/register     Register new user
===

# POST /api/v1/auth/login        Login, returns JWT

# GET  /api/v1/auth/me           Get current user profile

# 

# \### Expenses (expense-service)

# POST   /api/v1/expenses              Create expense (Idempotency-Key header supported)

# GET    /api/v1/expenses              List expenses (cursor pagination, category/date filters)

# GET    /api/v1/expenses/{id}         Get single expense

# PATCH  /api/v1/expenses/{id}         Update expense

# DELETE /api/v1/expenses/{id}         Delete expense

# 

# \### Analytics (analytics-service)

# GET /api/v1/analytics/monthly/{year}/{month}   Monthly spending by category

# GET /api/v1/analytics/yearly/{year}            Year overview by month

# 

# \## Running Locally

# 

# \### Prerequisites

# \- Java 21

# \- Docker Desktop

# \- Maven 3.9+

# \- Node.js 20+

# 

# \### Start infrastructure

# ```bash

# docker compose up -d

# ```

# 

# This starts PostgreSQL (x3), Kafka, Redis, and Kafka UI (http://localhost:8090).

# 

# \### Start services

# Open a terminal for each:

# ```bash

# \# Terminal 1

# cd services/user-service \&\& mvn spring-boot:run

# 

# \# Terminal 2

# cd services/expense-service \&\& mvn spring-boot:run

# 

# \# Terminal 3

# cd services/analytics-service \&\& mvn spring-boot:run

# 

# \# Terminal 4

# cd services/notification-service \&\& mvn spring-boot:run

# 

# \# Terminal 5

# cd services/api-gateway \&\& mvn spring-boot:run

# ```

# 

# \### Start frontend

# ```bash

# cd frontend \&\& npm run dev

# ```

# 

# Open http://localhost:5173

# 

# \### Environment variables

# All services use sensible local defaults. For production, set:

# 

# | Variable | Description |

# |----------|-------------|

# | `DB\_URL` | PostgreSQL JDBC URL |

# | `DB\_USERNAME` | Database username |

# | `DB\_PASSWORD` | Database password |

# | `KAFKA\_BOOTSTRAP\_SERVERS` | Kafka broker addresses |

# | `REDIS\_HOST` | Redis hostname |

# | `JWT\_SECRET` | 256-bit secret key |

# 

# \## CI/CD Pipeline

# 

# Every push to `main` triggers:

# 

# 1\. \*\*Test\*\* — Maven test for all 4 backend services (parallel matrix)

# 2\. \*\*Build\*\* — Maven package, Docker image build

# 3\. \*\*Push\*\* — Docker images pushed to GitHub Container Registry

# 4\. \*\*Frontend\*\* — TypeScript compile + Vite production build

# 

# Images are tagged with both `latest` and the commit SHA for rollback capability.

# 

# \## Infrastructure (Terraform)

# 

# See \[`infra/terraform/`](infra/terraform/) for full AWS infrastructure as code.

# 

# Deploys to `eu-west-1` (Dublin) for GDPR data residency compliance.

# 

# \## Project Structure

# smart-expense-tracker/

# ├── .github/workflows/     # GitHub Actions CI/CD

# ├── services/

# │   ├── api-gateway/       # Spring Cloud Gateway

# │   ├── user-service/      # Auth + JWT

# │   ├── expense-service/   # Core CRUD + Kafka producer

# │   ├── analytics-service/ # Kafka consumer + aggregations

# │   └── notification-service/ # Kafka consumer + email

# ├── shared/

# │   └── events/            # Shared Kafka event DTOs

# ├── frontend/              # React + TypeScript

# ├── infra/

# │   └── terraform/         # AWS infrastructure as code

# └── docker-compose.yml     # Local development

# 

# \## Screenshots

# 

# \### Dashboard

# !\[Dashboard](docs/screenshots/dashboard.png)

# 

# \### Analytics

!\[Analytics](docs/screenshots/analytics.png)


===

