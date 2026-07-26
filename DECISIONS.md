cat << 'EOF' > DECISIONS.md
# Architectural and Technical Decisions

## 1. Data Model & JPA (Level 1: Feed Entry Notes)
- **Entity Design:** Implemented `FeedEntryNote` entity with `@ManyToOne` relations to `User` and `FeedEntry`.
- **DAO Implementation:** Implemented `FeedEntryNoteDAO` extending `GenericDAO`. Used explicit `EntityManager` JPA queries to prevent issues with runtime metamodel compilation.

## 2. API Design & Security
- **REST Resources:** Created `FeedEntryNoteREST` and `LLMRewriteREST` under `/rest/entry/...` protected by `@RolesAllowed(Roles.USER)`.
- **DTO Placement:** Kept static DTO records at the end of resource files to adhere strictly to Checkstyle rules (`InnerTypeLast`).

## 3. LLM Content Rewriter (Level 2)
- **Provider:** Integrated Google Gemini-3.5-flash-lite via Java native `HttpClient`.
- **Configuration:** Flexible configuration loading supporting both System properties (`-DLLM_API_KEY`) and Environment variables (`System.getenv`).
- **Error Handling:** Handled missing credentials, non-200 provider status codes, and HTTP timeouts cleanly without crashing the application thread.

## 4. Code Quality
- Verified code style and formatting using `mvn com.diffplug.spotless:spotless-maven-plugin:apply -pl commafeed-server`.
- Checked zero warnings policy for Checkstyle rules.
  EOF