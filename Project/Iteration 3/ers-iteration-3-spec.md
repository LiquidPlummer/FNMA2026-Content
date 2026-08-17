# ERS — Iteration 3 Specification

## Overview

This document specifies Iteration 3 of the ERS. It defines the iteration's objectives, the functional and non-functional requirements, the user stories the application must satisfy, and the deliverables to be submitted.

Iteration 3 replaces the minimal frontend with a complete client application built in Angular and TypeScript. The Spring Boot backend from Iteration 2 is carried forward largely unchanged; work in this iteration concentrates on the client. The result of this iteration's core requirements is a finished product: a complete, polished ERS running locally.

This is now the project's final iteration, and it runs for the remainder of the project's time. Once the core requirements below are complete, remaining time goes toward the stretch goals: new features (paired with their backend halves from Iteration 2), Docker Compose containerization, and a refactor/polish pass — concluding with a final presentation of the complete system.

> **Note:** All backend requirements from Iteration 2 remain in effect. They are omitted from this document because they are unchanged; this specification covers only the frontend.

## Objectives

- Build a complete frontend in Angular and TypeScript, replacing the minimal frontend entirely
- Consume the existing REST API, including token-based authentication
- Provide distinct views and navigation appropriate to each role
- Deliver a polished, usable interface: this iteration produces the finished product
- As the final iteration, close out the project: pursue stretch-goal features, containerize the full stack, refactor and polish, and conclude with a presentation of the completed system

## Requirements

### Functional Requirements

1. The frontend provides registration and login forms
2. The frontend stores the bearer token after login and attaches it to all API requests
3. The frontend presents employees with views to submit, list, filter, and edit their reimbursements
4. The frontend presents managers with views to list, filter, and resolve reimbursements
5. Views and navigation reflect the logged-in user's role; users cannot navigate to functionality outside their role
6. The frontend redirects unauthenticated users to the login view
7. Users can log out through the interface; the client discards its token

### Non-Functional Requirements

1. The frontend is built with Angular and written in TypeScript
2. The frontend is organized into components and services; API access is confined to services
3. Client-side routing is handled by the Angular Router; protected routes use guards
4. Forms provide client-side validation with clear feedback before submission
5. API errors are surfaced to the user as understandable messages, not raw responses

### User Stories

- As a user, I can register an account through the interface
- As a user, I can log in through the interface and remain authenticated as I navigate
- As a user, I can log out through the interface
- As a user, I am redirected to the login view when not authenticated
- As an employee, I can submit a reimbursement request through a form
- As an employee, I can view and filter my requests in a list view
- As an employee, I can edit a pending request through the interface
- As an employee, I cannot see or navigate to manager functionality
- As a manager, I can view and filter all reimbursements in a list view
- As a manager, I can approve or deny requests from the interface
- As a manager, I can view a history of resolved requests
- As a manager, I can do everything an employee can do (submit and manage my own reimbursements)

## Stretch Goals

These are optional. Attempt them only after all core requirements are complete.

- As a user, I can navigate paginated lists of reimbursements from the interface
- As a user, I can sort list views by column
- As a user, I can toggle between light and dark themes

The features below were originally planned for a separate Iteration 4; that iteration has been folded into Iterations 2 and 3, with more time allotted to each. Each is scoped here to its **frontend slice**, pairing with the matching Iteration 2 backend stretch goal to complete the full feature described in its spec. Full requirements, technical notes, and definition of done for each are in [`../Features/`](../Features/).

| Feature (frontend slice) | Size | Points | Spec |
|---|---|---|---|
| Manager comments UI — comment input on deny, comment shown on denied requests | S | 1 | `ers-feature-manager-comments.md` |
| User profile page — a view/route for viewing and editing name and password | S | 1 | `ers-feature-user-profile-page.md` |
| Dashboard UI — the post-login landing view | M | 2 | `ers-feature-dashboard.md` |
| Saved reimbursements UI — draft save/edit/delete/submit actions in the employee's list | M | 2 | `ers-feature-saved-reimbursements.md` |
| Bulk actions UI — multi-select, confirmation, and outcome feedback in the manager's list | M | 2 | `ers-feature-bulk-actions.md` |
| Admin role UI — an admin-only user management view | L | 3 | `ers-feature-admin-role.md` |
| Department management UI — an admin-only department management view | L | 3 | `ers-feature-department-management.md` |
| Audit trail UI — a history section on the request detail view | L | 3 | `ers-feature-audit-trail.md` |
| Receipts & image uploads UI — upload, thumbnail, and full-size viewing flows | XL | 5 | `ers-feature-receipts-image-uploads.md` |

Also carried forward as stretch goals, previously Iteration 4 non-functional work:

- Containerize the full stack with Docker Compose (`docker compose up` runs database, backend, and frontend)
- A refactor/polish pass across the stack: address technical debt, inconsistencies, and usability issues

## Deliverables

- Codebase in a GitHub repository
- Live demonstration of the application, serving as the project's final presentation
- Frontend architecture summary (components, services, routing): a brief written summary or a detailed diagram
- Updated ERD reflecting the final schema, if any completed stretch goals changed it