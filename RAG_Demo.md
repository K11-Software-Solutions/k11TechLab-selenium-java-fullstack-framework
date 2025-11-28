
# 🌟 Full AI Workflow Demo: NLGenerationAndChatbotTest.java

Start with the comprehensive demonstration in `NLGenerationAndChatbotTest.java` to see the entire AI-enhanced test automation workflow in action—including natural language test generation, RAG-powered chatbot, conversation context/memory, and advanced integration.

**Test class:**
`src/test/java/org/k11techlab/framework_unittests/aiTests/NLGenerationAndChatbotTest.java`

**Key features demonstrated:**
- Natural language to TestNG test generation (with code patching and file output)
- RAG-powered chatbot Q&A, debugging, and code review
- Multi-turn conversation with context and memory
- Advanced integration: multi-step test generation, knowledge base retrieval, and performance assessment
- End-to-end assertions and debug output for each step

**Generated output:**
- All generated test code and conversation logs are saved in `src/test/ai_generated_tests/` for review and reuse.

**How to run:**
1. Build the project:
	```sh
	mvn clean package
	```
2. Run the test suite (from your IDE or with Maven):
	```sh
	mvn test -Dtest=org.k11techlab.framework_unittests.aiTests.NLGenerationAndChatbotTest
	```
3. Review the console output and generated files in `src/test/ai_generated_tests/`.


## See it in action:
* [NLGenerationAndChatbotTest.java (Full AI Workflow Demo)](src/test/java/org/k11techlab/framework_unittests/aiTests/NLGenerationAndChatbotTest.java)
* [Generated Output Folder](src/test/ai_generated_tests/)

Explore this class to see how RAG and NLP can supercharge your test automation workflow!

# How to Run the RAG Embedding Cache Demo

1. **Build the project:**
	```sh
	mvn clean package
	```
2. **Run the demo:**
	```sh
	java -cp target/classes org.k11techlab.framework.ai.rag.demo.RAGComponentsDemo
	Or, if using Maven:
	```sh
	mvn exec:java -Dexec.mainClass="org.k11techlab.framework.ai.rag.demo.RAGComponentsDemo"
	```
3. **View the output:**
	- Output will be printed to the console.
	- To save output to a file:
	  ```sh

# 🚀 How I Optimized RAG Embedding Efficiency in Java (with Live Demo Output!)
## What’s the impact?
- ⚡️ Massive speedup on repeated queries
- 💸 Reduced API costs (no redundant embedding calls)
- 🧠 Efficient, production-grade RAG for test automation

## How does it work?
- On first run, embeddings are computed and cached.
- On subsequent runs, the cache is hit for all unchanged files—no recomputation!
- Only new or modified files trigger fresh embedding calls.

## See it in action:
* [RAGComponentsDemo.java (Test Class)](https://github.com/K11-Software-Solutions/k11TechLab-selenium-java-fullstack-framework/blob/main/src/main/java/org/k11techlab/framework/ai/rag/demo/RAGComponentsDemo.java)
* [Sample Output (Cache Hits!)](https://github.com/K11-Software-Solutions/k11TechLab-selenium-java-fullstack-framework/blob/main/RAGComponentsDemo_output.txt)

## Sample Output
```text
[EMBED] Cache HIT for chunk: testartifacts/docs/AI_TESTING_ASSISTANT_GUIDE.md
[EMBED] Cache HIT for chunk: testartifacts/docs/AI_Testing_Assistant/RAG_Architecture/RAG_ARCHITECTURE_GUIDE.md
...
Top relevant docs for: How do I handle NoSuchElementException?
- testartifacts/docs/AI_Testing_Assistant/RAG_Architecture/RAG_ARCHITECTURE_GUIDE.md
...
===== Synthesized Answer =====
...
```

## Takeaway
With a simple file-based cache, you can make your RAG pipeline blazing fast and cost-effective. If you’re building AI search or Q&A in Java, check out the code and try it yourself!

#AI #Java #RAG #MachineLearning #TestAutomation #OpenSource

---

# 🧪 More RAG Test Samples

Below are additional sample queries and outputs to showcase the RAG pipeline’s power for framework Q&A and test automation:

## 1. Framework Q&A
**Query:**
```
How do I implement a self-healing Selenium locator?
```
**Sample Output:**
```
Top relevant docs for: How do I implement a self-healing Selenium locator?
- testartifacts/docs/AI_Testing_Assistant/SELENIUM_SELF_HEALING.md
...
===== Synthesized Answer =====
To implement a self-healing Selenium locator, use a wrapper that tries alternative locators when the primary one fails. For example, maintain a list of XPaths or CSS selectors and attempt each in order. See the Self-Healing module in the framework docs for code samples.
```

## 2. Test Case Generation
**Query:**
```
Generate a TestNG test for login functionality.
```
**Sample Output:**
```
Top relevant docs for: Generate a TestNG test for login functionality.
- testartifacts/docs/AI_Testing_Assistant/TEST_AUTOMATION_GUIDE.md
...
===== Synthesized Answer =====
@Test
public void testLogin() {
	driver.get("https://example.com/login");
	driver.findElement(By.id("username")).sendKeys("user");
	driver.findElement(By.id("password")).sendKeys("pass");
	driver.findElement(By.id("loginBtn")).click();
	Assert.assertTrue(driver.findElement(By.id("welcomeMsg")).isDisplayed());
}
```

## 3. Troubleshooting
**Query:**
```
Why am I getting StaleElementReferenceException in Selenium?
```
**Sample Output:**
```
Top relevant docs for: Why am I getting StaleElementReferenceException in Selenium?
- testartifacts/docs/AI_Testing_Assistant/SELENIUM_EXCEPTIONS.md
...
===== Synthesized Answer =====
StaleElementReferenceException occurs when the element you are interacting with is no longer attached to the DOM. This can happen after a page reload or dynamic content update. Solution: Re-locate the element before interacting, or use explicit waits.
```

## 4. Framework Usage
**Query:**
```
How do I configure the AI provider order for RAG?
```
**Sample Output:**
```
Top relevant docs for: How do I configure the AI provider order for RAG?
- config/chatbot.ai.properties
...
===== Synthesized Answer =====
Set the provider order in config/chatbot.ai.properties:
ai.provider.priority=OPENAI,OLLAMA,SIMPLE
This determines the fallback order for LLMs in your RAG pipeline.
```

---

