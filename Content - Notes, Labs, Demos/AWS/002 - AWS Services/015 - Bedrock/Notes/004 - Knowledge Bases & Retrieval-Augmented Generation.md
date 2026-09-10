# Knowledge Bases & Retrieval-Augmented Generation

A model knows what it was trained on. It does not know our documents, our data, or anything that happened after its training cutoff. **Retrieval-augmented generation (RAG)** addresses that by retrieving relevant content and including it in the prompt.

---

## The Mechanism

RAG has two phases.

**Ingestion**, done once and repeated as content changes:

1. Split documents into chunks.
2. Convert each chunk to an **embedding** — a vector representing its meaning.
3. Store the vectors in a vector database.

**Retrieval**, on every query:

1. Convert the user's question to an embedding.
2. Find the most similar chunks by vector distance.
3. Include them in the prompt as context.
4. The model answers from that context.

```
Question ──► embedding ──► vector search ──► top chunks
                                                  │
                                                  ▼
              "Answer using only this context: <chunks>\n\nQuestion: ..."
                                                  │
                                                  ▼
                                                Model
```

*The model is not trained on our data. The relevant portion is retrieved and placed in the prompt at query time.*

The key insight is that retrieval is **semantic**, not keyword-based. A question about "refund policy" matches a chunk about "returning purchases" because their embeddings are close, even with no shared words.

---

## Bedrock Knowledge Bases

Knowledge Bases manage the whole pipeline: ingestion, chunking, embedding, storage, and retrieval.

Configuration involves a data source (usually S3), an embedding model, and a vector store — OpenSearch Serverless, Aurora PostgreSQL with pgvector, Pinecone, or others.

Two APIs:

**`Retrieve`** returns relevant chunks, leaving generation to us. Useful when the chunks feed something other than a single model call.

**`RetrieveAndGenerate`** does both:

```python
response = agent_client.retrieve_and_generate(
    input={"text": "What is the refund window for enterprise customers?"},
    retrieveAndGenerateConfiguration={
        "type": "KNOWLEDGE_BASE",
        "knowledgeBaseConfiguration": {
            "knowledgeBaseId": "ABCD1234",
            "modelArn": "arn:aws:bedrock:us-east-1::foundation-model/anthropic.claude-sonnet-4-20250514-v1:0",
        },
    },
)
print(response["output"]["text"])
print(response["citations"])
```

*Retrieves and generates in one call, returning citations identifying which source chunks informed the answer.*

**Citations matter more than they first appear.** They let a user verify a claim against the source, which is the practical mitigation for a model stating something confidently and incorrectly.

---

## What Determines Quality

RAG quality is mostly a retrieval problem. If the right chunk is not retrieved, no model can answer correctly.

**Chunking strategy.** Chunks that are too small lose context; too large dilute the embedding and waste tokens. Splitting on semantic boundaries — sections, headings — works better than fixed character counts. Overlapping chunks avoid splitting an answer across a boundary.

**The embedding model.** Different models produce different retrieval quality for different content. Changing it requires re-embedding everything, so it is worth evaluating early.

**Number of chunks retrieved.** Too few risks missing the answer; too many dilute the context and multiply cost. Five to ten is a common starting point.

**Metadata filtering.** Restricting retrieval by document type, date, or access level before the vector search improves relevance substantially, and is how per-user document access is enforced.

**Source content quality.** Poorly structured, outdated, or contradictory documents produce poor answers. This is usually the largest factor and the least discussed.

---

## Where RAG Struggles

Being clear about the limits prevents disappointment:

**Aggregation questions.** "How many customers churned last quarter?" requires counting, not retrieval. That is a database query, and a RAG system will retrieve some documents and produce an unreliable answer. Route such questions to a query rather than to retrieval.

**Questions needing the whole corpus.** "Summarize all our policies" cannot be answered from ten chunks.

**Multi-hop reasoning.** Questions requiring a fact from document A to find the relevant part of document B often fail, because retrieval is a single step against the original question.

**Recency.** The index reflects the last ingestion. Content changed since then is not retrieved, so ingestion scheduling is a real operational concern.

**Precise numeric lookup.** Semantic similarity is poor at matching exact identifiers. Keyword or hybrid search handles these better, and several vector stores support hybrid retrieval combining both.

---

## Agents

**Bedrock Agents** extend this by letting a model call tools — invoking Lambda functions to query a database, call an API, or take an action — and chaining several steps.

An agent can answer "how many customers churned last quarter" by calling a function that runs the query, rather than attempting to retrieve the answer from documents. This addresses the aggregation gap directly, and it introduces the reliability considerations of any system where a model decides what to invoke.

---

## Key Takeaways

- RAG retrieves relevant content at query time and places it in the prompt; the model is never trained on our data.
- Retrieval is semantic, so relevant chunks match by meaning rather than shared keywords.
- Knowledge Bases manage ingestion, chunking, embedding, storage, and retrieval, with `Retrieve` and `RetrieveAndGenerate` APIs.
- Citations let users verify answers against sources and are the practical mitigation for confident errors.
- Quality is mostly retrieval quality — chunking, embedding model, chunk count, metadata filtering, and source content.
- Changing the embedding model requires re-embedding everything, so evaluate it early.
- RAG handles poorly: aggregation, whole-corpus questions, multi-hop reasoning, and exact identifier lookup.
- The index is only as current as the last ingestion, making ingestion scheduling an operational concern.
- Agents let a model call tools, which addresses aggregation questions that retrieval cannot.
