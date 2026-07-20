# ERS — Iteration 2 Specification

## Overview

This document specifies Iteration 2 of the ERS. It defines the iteration's objectives, the functional and non-functional requirements, the user stories the application must satisfy, and the deliverables to be submitted.

Iteration 2 migrates the backend from Javalin to Spring Boot and introduces genuine authentication: clients authenticate to receive a bearer token, which must accompany all subsequent requests. Input validation and application logging — stretch goals in Iteration 1 — are now core requirements. Application functionality otherwise remains as specified in Iteration 1. The frontend is carried forward unchanged, adjusted only as needed to remain functional.

Later iterations will replace the frontend entirely and extend the system with new features.

## Objectives

- Migrate the REST API from Javalin to Spring Boot (Spring Web, Spring Data)
- Implement token-based authentication (bearer tokens)
- Enforce role-based authorization on all protected endpoints
- Preserve all Iteration 1 functionality through the migration

## Requirements

### Functional Requirements

1. Users can register an account (default role `EMPLOYEE`)
2. Every user has exactly one role: `EMPLOYEE` or `MANAGER`
4. Employees can submit a reimbursement request (amount, description, type)
5. Employees can view their own reimbursements, filterable by status
6. Employees can edit a reimbursement while it is `PENDING`
7. Managers can view all reimbursements, filterable by status and department
8. Managers can approve or deny `PENDING` reimbursements; the resolver is recorded
9. Managers may also submit and manage their own reimbursements, as an employee does
10. Resolved reimbursements cannot be modified
11. Users authenticate with username and password to receive a bearer token
12. Failed login attempts (bad credentials) are rejected with `401 Unauthorized`
13. All endpoints except registration and login require a valid bearer token
14. Requests with a missing, invalid, or expired token are rejected with `401 Unauthorized`
15. Requests to endpoints outside the user's role are rejected with `403 Forbidden`
16. Users can log out; the client discards its token

### Non-Functional Requirements

1. The application is built with Spring Boot; Javalin is removed
2. Persistence uses Spring Data JPA in place of raw JDBC; data survives application restart
3. The API follows REST conventions (proper methods, status codes, JSON)
4. Passwords are not stored in plaintext; they are hashed with an industry-standard algorithm (e.g., BCrypt)
5. Tokens are validated on every request; each request must be fully processable without prior server-side state
6. Layered architecture is preserved: controller / service / repository
7. Input is validated with Bean Validation (`@Valid`); violations produce meaningful error responses
8. Application events and errors are logged with SLF4J/Logback
9. Unit tests for the service layer are updated and passing

### User Stories

- As a user, I can register an account
- As a user, I can log in and receive a token that authenticates my subsequent requests
- As a user, I can log out
- As a user, I am denied access to the system when my token is missing or invalid
- As an employee, I can submit a reimbursement request
- As an employee, I can view the status of my requests
- As an employee, I can edit a pending request
- As an employee, I am denied access to manager functionality
- As a manager, I can view pending requests
- As a manager, I can approve or deny requests
- As a manager, I can view a history of resolved requests
- As a manager, I can do everything an employee can do (submit and manage my own reimbursements)

## Stretch Goals

These are optional. Attempt them only after all core requirements are complete.

- As a user, I can request paginated and sorted lists of reimbursements
- As a user, my token expires after a set period, and I can refresh it without re-entering credentials

## Deliverables

- Codebase in a GitHub repository
- Live demonstration of the application, including authentication
- Brief written summary of the migration (what changed and why)