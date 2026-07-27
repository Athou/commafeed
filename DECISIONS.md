cat << 'EOF' > DECISIONS.md
# Technical Decisions & AI Redirections Log

This document records pivotal technical decisions, architecture choices, and instances where the AI proposal was corrected or redirected.

## 1. JPA Metamodel Avoidance vs Direct Entity Queries
- **AI Proposal:** Generated JPA Metamodels (`FeedEntryNote_`) for type-safe query criteria in DAOs.
- **Correction:** Metamodel generation caused build-time circular dependencies in Quarkus dev mode. Overrode AI to use explicit JPQL queries in `FeedEntryNoteDAO`.
- **Outcome:** Clean compile time with 0 build errors.

## 2. API Security & DTO Placement Rules
- **AI Proposal:** Separate DTO classes in inner nested structures or independent packages.
- **Correction:** Spotless and Checkstyle strictly enforced `InnerTypeLast` rules. Refactored DTOs to static Java `record` types located at the bottom of REST controllers.
- **Outcome:** Passed Checkstyle rules with 0 violations.

## 3. Gemini API Model Endpoint Correction
- **AI Proposal:** Standard REST call to `gemini-1.5-flash` with key as query parameter.
- **Correction:** Google API Studio returned 404/Quota errors for parameter-based authentication on new Flash models. Redirected AI to use `gemini-2.5-flash` with the header `x-goog-api-key`.
- **Outcome:** Successful end-to-end LLM rewrite execution.

## 4. Level 3 Asynchronous Non-Blocking Execution
- **AI Proposal:** Synchronous execution of keyword matching during RSS feed entry persistence.
- **Correction:** Sync HTTP calls inside feed refresh would degrade feed ingestion performance if webhooks slow down. Enforced `CompletableFuture.runAsync` pattern.
- **Outcome:** RSS feed parsing remains fast; notifications fire asynchronously.

## 5. Level 4 Choice: In-Memory Caching & Observability
- **Selection:** Implemented Thread-safe Caching (`ConcurrentHashMap`) and Structured Logging for `LLMRewriteService`.
- **Rationale:** Prevents unnecessary external LLM API billing and quota usage for identical re-write requests on the same article.
  EOF