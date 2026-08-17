# ERS Feature — Receipts & Image Uploads (XL, 5 points)

## Summary

Employees attach one or more receipt images to a reimbursement request, and users authorized to view the request can view its receipts. Requests currently carry only a textual description; this feature adds the documentary evidence a real reimbursement process requires, and with it the system's first handling of binary data.

## Workflow

1. An employee creates or edits one of their `PENDING` requests and uploads one or more receipt images.
2. Each upload is validated (type, size); accepted images are stored and listed on the request as thumbnails or a file list.
3. The employee may remove a receipt from the request while it is still `PENDING`.
4. A manager reviewing the request opens its receipts and views each image at full size before resolving.
5. Once the request is resolved, its receipts are frozen: they can no longer be added or removed, but remain viewable.

## Requirements

### Functional Requirements

1. An employee can attach one or more receipt images to their own request while it is modifiable (i.e., `PENDING`, and `DRAFT` where the saved-reimbursements feature is present)
2. Accepted formats are JPEG and PNG; any other upload is rejected with a meaningful error
3. Uploads are limited to 5 MB per image and 5 images per request; violations are rejected with a meaningful error
4. The request's author and managers can list a request's receipts and view each image; other employees are rejected with `403`
5. The author can remove a receipt while the request is modifiable
6. Once a request is resolved, receipts cannot be added or removed; the attempt is rejected. Viewing remains available
7. No user can attach receipts to, or remove receipts from, another user's request
8. Receipt retrieval requires a valid bearer token, exactly as every other protected resource
9. A request remains valid with zero receipts; attaching receipts is optional

### Non-Functional Requirements

1. Uploads use `multipart/form-data`; the stored content type is determined from the uploaded content, not trusted solely from the file extension or client-supplied type
2. Upload size limits are enforced on the backend regardless of any frontend checks
3. Stored receipts survive application restart and, once the stack is containerized, survive container recreation (volume-backed storage or database storage)
4. Unit tests cover the new service-layer behavior (attach, list, remove, authorization, validation, immutability after resolution)

## User Stories

- As an employee, I can attach receipt images to my pending request
- As an employee, I can view and remove the receipts on my pending request
- As an employee, I cannot modify receipts on a resolved request
- As a manager, I can view the receipts attached to any request I am reviewing
- As an employee, I cannot view or modify receipts on another employee's request

## Technical Notes

- Schema: a new receipt entity — id, reimbursement reference, file name, content type, size, and either the image bytes (database `BLOB`) or a storage path. Both storage strategies are acceptable; database storage is simpler to keep consistent and to containerize, filesystem storage keeps the database lean but requires a mounted volume. Choose one and document it. The new entity belongs on the final ERD
- Backend: endpoints under the reimbursement resource to upload (multipart), list metadata, download/view one receipt, and delete one receipt; reuse the request's existing authorization checks; configure multipart size limits so oversized uploads produce a clean `400`/`413` rather than a server error
- Frontend: a file input (accepting multiple files) on the request form and detail view for the author; receipt thumbnails or a list with a full-size view for authorized users; note that `<img src>` does not carry the Authorization header — fetch image bytes through the authenticated HTTP client and render via an object URL, or an equivalent approach
- This is the largest feature in the iteration; a sensible order is backend storage and endpoints first, verified with an HTTP client, then the frontend upload and viewing flows

## Definition of Done

- [ ] An employee can upload a JPEG or PNG to their pending request; it is persisted and appears when the request's receipts are listed
- [ ] Multiple receipts can be attached to one request, up to the limit; exceeding the limit is rejected with a meaningful error
- [ ] A non-image upload and an oversized upload are each rejected with a meaningful error and nothing is stored
- [ ] The author and managers can retrieve and view each receipt image; another employee receives `403`
- [ ] Receipt retrieval without a valid token is rejected with `401`
- [ ] The author can remove a receipt from a pending request; it no longer appears and its content is no longer retrievable
- [ ] After the request is resolved, adding or removing receipts is rejected; viewing still works
- [ ] Receipts survive a full stack restart (`docker compose down && docker compose up`)
- [ ] The frontend supports upload with progress/error feedback and displays receipt images to authorized users
- [ ] The receipt entity appears on the updated ERD
- [ ] Unit tests for the new service-layer behavior pass
- [ ] No regressions
