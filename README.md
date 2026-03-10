# Intelligent Underground Car Park Management System

Graduation project implementation with Spring Boot + Vue for a 1000-spot, single-floor garage.

## Delivered in this stage
- JWT authentication + RBAC (ADMIN / OWNER)
- Admin account/password management with request validation
- Security hardening for admin responses (no password hash leakage)
- Parking spot management (2D grid coordinates and pillar/elevator flags)
- Garage map element management (boundary, pillar, no-parking zone, entrances, elevator)
- Smart recommendation based on elevator distance + congestion + pillar penalty
- Route guidance path output from entrance to recommended spot
- Simulated traffic tick for demo (zone load + occupancy changes)
- Tiered billing
- Real payment gateway integration via Stripe PaymentIntent
- Payment status callback endpoint for gateway/webhook status sync
- Alerts: overtime(>24h), spot conflict, blacklist vehicle entry
- Operation logs for admin actions
- Scheduled cleanup for records older than one month
- Unified global API exception handling
- Unified API response envelope (`success`, `message`, `data`)
- Admin operational dashboard overview report API
- Swagger API docs
- MySQL schema script

## Run backend
```bash
cd backend
mvn spring-boot:run
```

Env vars:
- `STRIPE_SECRET_KEY`: required for `/api/driver/payment/{sessionId}`.

Default admin account:
- username: `admin`
- password: `Admin@123`

Swagger:
- http://localhost:8080/swagger-ui.html

## API quick flow
> Most endpoints now return `{ success, message, data }`.
1. `POST /api/auth/register-owner`
2. `POST /api/auth/login`
3. Owner uses:
   - `GET /api/driver/recommend`
   - `GET /api/driver/map-overview` (includes `routePath`)
   - `POST /api/driver/entry`
   - `POST /api/driver/exit/{plate}`
4. `POST /api/driver/payment/{sessionId}` to create real payment intent
5. `POST /api/driver/payment/webhook` to update payment status by payment intent id
6. Admin uses `/api/admin/*` endpoints for spots/accounts/logs/alerts
7. Admin can manage map geometry and simulation:
   - `GET/POST /api/admin/map-elements`
   - `POST /api/admin/simulate/tick`
8. Admin dashboard report:
   - `GET /api/admin/reports/overview`
9. Alert management:
   - `GET /api/admin/alerts?unresolvedOnly=true|false`
   - `POST /api/admin/alerts/{alertId}/resolve`

## Frontend
`frontend/src/main.js` provides:
- Owner map demo (recommended spot + simulated entry)
- Admin dashboard cards and recent 7-day revenue table

## Thesis material
- `docs/thesis_ppt_outline.md` provides a defense PPT outline template.
