
# Prompt Engineering & Structured Output

## Prompt fundamentals
Prompt is an input to an AI model which not just includes the question but also several other parameters:

``` 
Prompt
│
├── Context
├── Instructions
├── Constraints
├── Examples (optional)
└── User input
```
Example
```text
You are an experienced Java architect.
Explain HashMap to a senior Java developer.
Focus on internal implementation, collision handling,
time complexity and Java 8+ improvements.
Keep the answer under 150 words.

What is a HashMap?
```
Prompt didn't just include the question but some additional information.

```text
Role
 ↓
Audience
 ↓
Scope
 ↓
Constraints
 ↓
Question
```
## The 5-part prompt structure
For many AI applications, a useful starting point is:

```text
ROLE
CONTEXT
TASK
CONSTRAINTS
OUTPUT FORMAT
```
For example

```text
# System Design Review Prompt

You are a senior software architect and system design reviewer.

## ROLE
Assess the user's understanding of software architecture and their ability to design scalable, reliable, and maintainable systems.

## CONTEXT
The user is an experienced software developer who wants to improve their practical system design and architectural decision-making skills.

## TASK
Present a realistic software or system design scenario and evaluate the user's proposed solution.

## CONSTRAINTS
* Focus on scalability, reliability, performance, security, and maintainability.
* Prefer realistic scenarios over theoretical questions.
* Encourage the user to explain their architectural decisions and trade-offs.
* Do not immediately provide the ideal solution.
* Ask one scenario or follow-up question at a time.
* Adapt the difficulty based on the user's answers.
* Keep responses concise.

## OUTPUT FORMAT
For each scenario, provide:

Scenario:
Difficulty:
Focus Areas:
```

## Prompt Templates
Prompt template helps in generating prompts dynamically. This is an essential feature for real application.

A template contains placeholders:
```text
You are a senior software architect.

Present a <difficulty> system design scenario
about <topic>.

Focus particularly on <focus>.
```
Then our application supplies:
```text
topic      = URL shortener
difficulty = Medium
focus      = Scalability
```
Conceptually:
```
Template
   │
   ├── <topic>
   ├── <difficulty>
   └── <focus>
   │
   ▼
Resolved Prompt
   │
   ▼
ChatClient
   │
   ▼
  LLM
```
### Implementing Prompt Template using Spring AI

Add a GET API endpoint
```java
@GetMapping("/design-review")
public String designReview(
        @RequestParam String topic,
        @RequestParam String difficulty,
        @RequestParam String focus) {
    return aiService.designReview(topic, difficulty, focus);
}
```

Add a Service method to build prompt, call LLM and return response

```java
public String designReview(String topic, String difficulty, String focus) {
    String template = """
            You are a senior software architect and system design reviewer.
            
            Assess the user's understanding of software architecture.
            
            Present a realistic <difficulty> system design scenario
            about <topic>.
            
            Focus particularly on:
            <focus>
            
            Do not provide the ideal solution.
            Ask one question at a time.
            Keep the response concise.
            
            Output:
            
            Scenario:
            Difficulty:
            Focus Areas:
            """;

    PromptTemplate promptTemplate = PromptTemplate.builder()
            .template(template)
            .variables(Map.of("topic", topic, "difficulty", difficulty, "focus", focus))
            .build();

    return chatClient.prompt(promptTemplate.create())
            .call()
            .chatResponse()
            .getResult()
            .getOutput()
            .getText();
}
```
Call the endpoint by configuring the topic, difficulty, and focus such as GET http://localhost:8080/design-review?topic=URL%20shortener&difficulty=Medium&focus=Scalability

## Few shot prompting
In "Few shot prompting", instead of asking the model what we want, we show it some examples.
This approach infers the desired pattern from examples.

Consider this sentiment classification example:

``` 
Input: I absolutely loved this product.
Output: POSITIVE

Input: The product stopped working after one day.
Output: NEGATIVE

Input: The product arrived yesterday.
Output: NEUTRAL

Input: The customer service was fantastic.
Output:
```
There are multiple types of Few-shots:
* Zero-shot: No examples
* One-shot: One example
* Few-shot: Multiple examples

### Few shots implementation example

Add an endpoint
```java
@GetMapping("/design-review-few-shots")
public String designReviewFewShots(
        @RequestParam String topic,
        @RequestParam String difficulty,
        @RequestParam String focus) {
    return aiService.designReviewFewShot(topic, difficulty, focus);
}
```

Then add a service method

