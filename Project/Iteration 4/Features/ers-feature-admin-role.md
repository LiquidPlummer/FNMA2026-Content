# ERS Feature — Admin Role (L, 3 points)

## Summary

A third role, `ADMIN`, with the ability to change the role of any other user. Role assignment currently has no in-application mechanism — every registered user is an `EMPLOYEE` or `MANAGER`, and promotion requires manual database edits. This feature moves role administration into the system.

## Workflow

1. An admin logs in and navigates to a user management view, visible only to admins.
2. The view lists all users: username, name, role, department.
3. The admin selects a user and assigns a new role (`EMPLOYEE`, `MANAGER`, or `ADMIN`).
4. The change is persisted and reflected in the list. The affected user receives the capabilities of the new role from their next authentication onward.

## Requirements

### Functional Requirements

1. The system defines a third role: `ADMIN`
2. Admins can view a list of all users, showing at least username, first name, last name, role, and department
3. Admins can change the role of any other user to `EMPLOYEE`, `MANAGER`, or `ADMIN`
4. An admin cannot change their own role; the attempt is rejected
5. All user-administration endpoints reject non-admin callers with `403`
6. Registration continues to create users with the default role `EMPLOYEE`; there is no self-service path to `ADMIN`
7. At least one admin account exists in a freshly initialized system (seed data), so the feature is usable without database edits
8. An admin retains the employee capabilities all users share (submitting and managing their own reimbursements); the admin role does not grant manager resolution capabilities
9. A role change takes effect no later than the affected user's next login

### Non-Functional Requirements

1. Role enforcement happens on the backend; hiding frontend navigation is not sufficient
2. The user list returned to admins never includes passwords or password hashes
3. Unit tests cover the new service-layer behavior (role change, self-change rejected, non-admin rejected)

## User Stories

- As an admin, I can view all users and their roles
- As an admin, I can change the role of any other user
- As an admin, I cannot change my own role
- As an admin, I can submit and manage my own reimbursements
- As an employee or manager, I cannot access user administration

## Technical Notes

- Schema: extend the role representation to admit `ADMIN`; add a seed admin user to the initialization data
- Backend: a `GET` endpoint listing users and an endpoint updating a user's role, both restricted to `ADMIN`; extend the existing authorization mechanism (token role claim and endpoint guards) to recognize the third role. If the role is embedded in the bearer token, a changed role is not visible until a new token is issued — hence requirement 9's "next login" bound
- Frontend: an admin-only route with a guard, a users table with a role control per row (disabled on the admin's own row), and navigation that appears only for admins
- The department-management feature builds on this one; keeping the user list view extensible will save rework there

## Definition of Done

- [ ] A seeded admin can log in, view all users, and see roles and departments; no password material appears in the response
- [ ] The admin can change another user's role to each of the three roles; the change persists and the user has the new role's access after next login
- [ ] An admin's attempt to change their own role is rejected and the role is unchanged
- [ ] An employee or manager calling any admin endpoint receives `403`
- [ ] Registration still yields role `EMPLOYEE`
- [ ] The admin view is reachable only by admins; other roles see neither the navigation entry nor the route
- [ ] An admin can submit and manage their own reimbursements
- [ ] Unit tests for the new service-layer behavior pass
- [ ] No regressions in existing employee and manager authorization
