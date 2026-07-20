# ERS Feature — Dashboard (M, 2 points)

## Summary

A landing view presented immediately after login, summarizing the state of the system relevant to the user's role: pending counts and recent activity. It replaces the current post-login redirect to a list view, giving users an at-a-glance starting point instead of an unfiltered table.

## Workflow

1. A user logs in and lands on the dashboard.
2. An employee sees counts of their own requests by status (`PENDING`, `APPROVED`, `DENIED`) and a short list of their most recent activity — their latest submissions and resolutions.
3. A manager sees the count of pending requests across all employees and a short list of recent activity — the latest submissions and resolutions system-wide.
4. Each count and activity item links to the corresponding list or detail view (e.g., the pending count navigates to the pending-filtered list).
5. The dashboard remains reachable from the navigation at any time.

## Requirements

### Functional Requirements

1. The dashboard is the landing view after a successful login
2. The employee dashboard shows counts of the authenticated user's requests grouped by status
3. The employee dashboard shows the user's recent activity: their most recently submitted and most recently resolved requests (a bounded list, e.g., the 5 most recent)
4. The manager dashboard shows the count of `PENDING` requests across all employees
5. The manager dashboard shows recent activity system-wide: the most recently submitted and most recently resolved requests (a bounded list)
6. Since managers may also act as employees, the manager dashboard additionally shows the manager's own pending count
7. Counts and activity reflect the data at the time the view loads
8. Dashboard elements navigate to the relevant existing views (filtered lists, request detail)
9. Employees see no data belonging to other users on their dashboard

### Non-Functional Requirements

1. Dashboard data for a role is retrievable in a small, fixed number of API calls (a single summary endpoint is acceptable and preferred over the client assembling counts from full list responses)
2. The dashboard follows the established component/service structure; API access is confined to services

## User Stories

- As a user, I land on a dashboard when I log in
- As an employee, I can see how many of my requests are pending, approved, and denied
- As an employee, I can see my recent submissions and resolutions and navigate to them
- As a manager, I can see how many requests await resolution and navigate to the pending list
- As a manager, I can see recent activity across all employees

## Technical Notes

- Schema: no changes; all data derives from existing tables. Recency requires an ordering criterion — if the reimbursement table lacks timestamps, either add created/resolved timestamps or order by id, and state the choice
- Backend: a summary endpoint (or one per role) returning counts and recent items in a purpose-built DTO; derive scope (own vs. all) from the authenticated user's role; aggregate with repository queries (`COUNT` grouped by status, top-N ordered queries) rather than loading full lists
- Frontend: a dashboard component set as the post-login route and the default authenticated route; role-conditional sections; reuse existing navigation and route guards

## Definition of Done

- [ ] Logging in as an employee lands on a dashboard showing that user's counts by status and their recent activity; no other user's data appears
- [ ] Logging in as a manager lands on a dashboard showing the system-wide pending count, system-wide recent activity, and the manager's own pending count
- [ ] Counts match the underlying data (verifiable by submitting/resolving a request and reloading)
- [ ] The pending count navigates to the corresponding pending-filtered list view; activity items navigate to the relevant view
- [ ] Dashboard data is fetched through service(s), not assembled from full list responses in components
- [ ] Unit tests cover the new backend aggregation logic
- [ ] No regressions