```java
public String designReviewFewShot(String topic, String difficulty, String focus) {
    String fewShotTemplate = """
            You are a senior software architect and system design reviewer.
            
            Generate a realistic system design scenario.
            
            Here are examples of the expected style.
            
            Example 1:
            
            Topic: URL shortener
            Difficulty: Medium
            Focus: Scalability
            
            Scenario:
            Design a URL shortening service capable of handling high read traffic.
            
            Question:
            How would you generate unique short URLs while supporting horizontal scaling?
            
            
            Example 2:
            
            Topic: Payment service
            Difficulty: Hard
            Focus: Reliability
            
            Scenario:
            Design a payment processing service that remains reliable during
            network failures and duplicate requests.
            
            Question:
            How would you guarantee idempotency when processing payments?
            
            
            Now generate a scenario for:
            
            Topic: <topic>
            Difficulty: <difficulty>
            Focus: <focus>
            
            Do not provide the solution.
            Ask one question at a time.
            """;

    PromptTemplate promptTemplate = PromptTemplate.builder()
            .template(fewShotTemplate)
            .variables(Map.of("topic", topic, "difficulty", difficulty, "focus", focus))
            .build();

    return chatClient.prompt(promptTemplate.create())
            .call()
            .content();

}

```
* Test by calling the endpoint using an example such as GET http://localhost:8080/design-review-few-shots?topic=Real-time%20chat%20application&difficulty=Hard&focus=Reliability%20and%20message%20delivery

Sample result

``` 
Question: What design principles should be applied to ensure a real-time chat application remains reliable during high traffic and guarantees message delivery accuracy in the face of potential disruptions?
```

## Output Constraints
Output constraints in a prompt enable model to produce the output in a desirable way.

For example if we want the result only in Json format, the code can be written as follows:

