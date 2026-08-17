# ERS Feature — Department Management (L, 3 points)

## Summary

Admins create and rename departments and reassign users between them. Departments are currently fixed at initialization; the organization cannot change shape without database edits. This feature presumes the `ADMIN` role (see the admin-role feature specification) and extends its administration surface.

## Workflow

1. An admin navigates to a department management view, visible only to admins.
2. The view lists all departments and the number of users in each.
3. The admin creates a new department by supplying a unique name; it appears in the list and becomes available wherever departments are used (e.g., the manager's department filter).
4. The admin renames an existing department; the new name appears everywhere the department is referenced, including on its users and their reimbursements.
5. The admin selects a user and reassigns them to a different department; the user's profile and all department-filtered views reflect the change.

## Requirements

### Functional Requirements

1. Admins can view all departments
2. Admins can create a department with a name that is unique among departments; a duplicate name is rejected with a meaningful error
3. Admins can rename a department, subject to the same uniqueness rule
4. Renaming a department is a change to the department itself: users assigned to it remain assigned, and the new name is reflected wherever the department appears
5. Admins can reassign any user to a different existing department
6. Departments cannot be deleted; deletion is out of scope
7. All department-administration endpoints reject non-admin callers with `403`
8. The manager's existing filter-by-department continues to work with created and renamed departments
9. Department names are validated: non-blank, bounded length

### Non-Functional Requirements

1. Uniqueness is enforced at the database level (unique constraint) in addition to service-layer validation
2. Unit tests cover the new service-layer behavior (create, duplicate rejected, rename, reassign, non-admin rejected)

## User Stories

- As an admin, I can view all departments
- As an admin, I can create a department
- As an admin, I can rename a department
- As an admin, I can move a user to a different department
- As an employee or manager, I cannot access department administration
- As a manager, I can filter reimbursements by a newly created or renamed department

## Technical Notes

- Schema: the department table exists; verify a unique constraint on `name` is present and add one if not. No structural change should be needed for reassignment — the user's department reference is updated in place
- Backend: CRUD-minus-delete endpoints for departments and an endpoint (or an extension of the admin user-update endpoint) for reassigning a user's department, all restricted to `ADMIN`; duplicate names surface as `400` or `409` with a clear message
- Frontend: a department management view under the admin area — department list with create and rename forms; user reassignment fits naturally into the admin user list from the admin-role feature (a department control per row). Sources of department options (registration form, manager filter) should read the live department list, not a hardcoded set

## Definition of Done

- [ ] An admin can create a department; it persists and appears in the department list and the manager's department filter
- [ ] Creating or renaming a department to an existing name is rejected with a meaningful error and no change occurs
- [ ] Renaming a department updates its name everywhere it is displayed; its users remain assigned to it
- [ ] An admin can reassign a user to another department; the user's profile and department-filtered manager views reflect the change
- [ ] No endpoint deletes a department
- [ ] An employee or manager calling any department-administration endpoint receives `403`
- [ ] Department administration views are reachable only by admins
- [ ] Unit tests for the new service-layer behavior pass
- [ ] No regressions, including the manager's department filter
