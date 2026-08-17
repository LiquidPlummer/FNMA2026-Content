# ERS Feature — Saved Reimbursements (M, 2 points)

## Summary

Employees save a reimbursement request as a draft and submit it later. Drafts are visible only to their author: they do not appear in manager views and cannot be resolved. The feature decouples composing a request from submitting it into the review queue.

## Workflow

1. An employee fills in the reimbursement form and chooses *Save as draft* instead of *Submit*.
2. The draft appears in the employee's own list, clearly marked as a draft.
3. The employee may edit the draft, delete it, or submit it at any time.
4. On submission, the draft becomes a `PENDING` request and enters the normal lifecycle: it appears in manager views and can be resolved.
5. Managers never see the request while it is a draft.

## Requirements

### Functional Requirements

1. An employee can save a reimbursement as a draft instead of submitting it; a draft carries the same fields as a submitted request and is subject to the same validation
2. A draft is visible only to its author; drafts are excluded from all manager list views, filters, and any counts of pending work
3. The author can edit a draft
4. The author can delete a draft; submitted requests remain non-deletable as before
5. The author can submit a draft; its status becomes `PENDING` and it thereafter behaves exactly as a directly submitted request
6. A draft cannot be approved or denied; the status transitions permitted from draft are submission (to `PENDING`) and deletion
7. No user can view, edit, delete, or submit another user's draft
8. The status filter in the employee's own list view includes drafts

### Non-Functional Requirements

1. Draft handling follows the established architecture; the draft state is enforced in the service layer, not solely in the frontend
2. Unit tests cover the new service-layer behavior (save, edit, delete, submit, exclusion from manager queries, cross-user access denied)

## User Stories

- As an employee, I can save a reimbursement request as a draft instead of submitting it
- As an employee, I can view my drafts alongside my other requests, distinguished from them
- As an employee, I can edit, delete, or submit a draft
- As a manager, I never see another user's drafts
- As an employee, I cannot access another employee's drafts

## Technical Notes

- Schema: a new `DRAFT` value in the status enumeration (and its database representation) is the least invasive design; a separate boolean flag is a workable alternative — either way, every manager-facing query must exclude drafts
- Backend: creation endpoint accepts a draft/submit distinction (flag or separate endpoint); a submit transition endpoint; a delete endpoint restricted to the author's drafts; audit all existing manager queries for draft leakage
- Frontend: a *Save as draft* action on the request form; a draft badge and edit/delete/submit actions in the employee's list; the existing edit form is reusable since drafts share the pending request's editability

## Definition of Done

- [ ] Saving as draft persists the request with draft status; it appears in the author's list marked as a draft
- [ ] Drafts do not appear in any manager list view or filter result, nor in pending counts
- [ ] The author can edit a draft and the changes persist
- [ ] The author can delete a draft; fetching it afterward returns `404`
- [ ] Submitting a draft sets its status to `PENDING`; it then appears to managers and can be resolved normally
- [ ] Attempting to approve or deny a draft is rejected
- [ ] Any access to another user's draft (view, edit, delete, submit) is rejected
- [ ] Deleting a non-draft request is rejected
- [ ] Unit tests for the new service-layer behavior pass
- [ ] No regressions in the existing submit/edit/resolve flows
