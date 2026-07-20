# ERS Feature — User Profile Page (S, 1 point)

## Summary

Users view their own profile and edit its user-serviceable parts: name and password. The system currently offers no way for a user to see their own account details or correct them after registration.

## Workflow

1. A logged-in user navigates to the profile view from the application's navigation.
2. The view displays the user's username, first name, last name, role, and department. Username, role, and department are read-only.
3. The user edits their first or last name and saves; the view reflects the change.
4. To change their password, the user enters their current password, a new password, and a confirmation, then saves.
5. On the next login, the new password is required; the old one is rejected.

## Requirements

### Functional Requirements

1. Any authenticated user can view their own profile: username, first name, last name, role, department
2. A user can update their own first name and last name
3. A user can change their own password by supplying their current password and a new password
4. A password change is rejected with `400` when the supplied current password is incorrect
5. Username, role, and department are not modifiable through this feature
6. A user can view and modify only their own profile; the profile endpoints operate on the authenticated identity, not on a client-supplied user id

### Non-Functional Requirements

1. The new password is subject to the same validation rules as registration and is stored hashed, never in plaintext
2. Input is validated with Bean Validation; the frontend form provides client-side validation (required fields, new-password confirmation match) before submission
3. Unit tests cover the new service-layer behavior (name update, password change, wrong current password)

## User Stories

- As a user, I can view my profile
- As a user, I can update my first and last name
- As a user, I can change my password
- As a user, I cannot change my username, role, or department

## Technical Notes

- Schema: no changes; all fields exist on the user table
- Backend: a `GET` endpoint returning the authenticated user's profile (password excluded) and update endpoint(s) for name and password; resolve the target user from the bearer token; verify the current password against the stored hash before accepting a new one
- Frontend: a profile component and route, guarded like other authenticated routes, reachable from the navigation for all roles; separate the name form from the password form to keep validation simple

## Definition of Done

- [ ] A logged-in user can fetch their profile; the response contains username, first name, last name, role, and department, and never the password or its hash
- [ ] Updating first/last name persists and is reflected on subsequent fetches
- [ ] A password change with the correct current password succeeds; the user can log in with the new password and cannot log in with the old one
- [ ] A password change with an incorrect current password is rejected with `400` and does not alter the stored credential
- [ ] Requests attempting to alter username, role, or department through the profile endpoints have no effect on those fields
- [ ] The profile view is reachable from the navigation for every role; forms validate client-side before submission
- [ ] Unit tests for the new service-layer behavior pass
- [ ] No regressions
