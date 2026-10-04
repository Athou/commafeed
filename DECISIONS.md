# Engineering Decisions

This document records four points where generated implementation details were corrected or constrained to match the project requirements.

## Java Environment and Build Flags

The build workflow was constrained to JDK 21 by enforcing `-Dmaven.compiler.release=21`. Maven builds also use `-am` where needed so dependent modules are built together, avoiding local Maven artifact resolution issues.

## Deprecation Fixes

Deprecated `@Temporal` annotations were removed from the `FeedEntryNote` entity. Its `Date` field now follows the existing entity mapping style, supporting the repository's strict `-Werror` compilation policy.

## Checkstyle and Formatting Enforcement

The unnecessary-parentheses condition in `EntryREST.java` was corrected, and the affected code was reformatted through Spotless. This keeps the implementation aligned with the repository's Checkstyle and Google Java Format rules.

## Dependency and Architecture Alignment

Missing Apache HttpClient imports were replaced with native Java 21 `java.net.http.HttpClient` APIs in `LlmRewriteService.java`. This avoids introducing or relying on unnecessary external HTTP dependencies while keeping the service compatible with the project's Java 21 baseline.
