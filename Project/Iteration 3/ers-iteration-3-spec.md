# ERS — Iteration 3 Specification

## Overview

This document specifies Iteration 3 of the ERS. It defines the iteration's objectives, the functional and non-functional requirements, the user stories the application must satisfy, and the deliverables to be submitted.

Iteration 3 replaces the minimal frontend with a complete client application built in Angular and TypeScript. The Spring Boot backend from Iteration 2 is carried forward largely unchanged; work in this iteration concentrates on the client. The result of this iteration is a finished product: a complete, polished ERS running locally.

Iteration 4 will treat this finished product as an inherited codebase and extend it with new features.

> **Note:** All backend requirements from Iteration 2 remain in effect. They are omitted from this document because they are unchanged; this specification covers only the frontend.

## Objectives

- Build a complete frontend in Angular and TypeScript, replacing the minimal frontend entirely
- Consume the existing REST API, including token-based authentication
- Provide distinct views and navigation appropriate to each role
- Deliver a polished, usable interface: this iteration produces the finished product

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

## Deliverables

- Codebase in a GitHub repository
- Live demonstration of the application
- Frontend architecture summary (components, services, routing): a brief written summary or a detailed diagram