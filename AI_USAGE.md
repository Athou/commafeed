# AI Tools Usage Statement

This document outlines how AI capabilities and LLM tools were utilized during the design, development, and integration process of the CommaFeed features.

---

## 1. Feature Architecture & Embedded LLM Integration (Level 2)

As part of **Level 2 (LLM Rewrite)**, a generative AI model was integrated directly into the application backend to provide dynamic text rewriting and summarization capabilities for RSS feed items.

* **Provider & Model:** Google Gemini API (`gemini-3.5-flash-lite`).
* **Implementation Details:**
    * Developed `LLMRewriteService` utilizing standard Java `HttpClient` and Jackson (`ObjectMapper`) for lightweight, dependency-free execution.
    * Configured dynamic prompt engineering for target options (e.g., rewriting titles vs. articles according to user instructions).
    * Implemented an in-memory caching mechanism using `ConcurrentHashMap` based on composite request hashes (`originalContent + target + prompt`) to optimize API latency and reduce token consumption.

---

## 2. AI-Assisted Development & Productivity

AI tools were also leveraged during the software engineering workflow to accelerate development and uphold code quality:

* **Code Generation & Boilerplate:** Used AI assistance to draft initial DTO representations, REST endpoint structures (`KeywordREST`, `EntryREST` extensions), and Liquibase database migration scripts (`db.changelog-*.xml`).
* **Debugging & Concurrency Handling:** Consulted AI for best practices regarding Quarkus CDI context propagation (`Arc.container().requestContext()`) inside asynchronous `CompletableFuture` pipelines within `KeywordNotificationService`.
* **Code Formatting & Verification:** Leveraged AI guidance to ensure strict adherence to Checkstyle and Spotless formatting rules enforced by the Maven build system.

---

## 3. Summary of Key Benefits

1. **Efficiency:** Significantly reduced boilerplate coding time for REST interfaces and JPA entities.
2. **Performance:** Reduced external API costs and response times via intelligent In-Memory caching.
3. **Reliability:** Ensured proper transaction management and scope handling across background threads in Quarkus.