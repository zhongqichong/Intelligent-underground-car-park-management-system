# Thesis & Defense PPT Outline (Underground Garage Intelligent Management)

## 1. Project Background
- Urban parking pressure and underground garage pain points.
- Why smart guidance + billing + alerts are needed.

## 2. Requirements and Objectives
- Scope: 1000 spots, 4 gates, single floor.
- Roles: owner / admin.
- Core goals: guidance, tiered billing + payment gateway, anomaly alerts.

## 3. System Architecture
- Frontend: Vue web console.
- Backend: Spring Boot service.
- Database: MySQL.
- Security: JWT + RBAC.

## 4. Data Model Design
- Key tables: users, parking_spots, map_elements, parking_sessions, alerts, operation_logs.
- Relationship highlights and retention strategy (1 month cleanup).

## 5. Core Functional Design
- Vehicle entry/exit workflow.
- Recommendation scoring strategy.
- Payment flow with Stripe PaymentIntent and webhook sync.
- Alert rules: overtime, spot conflict, blacklist entry.

## 6. Key Implementation Details
- Global exception handling and validation strategy.
- Scheduled maintenance cleanup.
- Admin operation logging (AOP).
- Map simulation for no-hardware demo.

## 7. Demo Walkthrough
- Login and role access.
- Owner map + recommended spot + simulated entry.
- Admin dashboard metrics + map management + simulation tick.

## 8. Testing and Results
- Unit tests for pricing/admin/report/payment guard.
- Functional endpoint test checklist.
- Performance expectation (20 concurrent entry/exit).

## 9. Conclusions and Future Work
- Current delivered capability.
- Future enhancements: pathfinding, stronger analytics, hardware integration.
