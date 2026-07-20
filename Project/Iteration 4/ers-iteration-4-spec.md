# ERS — Iteration 4 Specification

## Overview

This document specifies Iteration 4 of the ERS. It defines the iteration's objectives, the features available for implementation, the requirements governing that work, and the deliverables to be submitted.

The ERS is MVP-complete as of Iteration 3. Iteration 4 treats it as an inherited codebase: we extend the system with new features, refactor problem areas, and polish rough edges across the stack. This is the final iteration and concludes with a presentation of the finished system.

> **Note:** All requirements from prior iterations remain in effect. This specification covers only the new work.

## Objectives

- Extend an existing, working codebase with new features touching every layer
- Containerize the full application stack with Docker Compose
- Refactor and polish: address technical debt, inconsistencies, and usability issues
- Present the completed system

## Features

New features are selected from the list below. Each is sized in story points from S (small) to XL (extra large), with a corresponding point value. Select features based on their combined point value — one XL story or several S and M stories represent comparable effort.

| Feature | Size | Points | Description |
|---------|------|--------|-------------|
| Manager comments | S | 1 | Managers attach a comment when denying a request; the author can view it |
| User profile page | S | 1 | Users view and edit their own profile (name, password) |
| Dashboard | M | 2 | A landing view on login: pending counts and recent activity relevant to the user's role |
| Saved reimbursements | M | 2 | Employees save a request as a draft and submit it later; drafts are visible only to their author |
| Bulk actions | M | 2 | Managers approve or deny multiple pending requests in a single action |
| Admin role | L | 3 | A third role, `ADMIN`, able to change the role of any other user |
| Department management | L | 3 | Admins create and rename departments, and reassign users between them |
| Audit trail | L | 3 | Every status change to a request is recorded and viewable in a per-request history |
| Receipts & image uploads | XL | 5 | Employees attach one or more receipt images to a request; authorized users can view them |

## Requirements

### Non-Functional Requirements

1. The full stack (database, backend, frontend) runs via a single `docker compose up`
2. New features follow the established architecture on both backend and frontend
3. Existing functionality remains intact; no regressions
4. Unit tests cover the new backend functionality

## Deliverables

- Codebase in a GitHub repository
- Final presentation with a live demonstration of the complete system
- Updated ERD reflecting the final schema