# ERS Feature — Bulk Actions (M, 2 points)

## Summary

Managers approve or deny multiple pending requests in a single action. Resolving requests one at a time does not scale to a queue of any size; this feature turns N identical resolutions into one operation.

## Workflow

1. A manager views the pending requests; each row offers a selection control (checkbox), with a select-all for the visible set.
2. The manager selects two or more requests and chooses *Approve selected* or *Deny selected*.
3. The interface asks for confirmation, stating the action and the number of requests affected.
4. On confirmation, the action is applied. Each selected request that is still `PENDING` is resolved; the manager is recorded as its resolver.
5. The interface reports the outcome, including any requests that could not be resolved (e.g., already resolved by another manager), and the list refreshes.

## Requirements

### Functional Requirements

1. A manager can select multiple `PENDING` requests and approve all of them, or deny all of them, in a single action
2. The bulk action is a single API request carrying the target request ids and the resolution
3. Each successfully resolved request records the acting manager as its resolver, identically to a single resolution
4. Bulk resolution is per-item, not all-or-nothing: requests that are no longer `PENDING` at execution time are left unmodified, and the remainder still resolve
5. The response reports the outcome per request id (resolved, or skipped with a reason)
6. A bulk action containing ids the manager cannot resolve (nonexistent, or drafts if the saved-reimbursements feature is present) skips those ids and reports them; it does not fail the whole action
7. Only managers can invoke the bulk action; employees are rejected with `403`
8. The frontend requires confirmation before executing a bulk action

### Non-Functional Requirements

1. The per-item resolution logic reuses the existing single-resolution service logic rather than duplicating it
2. Unit tests cover the new service-layer behavior, including the mixed case (some resolvable, some not)

## User Stories

- As a manager, I can select multiple pending requests and approve them in one action
- As a manager, I can select multiple pending requests and deny them in one action
- As a manager, I am told which requests in my selection could not be resolved and why
- As an employee, I cannot invoke bulk actions

## Technical Notes

- Schema: no changes
- Backend: one endpoint accepting a list of request ids and a resolution (`APPROVED`/`DENIED`); iterate in the service layer, delegating to the existing resolve logic per id; return a result DTO listing successes and skips. If the manager-comments feature is also implemented, decide and document whether a bulk denial accepts a single comment applied to every denied request
- Frontend: selection state on the manager's pending list, select-all, an action bar enabled when the selection is non-empty, a confirmation dialog, and outcome feedback (e.g., "8 approved, 1 skipped: already resolved")

## Definition of Done

- [ ] A manager can approve several pending requests with one API call; all become `APPROVED` with the manager recorded as resolver
- [ ] A manager can deny several pending requests with one API call; all become `DENIED` with the manager recorded as resolver
- [ ] A bulk action whose selection includes an already-resolved request resolves the rest and reports that request as skipped; the skipped request is unmodified
- [ ] The response identifies the outcome of every id submitted
- [ ] An employee invoking the bulk endpoint receives `403`
- [ ] The frontend supports multi-select with select-all, confirms before acting, and surfaces per-item outcomes
- [ ] Unit tests for the new service-layer behavior pass, including the mixed-outcome case
- [ ] Single-request resolution continues to work unchanged; no regressions
