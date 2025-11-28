# How to Run the RAG Embedding Cache Demo

1. **Build the project:**
	```sh
	mvn clean package
	```
2. **Run the demo:**
	```sh
	java -cp target/classes org.k11techlab.framework.ai.rag.demo.RAGComponentsDemo
	```
	Or, if using Maven:
	```sh
	mvn exec:java -Dexec.mainClass="org.k11techlab.framework.ai.rag.demo.RAGComponentsDemo"
	```
3. **View the output:**
	- Output will be printed to the console.
	- To save output to a file:
	  ```sh
	  java -cp target/classes org.k11techlab.framework.ai.rag.demo.RAGComponentsDemo > RAGComponentsDemo_output.txt
	  ```
4. **Check the embedding cache:**
	- The file `embedding_cache.json` will be created/updated in your project directory.
	- On subsequent runs, you should see `[EMBED] Cache HIT` for all unchanged chunks.

# 🚀 How I Optimized RAG Embedding Efficiency in Java (with Live Demo Output!)

Retrieval-Augmented Generation (RAG) is a game-changer for AI-powered search and Q&A, but embedding computation can be slow and costly if not managed well. In my latest project, I implemented a persistent embedding cache for our Java-based RAG pipeline—ensuring embeddings are only computed once per document chunk and reused on every run.

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