Example code
```java
@GetMapping("/design-review-json")
public String designReviewJson(
        @RequestParam String topic,
        @RequestParam String difficulty,
        @RequestParam String focus) {
    return aiService.designReviewJson(topic, difficulty, focus);
}

public String designReviewJson(String topic, String difficulty, String focus) {
        String template = """
               You are a senior software architect.
               Generate a system design scenario about a <topic>.
               Focus particularly on: <focus> and difficulty: <difficulty>

               Return ONLY valid JSON.

               The JSON must contain exactly these fields:

               {
                 "scenario": "String",
                 "difficulty": "String",
                 "question": "String"
               }

               Do not include markdown.
               Do not include ```json.
               Do not include explanations.
               """;

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("topic", topic, "difficulty", difficulty, "focus", focus))
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .build();

        return chatClient.prompt(promptTemplate.create())
                .call()
                .content();

    }
```
Calling the endpoint using http://localhost:8080/design-review-json?topic=URL%20shortener&difficulty=Medium&focus=Scalability can produce the response as follows:

``` 
{
  "scenario": "A real-time data processing system that prioritizes latency and scalability, with a focus on optimizing data pipeline throughput and caching strategies. The system is designed to handle high traffic volumes by leveraging distributed computing and adaptive caching, while the difficulty is set at medium level due to the integration of real-time analytics and dynamic resource allocation mechanisms.",
  "difficulty": "medium",
  "question": "How does the system handle real-time data processing with fluctuating traffic demands and ensure optimal latency while minimizing operational complexity?"
}
```

Output constraints can help applications build complex system

```
                   ┌──────────────────┐
                   │  Prompt Template │
                   └────────┬─────────┘
                            │
                            ▼
                     ┌─────────────┐
                     │     LLM     │
                     └──────┬──────┘
                            │
                            ▼
                       JSON output
                            │
                            ▼
                   ┌─────────────────┐
                   │ Java deserialization │
                   └────────┬────────┘
                            │
                            ▼
                       Java Record
                            │
                            ▼
                        Validation
                            │
                     ┌──────┴──────┐
                     │             │
                   Valid         Invalid
                     │             │
                     ▼             ▼
                Business       Error/
                 logic        retry/fallback 
```

## Structured output
* Structured Output is one of the most powerful feature of Spring AI which enables transforming the output in desirable format which the application can use in its business logics.
* This feature transitions

```
             BEFORE

Prompt
  ↓
LLM
  ↓
String
  ↓
"Hopefully valid JSON"
```
to:

```
             AFTER

Prompt
  ↓
LLM
  ↓
Structured output
  ↓
Java object
```

### Implementing Structured Output

Create a Java model

```java
public record DesignReview(
    String scenario,
    String difficulty,
    String question
) { }
```

Create an API endpoint

```java
@GetMapping("/design-review-structured")
public DesignReview designReviewStructured(
        @RequestParam String topic,
        @RequestParam String difficulty,
        @RequestParam String focus) {
    return aiService.designReviewStructured(topic, difficulty, focus);
}
```
Create a service method to handle it

```java
public DesignReview designReviewStructured(String topic, String difficulty, String focus) {
    String template = """
            You are a senior software architect.

            Generate a system design scenario about <topic>.

            Focus particularly on <focus>.
            Difficulty: <difficulty>.

            Provide:
            - a realistic scenario
            - the difficulty
            - one architectural question

            Do not provide the solution.
           """;

    PromptTemplate promptTemplate = PromptTemplate.builder()
            .template(template)
            .variables(Map.of("topic", topic, "difficulty", difficulty, "focus", focus))
            .renderer(StTemplateRenderer.builder()
                    .startDelimiterToken('<')
                    .endDelimiterToken('>')
                    .build())
            .build();

    ChatClient.CallResponseSpec responseSpec = chatClient.prompt(promptTemplate.create())
            .call();

    System.out.println("Response=" + responseSpec.content());

    return responseSpec
            .entity(DesignReview.class);

}
```
Testing the endpoint produces following results

* Test case-1
```
GET http://localhost:8080/design-review-structured?topic=URL%20shortener&difficulty=Medium&focus=Scalability

{
    "scenario": "A global e-commerce platform must handle 10 million daily requests while maintaining uptime across 50 data centers...",
    "difficulty": "Moderate",
    "question": "Design a redundant data center architecture to ensure business continuity during high-traffic events, considering both horizontal and vertical redundancy."
}
```
The response logged in the service prints

```
Response=**Scenario:**  
A real-time data streaming platform must handle millions of concurrent requests while maintaining near-zero latency and 99.99% uptime...

**Difficulty:** Moderate  
**Architectural Question:**  
How can we design a system that scales horizontally to handle 10,000 concurrent queries while maintaining 99.99% uptime and 99.999% latency, using a fault-tolerant approach and minimizing downtime?

---

**Note:** This scenario is designed to highlight the integration of horizontal scaling, fault tolerance, and microservices with real-world challenges...
```
You will notice that although the logged response is not in desirable format, API response is in because Spring AI has structured it.

Structured Output guarantees structure but not correctness. If you look at the results:

we asked for:

```
topic = URL shortener
difficulty = Medium
focus = Scalability
```
but we got

```
scenario = global e-commerce platform
difficulty = Moderate
```

So we have a structured object, but not necessarily a correct object. That gives us our next principle:

> Deserialization guarantees structure, not semantic correctness.

## Type Safety with Java Enums

Type safety ensures that only valid values will be returned in the API response for the fields with finite set of values.

### Implementing Type safety

In the previous example, we can make some small changes to achieve this.

Create an enum Difficulty and use that in the DesignReview model.

```java
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD
}

public record DesignReview(
        String scenario,
        Difficulty difficulty,
        String question
) { }
```

Test it by calling the API endpoint GET http://localhost:8080/design-review-structured?topic=URL%20shortener&difficulty=Medium&focus=Scalability

We get the response as follows:

```
{
    "scenario": "A microservices-based cloud platform must handle real-time user authentication, requiring distributed consensus protocols to maintain consistent session states across multiple service instances.",
    "difficulty": "MEDIUM",
    "question": "How can a distributed system ensure real-time consensus over multiple nodes in a high-latency environment?"
}
```

## Bean Validation

An enum gives allowed values but enum can only be used for fields which has finite and fixed values.
Example, enum cannot be used for:

```
scenario = ""
question = ""
```
For these fields, we can use bean validation

### Implementing Bean Validation

* Add `spring-boot-starter-validation` as the maven dependency.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
* Add required annotations on the model 

```java
public record DesignReview(
    @NotBlank
    String scenario,

    @NotNull
    Difficulty difficulty,

    @NotBlank
    String question
) { }
```
* Add the validation in the service class

```
DesignReview review = responseSpec.entity(DesignReview.class);

Set<ConstraintViolation<DesignReview>> validations = validator.validate(review);
if(!validations.isEmpty()) {
    throw new IllegalArgumentException("Invalid AI response: " + validations);
}

return review;
```

In the code repository, a new API endpoint `/design-review-valid-structured` can be found for detailed implementation.

Now our AI pipeline becomes:

```
Prompt
   ↓
LLM
   ↓
Structured Output
   ↓
DesignReview
   ↓
Bean Validation
   ↓
┌───────────────┐
│ Valid?        │
└───────┬───────┘
        │
    ┌───┴───┐
    ▼       ▼
  YES       NO
   │         │
   ▼         ▼
 Return    Reject
```

## Handling Invalid AI Output
When AI output is invalid, we can handle it using 4 different strategy

1. Reject

``` 
LLM → invalid → error
```

Simple and appropriate when incorrect AI output must never reach the business logic.

2. Retry

```
LLM → invalid
       ↓
      retry
       ↓
      LLM
```

Potentially useful, but increases latency and model usage.

3. Ask the model to correct itself

```
LLM
 ↓
invalid output
 ↓
"Your previous response was invalid because..."
 ↓
LLM
 ↓
corrected output
```

Interesting, but we should not blindly trust the second response either. Also, add a limit for max retries

```
Attempt 1
   ↓
Invalid
   ↓
Attempt 2
   ↓
Invalid
   ↓
Attempt 3
   ↓
Invalid
   ↓
FAIL
```

4. Fallback

```
LLM → invalid
       ↓
fallback
       ↓
deterministic application behaviour
```
This is often the safest option for critical functionality. So eventual flow becomes

```
                  Request
                     ↓
                   Prompt
                     ↓
                    LLM
                     ↓
              Structured output
                     ↓
                Validation
                     ↓
              ┌──────┴──────┐
              │             │
            Valid         Invalid
              │             │
              ▼             ▼
            Return       Retry?
                            │
                       ┌────┴────┐
                       │         │
                      Yes        No
                       │         │
                       ▼         ▼
                      LLM       Error
```

In the code repository, you can find a new POST API endpoint `/design-review` which demonstrates all the key concepts covered so far.
