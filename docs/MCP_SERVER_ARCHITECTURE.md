**MCP Server Architecture**

**Overview**

This document describes the architecture of the Model Control Plane (MCP) server implemented in this repository. The MCP provides a lightweight HTTP front-end that accepts prompts and workflow requests, orchestrates AI clients and local tools (Playwright bridge), and stores short-term context.

**Components**

- **MCP server (Java):** The entry point that binds HTTP endpoints and dispatches requests to handlers. See [src/main/java/org/k11techlab/framework/ai/mcp/MCPServer.java](src/main/java/org/k11techlab/framework/ai/mcp/MCPServer.java#L1).
- **Handlers:** Small HttpHandler classes for each endpoint (health, completion, context, generate-page-object, generate-and-run-playwright-test, generate-and-run-selenium-test, correct-code, workflow). See [src/main/java/org/k11techlab/framework/ai/mcp/handlers](src/main/java/org/k11techlab/framework/ai/mcp/handlers/).
- **AI Clients:** Pluggable clients used by handlers to call LLMs or RAG services (RAGEnhancedAIClient, OpenAIClient). These encapsulate prompt formatting, API calls and retry/timeout logic.
- **Playwright bridge (PlaywrightMCPClient):** A process-based JSON-RPC client wrapper that runs `npx @playwright/mcp` and communicates over stdio to control Playwright for browser-driven test execution. See [src/main/java/org/k11techlab/framework/ai/mcp/util/PlaywrightMCPClient.java](src/main/java/org/k11techlab/framework/ai/mcp/util/PlaywrightMCPClient.java#L1).
- **Workflows:** Higher-level orchestration (e.g., PlaywrightWorkflow) that generates a test, runs it, and archives logs/reports. See [src/main/java/org/k11techlab/framework/ai/mcp/workflow/playwright/PlaywrightWorkflow.java](src/main/java/org/k11techlab/framework/ai/mcp/workflow/playwright/PlaywrightWorkflow.java#L1).
- **Context store (MongoContextStore):** Optional persistence for conversation/context objects using MongoDB. Configured via `config/mcp-config.properties`. See [src/main/java/org/k11techlab/framework/ai/mcp/store/MongoContextStore.java](src/main/java/org/k11techlab/framework/ai/mcp/store/MongoContextStore.java#L1).
- **Prompts folder:** `mcp_prompts/` holds prompt files referenced by requests (e.g. `mcp_prompts/k11softwaresolutions/pages/pageobject_creation_prompt_multi.txt`). The server can accept a `promptFile` reference to load prompts server-side.

**Endpoints (summary)**

- `GET /mcp/health` — simple liveness check handled by HealthHandler.
- `POST /mcp/completion` — textual prompt completion using AI client.
- `POST /mcp/generate-page-object` — accept `{ "promptFile":"<relpath>" }` or text and run the page-object generator.
- `POST /mcp/generate-and-run-playwright-test` — generate Playwright code and run it via the Playwright bridge; archives logs to `mcp_testlog/` and `mcp_testreport/`.
- `POST /mcp/generate-and-run-selenium-test` — generate Selenium (Java/TestNG) test and run via Maven/Surefire.
- `POST /mcp/correct-code` — fix or transform submitted code.

See handlers directory for implementation details: [src/main/java/org/k11techlab/framework/ai/mcp/handlers](src/main/java/org/k11techlab/framework/ai/mcp/handlers/).

**Data & control flow (high-level)**

1. Client sends HTTP request to MCP server endpoint (e.g., generate-page-object).
2. Handler validates input, loads prompt text (inline or from `mcp_prompts/`), and prepares LLM input.
3. Handler invokes an AI client (RAG or direct LLM) to generate code (page object or test).
4. For run requests, the handler either:
   - writes generated artifacts to disk and calls the Playwright bridge (PlaywrightMCPClient) which runs Playwright and streams test results; or
   - generates Java test code and invokes Maven to run the test (Selenium path).
5. Workflow copies outputs to `mcp_testlog/` and `mcp_testreport/` and returns a response with run metadata.

**Configuration**

- `config/mcp-config.properties` controls port, prompt mappings, classNameBase, log.dir and Mongo connection settings. See [config/mcp-config.properties](config/mcp-config.properties#L1).
- Default port observed in the repo is `8090` (property `mcp.port`).

**Runtime requirements**

- Java 17+ / JDK 21 recommended for running the server (project was prepared for Java 21 migration).
- Node.js + Playwright CLI (`npx @playwright/mcp`) for Playwright runs.
- MongoDB if you want persistent context storage (optional; in-memory alternatives may exist).

**Artifacts & logs**

- Generated tests and page objects: `src/test/java/org/k11techlab/framework_unittests/ai_generated/...` (example generated test: [GeneratedPlaywrightLoginTest.java](src/test/java/org/k11techlab/framework_unittests/ai_generated/k11softwaresolutions/GeneratedPlaywrightLoginTest.java#L1)).
- Test run logs: `mcp_testlog/`.
- HTML reports: `mcp_testreport/` and `target/surefire-reports/` for Maven-run tests.

**Troubleshooting notes**

- If endpoints return connection errors, ensure the MCP server process is running and listening on the configured port (look for startup message: "MCP Server started on port <port>").
- Playwright runs require `npx` available in PATH and appropriate browser binaries; run `npx playwright install` if required.
- Check `mcp_testlog/` for Playwright bridge logs and `mcp_testreport/` for HTML test reports after a run.

**Where to look in the code**

- Server bootstrap: [src/main/java/org/k11techlab/framework/ai/mcp/MCPServer.java](src/main/java/org/k11techlab/framework/ai/mcp/MCPServer.java#L1)
- Handlers: [src/main/java/org/k11techlab/framework/ai/mcp/handlers](src/main/java/org/k11techlab/framework/ai/mcp/handlers/)
- Playwright bridge: [src/main/java/org/k11techlab/framework/ai/mcp/util/PlaywrightMCPClient.java](src/main/java/org/k11techlab/framework/ai/mcp/util/PlaywrightMCPClient.java#L1)
- Playwright orchestration: [src/main/java/org/k11techlab/framework/ai/mcp/workflow/playwright/PlaywrightWorkflow.java](src/main/java/org/k11techlab/framework/ai/mcp/workflow/playwright/PlaywrightWorkflow.java#L1)
- Prompts: `mcp_prompts/` folder (example: [mcp_prompts/k11softwaresolutions/pages/pageobject_creation_prompt_multi.txt](mcp_prompts/k11softwaresolutions/pages/pageobject_creation_prompt_multi.txt#L1)).

**Next steps / improvements (suggested)**

- Harden generated interactions with robust waits / fallback actions in generators (addresses flaky selectors like the "login menu not visible" case).
- Add a small systemd/Windows service script or a Dockerfile to run MCP persistently for CI/demo scenarios.
- Add a health-check and readiness probe endpoint that also verifies Playwright and Mongo availability.
