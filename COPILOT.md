# Copilot Development Guide

## Stack and Architecture

- **Backend (`commafeed-server`):** Java 21, Quarkus, and H2 Database.
- **Frontend (`commafeed-client`):** React and TypeScript.
- Preserve backend layer isolation: **REST Resource -> Service -> DAO -> JPA Entity**.
- Follow the existing API, routing, state-management, and component patterns in `commafeed-client`.

## Build and Test Commands

Run from the project root unless noted:

```text
.\mvnw.cmd spotless:apply -pl commafeed-server
.\mvnw.cmd checkstyle:check -pl commafeed-server
.\mvnw.cmd test -pl commafeed-server
```

For frontend development, run `npm run dev` from `commafeed-client`.

## Constraints

- Inspect existing vertical slices before generating code.
- Do not write unused code or redundant comments.
- Do not modify files outside the current feature scope.

## Level 1: Saved Entry Notes

- Saved entry notes use the backend vertical slice: `FeedEntryNoteREST -> FeedEntryNoteService -> FeedEntryNoteDAO -> FeedEntryNote`.
- Notes belong to the authenticated user and a subscribed feed entry; preserve this ownership boundary.
- The note rating is optional but, when provided, must be an integer from 1 through 5.
- Keep note request/response DTOs in the existing `commafeed-server` frontend model packages and follow existing REST security annotations.
