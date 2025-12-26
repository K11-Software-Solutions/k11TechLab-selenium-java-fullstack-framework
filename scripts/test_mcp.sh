#!/usr/bin/env bash
set -euo pipefail

# Test MCP endpoints script
# Usage: MCP_PORT=8090 MCP_HOST=http://localhost ./scripts/test_mcp.sh

HOST=${MCP_HOST:-http://localhost}
PORT=${MCP_PORT:-8090}
BASE="$HOST:$PORT/mcp"

echo "Using MCP at $BASE"

echo "\n=== /mcp/health ==="
curl -sS "$BASE/health" || true

echo "\n=== /mcp/workflow ==="
curl -sS -X POST "$BASE/workflow" -H "Content-Type: text/plain" -d "Run login workflow for test user" || true

# Sample broken Java for /mcp/correct-code
BROKEN_JAVA='public class X { public static void main(String[] args) { System.out.println("hi") } }'

echo "\n=== /mcp/correct-code ==="
curl -sS -X POST "$BASE/correct-code" -H "Content-Type: text/plain" -d "$BROKEN_JAVA" || true

# Read page object prompt file from repository and post it to MCP
PROMPT_REL="k11softwaresolutions/pages/pageobject_creation_prompt_multi.txt"
PROMPT_FILE="mcp_prompts/$PROMPT_REL"

if [ -f "$PROMPT_FILE" ]; then
  echo "\n=== /mcp/generate-page-object (using promptFile reference) ==="
  # Instruct server to load the prompt file from its mcp-prompts folder
  curl -sS -X POST "$BASE/generate-page-object" -H "Content-Type: application/json" -d "{\"promptFile\":\"$PROMPT_REL\"}" || true

  # Extract human-readable instruction lines (bulleted lines starting with '-') and send to completion endpoint
  INSTRUCTIONS=$(awk '/^- /{flag=1} flag{print}' "$PROMPT_FILE" | sed 's/^- //') || true
  if [ -n "$INSTRUCTIONS" ]; then
    echo "\n=== /mcp/completion (text prompt from file instructions) ==="
    curl -sS -X POST "$BASE/completion" -H "Content-Type: text/plain" -d "$INSTRUCTIONS" || true
  fi
else
  echo "\nPrompt file not found: $PROMPT_FILE — skipping generate-page-object requests"
fi

# Generate and run tests (playwright + selenium)
echo "\n=== /mcp/generate-and-run-playwright-test ==="
curl -sS -X POST "$BASE/generate-and-run-playwright-test?engine=playwright&packageName=org.k11techlab.framework_unittests.ai_generated.k11softwaresolutions" -H "Content-Type: text/plain" -d "Open login page and sign in" || true

echo "\n=== /mcp/generate-and-run-selenium-test ==="
curl -sS -X POST "$BASE/generate-and-run-selenium-test?engine=selenium&packageName=org.k11techlab.framework_unittests.ai_generated.k11softwaresolutions" -H "Content-Type: text/plain" -d "Open login page and sign in" || true

# Done

echo "\nAll MCP endpoint checks completed."
