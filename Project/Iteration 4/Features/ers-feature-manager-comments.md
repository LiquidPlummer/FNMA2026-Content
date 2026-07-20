# ERS Feature — Manager Comments (S, 1 point)

## Summary

When denying a reimbursement request, a manager may attach a comment explaining the decision, and the request's author can view it. The feature closes an information gap in the existing workflow: a denial currently carries no explanation, leaving the employee to guess at the reason.

## Workflow

1. A manager views the pending requests and elects to deny one.
2. The interface presents an optional comment field alongside the deny action.
3. The manager enters a comment (or leaves it empty) and confirms the denial.
4. The request is resolved to `DENIED`; the comment is stored with the resolution.
5. The author views the denied request and sees the manager's comment alongside the resolution.

## Requirements

### Functional Requirements

1. When denying a `PENDING` request, a manager may attach a comment; the comment is optional
2. A denial without a comment behaves exactly as before
3. Approvals do not carry comments; a comment supplied on an approval is rejected with a validation error
4. The comment is visible to the request's author and to managers wherever the request is retrievable
5. The comment is set only at the moment of denial; it cannot be added, edited, or removed afterward
6. The comment is validated: non-blank if present, bounded length (255 characters)

### Non-Functional Requirements

1. Comment input is validated with Bean Validation; violations produce meaningful error responses
2. Unit tests cover the new service-layer behavior (deny with comment, deny without comment, comment on approval rejected)

## User Stories

- As a manager, I can attach a comment when denying a request
- As a manager, I can deny a request without a comment
- As an employee, I can view the manager's comment on my denied request

## Technical Notes

- Schema: a nullable `comment` column on the reimbursement table is sufficient; a separate table is not warranted at this scope
- Backend: extend the existing resolve endpoint's request body with an optional comment field; enforce the denial-only rule in the service layer; include the comment in the reimbursement response DTO
- Frontend: the manager's deny action gains a comment input (e.g., a textarea in a confirmation dialog); the employee's list or detail view displays the comment on denied requests

## Definition of Done

- [ ] A manager denying a request may attach a comment; the comment is returned when the author fetches the request
- [ ] A manager can deny a request without a comment; the response contains no comment
- [ ] Supplying a comment on an approval is rejected with a `400` and a meaningful message
- [ ] No endpoint permits adding or changing a comment after resolution
- [ ] A comment exceeding the length bound or consisting only of whitespace is rejected with a `400`
- [ ] The frontend deny flow offers a comment input; denied requests display the comment to their author
- [ ] Unit tests for the new service-layer behavior pass
- [ ] Existing approve/deny functionality is unchanged; no regressions
