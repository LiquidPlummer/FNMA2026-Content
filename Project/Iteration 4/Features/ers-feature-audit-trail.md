# ERS Feature — Audit Trail (L, 3 points)

## Summary

Every status change to a reimbursement request is recorded as an immutable audit entry, and the entries are viewable as a per-request history. The system currently retains only a request's final state; who changed what, and when, is unrecoverable. The audit trail makes the lifecycle of each request inspectable.

## Workflow

1. An employee submits a request; the system records a creation entry (to `PENDING`, by the author, with a timestamp).
2. A manager denies the request; the system records a resolution entry (`PENDING` to `DENIED`, by the manager, with a timestamp).
3. Any user entitled to view the request opens its detail view and sees the history: each entry showing the transition, the acting user, and when it occurred, in chronological order.
4. No user, of any role, can alter or delete history entries.

## Requirements

### Functional Requirements

1. Every status change to a reimbursement is recorded as an audit entry: the prior status (empty for creation), the new status, the acting user, and a timestamp
2. Creation is recorded as the first entry of every request's history
3. The full history of a request is retrievable, ordered chronologically
4. History visibility mirrors request visibility: the author and managers can view a request's history; other employees cannot
5. Audit entries are immutable; the API exposes no operation that modifies or deletes them
6. Status changes made by any mechanism produce entries — including bulk actions (one entry per affected request) and draft submission (`DRAFT` to `PENDING`), where those features are present
7. An audit entry is written if and only if its status change is committed: a failed or rejected change produces no entry, and a successful change never lacks one

### Non-Functional Requirements

1. The audit entry is persisted in the same transaction as the status change it records
2. Recording is implemented in the service layer so every code path that changes status passes through it
3. Unit tests cover the new behavior (entry on creation, entry on resolution, history retrieval, authorization)

## User Stories

- As an employee, I can view the history of my own request: what changed, who changed it, and when
- As a manager, I can view the history of any request
- As an employee, I cannot view the history of another employee's request
- As a user, I cannot alter or delete history entries

## Technical Notes

- Schema: a new audit table — id, reimbursement reference, prior status (nullable), new status, acting-user reference, timestamp. This is a new entity and belongs on the final ERD
- Backend: a history endpoint under the reimbursement resource (e.g., `GET /reimbursements/{id}/history`) reusing the request's existing authorization check; entry creation lives beside the status-change logic in the service layer so controllers cannot bypass it
- Frontend: a history section in the request detail view (or an expandable panel in the list), rendering each entry as transition, actor, and timestamp
- If other Iteration 4 features that add status transitions are implemented alongside this one, integrate them at the service layer so their transitions are captured without special cases

## Definition of Done

- [ ] Submitting a request creates a history entry recording creation, the author, and a timestamp
- [ ] Approving or denying a request creates a history entry recording the transition, the resolving manager, and a timestamp
- [ ] Fetching a request's history returns all entries in chronological order
- [ ] The author and managers can fetch a request's history; another employee receives `403`
- [ ] No endpoint exists to modify or delete an audit entry
- [ ] A rejected status change (e.g., resolving an already-resolved request) produces no entry
- [ ] Where bulk actions or drafts are implemented, their transitions appear in the history
- [ ] The frontend displays the history in the request's detail context
- [ ] The audit entity appears on the updated ERD
- [ ] Unit tests for the new service-layer behavior pass
- [ ] No regressions
