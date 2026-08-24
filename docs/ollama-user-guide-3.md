# Stateless vs stateful conversations

LLMs don't automatically know previous requests.
The default behaviour is Stateless i.e. each request is independent to LLM

```
Request 1 → LLM → Response

Request 2 → LLM → Response
```
For example - 

```
User: What is Kafka?
AI: Kafka is a distributed event streaming platform...
```
Then the user asks:

```
User: What are its main components?
```

The application sends only the second question. The model sees:

```
"What are its main components?"
```

It does not necessarily know that "its" refers to Kafka.


Applications can make the flow Stateful by retaining the previous messages and supplying to subsequent requests to make the LLM understand the context for accurate response.

``` 
Request
   ↓
Memory
   ↓
History + New Message
   ↓
LLM
   ↓
Response
   ↓
Memory
```

A conversation ID is used to separate one user's conversation from another.

```
conversationId
      ↓
retrieve history
      ↓
add new message
      ↓
call LLM
      ↓
store response
```

# Conversation History & Message Roles

## What is Conversation History?
A conversation is essentially a sequence of messages:

```
User
  ↓
Assistant
  ↓
User
  ↓
Assistant
  ↓
...
```

Example:

```
User: What is Kafka?

Assistant: Kafka is a distributed event streaming platform.

User: What are its main components?
```
## Message Roles

There are 3 important message types:

### System Message

Defines the AI's behaviour and instructions.

``` 
You are a senior software architect.
```

### User Message

Represents the user's input.

``` 
What is Apache Kafka?
```

### Assistant Message

Represents the model's previous response.

```
Apache Kafka is a distributed event streaming platform...
```

These forms the complete conversation:

```
System
   ↓
User
   ↓
Assistant
   ↓
User
   ↓
Assistant
```
For example, the application constructs a message history as follows:

```
System: You are a software architect.

User: What is Kafka?

Assistant: Kafka is...

User: What are its main components?
```
## Implementing a custom Conversation History
* Maintain an in-memory list of type List<Message> for storing conversation.
* On each LLM call, keep saving user message, and LLM response as UserMessage and AssistantMessage.
* Below is the code example that contains the implementation of the Service class:


```java
@Service
public class TempConversationService {
    private final ChatClient chatClient;
    private final List<Message> conversationHistory;

    public TempConversationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.conversationHistory = new LinkedList<>();
    }

    public String chatBasic(String message) {

        String response = chatClient.prompt()
                .messages(conversationHistory)
                .user(message)
                .system("You are a senior software architect and system design expert.")
                .call()
                .content();


        conversationHistory.add(new UserMessage(message));
        conversationHistory.add(new AssistantMessage(response));
        return response;
    }
}
```

With a custom in-memory conversation history, if we call the endpoint with the same questions, we will notice that the LLM is able to understand the context and gives more appropriate answers.


```
Question: What is Apache Kafka/

Response: Apache Kafka is a distributed event processing system designed to handle real-time data streams...

What are its main components?

Apache Kafka is a distributed event processing system, and its core components are:

1. **Producer**: A component that sends data to Kafka producers, which are used to send messages to a queue or topic.  
2. **Consumer**: A component that consumes data from a Kafka topic.  
3. **Broker**: A cluster of servers that store data in a distributed manner, ensuring data is replicated across multiple nodes.
```

# ChatMemory
The custom conversation history that we built is good for demonstrating the concept. But imagine a real application where we might have:

```
User A → Conversation 1
User A → Conversation 2
User B → Conversation 3
User B → Conversation 4
```
We need to answer questions such as:

* Where is each conversation stored?
* How do we retrieve a particular conversation?
* How many messages should we retain?
* How do we add new messages?
* How do we remove a conversation?
* What happens when the application restarts?
* Can we replace the storage mechanism later?

If every service implements these things itself, we end up with duplicated conversation-management logic.

This is the problem `ChatMemory` helps address.

ChatMemory is Spring AI's abstraction for managing conversational messages. 
With ChatMemory, applications don't need to know all the details of how the memory is managed.

For example:
```
Spring Data
    ↓
Repository abstraction
    ↓
Database implementation
```
Applications can work with a repository without carding about all the database implementation details.

Similarly:
```
Spring AI
    ↓
ChatMemory abstraction
    ↓
Memory implementation
```

## Basic operations of ChatMemory
* Store messages
* Retrieve messages
* Clear messages

Each conversation need an identifier to isolate from another. For example:
```
ChatMemory
    │
    ├── conversation-001
    │       ├── User
    │       ├── Assistant
    │       └── User
    │
    ├── conversation-002
    │       ├── User
    │       └── Assistant
    │
    └── conversation-003
            ├── User
            ├── Assistant
            └── User
```
Application can get or operate on a conversation using a conversation ID. 

```java
chatMemory.add(conversationId, messages);

List<Message> messages = chatMemory.get(conversationId);

chatMemory.clear(conversationId);
```

### Where does the actual memory live?
The actual storage is provided by an implementation.

Conceptually:
```
                  ChatMemory
                     │
          ┌──────────┴──────────┐
          │                     │
          ▼                     ▼
      In-memory              Persistent
       storage                storage
          │                     │
          ▼                     ▼
       JVM/Map              Database/Redis
```

## ChatMemory Implementation

Create an endpoint
```java
@RestController
@RequestMapping("/api")
public class ConversationController {
    private final AiService aiService;
    private final TempConversationService tempConversationService;
    private final ConversationMemoryService conversationMemoryService;

    public ConversationController(AiService aiService,
                                  ConversationMemoryService conversationMemoryService) {
        this.aiService = aiService;
        this.conversationMemoryService = conversationMemoryService;
    }

    @GetMapping("/conversation-memory")
    public String conversationMemory(
            @RequestParam String conversationId,
            @RequestParam String message
    ) {
        return conversationMemoryService.chatWithConversationHistory(conversationId, message);
    }
}
```

Add service implementation
```java
@Service
public class ConversationMemoryService {

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;
    private static final String SYSTEM_MESSAGE = "You are a senior software architect and a system design expert.";

    public ConversationMemoryService(ChatMemory chatMemory, ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
    }

    public String chatWithConversationHistory(String conversationId, String message) {
        // Get the existing conversation history
        List<Message> conversationHistory = chatMemory.get(conversationId);

        // Call the model with user message and conversation history
        String response = chatClient.prompt()
                .messages(conversationHistory)
                .system(SYSTEM_MESSAGE)
                .user(message)
                .call()
                .content();

        // Add to conversation history
        chatMemory.add(conversationId, new UserMessage(message));
        chatMemory.add(conversationId, new AssistantMessage(response));

        return response;
    }
}
```
### Testing the endpoint

Call the endpoint using GET `/api/conversation-memory?conversationId=kafka-001&message=What is Kafka?`

API response
```
Apache Kafka is a **distributed message system** developed by **Apache Software Foundation**. It is designed for **real-time data processing**, **high throughput** ...
```
Call the same endpoint to check the context using GET `/api/conversation-memory?conversationId=kafka-001&message=What are its main components?`

API response
```
The main components of Apache Kafka are:Brokers, Producers, Consumers....
```

Call the endpoint using a different conversationId. GET `/api/conversation-memory?conversationId=db-001&message=What is PostgreSQL?`

```
PostgreSQL is a powerful, open-source relational database management system (RDBMS) designed for modern applications. It was initially developed by Portland State University and later became a major open-source project...
```
Call the same endpoint to check the context using the different conversationId. GET `/api/conversation-memory?conversationId=db-001&message=What are its main features?`

```
PostgreSQL has several core components that make it a robust and flexible database system. Here are the main components...
```
From these results, we can see that the conversation history is being maintained.

Detailed code snippet can be found in the repository in conversation package.

# MessageWindowChatMemory
Imagine this conversation:
```
User → Message 1
AI   → Response 1

User → Message 2
AI   → Response 2

User → Message 3
AI   → Response 3

...

User → Message 100
AI   → Response 100
```
ChatMemory can keep accumulating messages. Then every new request potentially becomes:

```
Message 1
Message 2
Message 3
...
Message 100
+
Current message
```
and all of that context is sent to the model.

That raises an important question: Should we really send the entire conversation to the LLM every time?

Usually, no. This is where MessageWindowChatMemory becomes important.

So the memory becomes:

```
Oldest ───────────────────► Newest

[1] [2] [3] [4] [5] [6] [7] [8] [9] [10]
                          └───────────────┘
                              Window
```

As new messages arrive:

```
[2] [3] [4] [5] [6] [7] [8] [9] [10] [11]
                          └────────────────┘
```
This window moves forward.

Limiting the window size helps in reducing the conversational context and achieve faster/cheaper processing.

### Implementing MessageWindowChatMemory

Create a GET Endpoint

```
@GetMapping("/conversation-memory-limited")
    public String conversationMemoryLimited(
            @RequestParam String conversationId,
            @RequestParam String message
    ) {
        return conversationMemoryService.chatWithLimitedConversationHistory(conversationId, message);
    }

```

Implement the method in Service class

```
package com.deeptechhub.ollama.conversation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationMemoryService {

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;
    private final ChatMemory messageWindowChatMemory;
    private static final String SYSTEM_MESSAGE = "You are a senior software architect and a system design expert.";

    public ConversationMemoryService(ChatMemory chatMemory,
                                     ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
                .maxMessages(2).build(); // tiny window for testing
    }


    public String chatWithLimitedConversationHistory(String conversationId, String message) {
        // Get the existing conversation history
        List<Message> conversationHistory = messageWindowChatMemory.get(conversationId);

        // Call the model with user message and conversation history
        String response = chatClient.prompt()
                .messages(conversationHistory)
                .system(SYSTEM_MESSAGE)
                .user(message)
                .call()
                .content();

        // Add to conversation history
        messageWindowChatMemory.add(conversationId, new UserMessage(message));
        messageWindowChatMemory.add(conversationId, new AssistantMessage(response));

        return response;
    }
}
```

# Persistent Conversation Memory

In-memory conversations have several limitations. 
1. They are lost after the application restart.
2. When the application has multiple instances, the conversation stored in the memory of one instance won't be available to another instance. 

```
             Load Balancer
              /    |    \
             ▼     ▼     ▼
           App A App B App C
             │     │     │
           Memory Memory Memory
```

To make the conversations available even after the service restarts, conversation data needs to be persisted in persistence storages such as Database or Redis.

```
                    Spring Boot
                         │
                         ▼
                    ChatMemory
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
          Database                 Redis
```
Conversation Data example

```
conversation_id | role      | message
------------------------------------------------
kafka-001       | USER      | What is Kafka?
kafka-001       | ASSISTANT | Kafka is...
kafka-001       | USER      | What are its components?
```
# JDBC-based Chat Memory

Using JDBC based Chat memory, the chat history can be persisted in relational databases such as PostgreSQL, Oracle, H2, etc.
For simplicity, we will use H2 database with file based storage to survive application restarts.

## Steps to implement JDBC based Chat memory

Step-1: Add the required dependencies

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
</dependency>

<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-h2console</artifactId>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```
Step-2: Configure H2 as the file database in application.yaml
```
spring:
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always

  datasource:
    url: jdbc:h2:file:./data/chatmemory
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true

```
`url: jdbc:h2:file:./data/chatmemory` will make sure the data is stored in file system and not in-memory.

`initialize-schema: always` is needed when we want Spring AI to initialize the schema - `SPRING_AI_CHAT_MEMORY`. 

Step-3: Configure the beans

```java
@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository) // JDBC as the persistent storage
                .maxMessages(10) // Conversation window size
                .build();
    }
}
```
Step-4: Add a Service class

```java
@Service
public class PersistentConversationMemoryService {
    private final ChatMemory chatMemory;
    private final ChatClient chatClient;

    private static final String SYSTEM_MESSAGE = "You are a senior software architect and a system design expert.";

    public PersistentConversationMemoryService(ChatMemory chatMemory, ChatClient.Builder chatClientBuilder) {
        this.chatMemory = chatMemory;
        this.chatClient = chatClientBuilder.build();
    }

    public String chat(String conversationId, String message) {

        List<Message> conversationHistory = chatMemory.get(conversationId);

        String response = chatClient.prompt()
                .system(SYSTEM_MESSAGE)
                .messages(conversationHistory)
                .user(message)
                .call()
                .content();

        chatMemory.add(conversationId, new UserMessage(message));
        chatMemory.add(conversationId, new AssistantMessage(response));

        return response;
    }
}

```
The important part in the service is, it doesn't know that H2 exists. It only knows ChatMemory.
The storage chain is hidden behind the abstractions:

```
Service
  ↓
ChatMemory
  ↓
MessageWindowChatMemory
  ↓
JdbcChatMemoryRepository
  ↓
H2
```

Step-5: Add the endpoint
```java
@GetMapping("/conversation-memory-jdbc")
public String conversationMemoryJdbc(
        @RequestParam String conversationId,
        @RequestParam String message
) {
    return persistentConversationMemoryService.chat(conversationId, message);
}
```
Step-6: Start the application and test by calling the endpoints using the steps mentioned in ChatMemory section. 
Additionally, test by application restart to verify persistency after application restarts.

Persisted Data can also be viewed using H2 console - http://localhost:8080/h2-console since it has been enabled. 

# Memory Limits, Context Windows & LLM Context

Storing conversation history is not the same thing as sending conversation history to the LLM. Suppose our persistent database contains:

```
conversationId = kafka-001

Message 1
Message 2
Message 3
...
Message 1000
```

Our application has successfully persisted all 1,000 messages.  

Does that mean the LLM receives all 1,000 messages when the user asks: Most probably No

The application decides what portion of the stored conversation becomes the LLM's context.

## There different concepts

### A. Stored history
Everything we've persisted
```
Database
   ↓
Message 1
Message 2
...
Message 1000
```

### B. Memory window
The subset selected by our memory implementation:
```
Message 991
Message 992
...
Message 1000
```

### C. LLM context
What is actually included in the request sent to the model

```
System message
+
Selected conversation messages
+
Current user message
```
So:

```
┌───────────────────────────────┐
│ Persistent Conversation       │
│                               │
│ 1 ... 1000                    │
└──────────────┬────────────────┘
               │
               │ select
               ▼
┌───────────────────────────────┐
│ Memory Window                 │
│                               │
│ 991 ... 1000                  │
└──────────────┬────────────────┘
               │
               │ build prompt
               ▼
┌───────────────────────────────┐
│ LLM Context                   │
│                               │
│ System                        │
│ Messages 991...1000           │
│ Current user message          │
└──────────────┬────────────────┘
               │
               ▼
             Ollama
```

### Example using MessageWindowChatMemory
Suppose database contains ten messages.

```
MessageWindowChatMemory.builder()
        .maxMessages(2)
        .build();
```
But our memory window would contain last 2 messages

So when we make the LLM call:

```
List<Message> history = chatMemory.get(conversationId);
```
we don't necessarily get everything stored in the database. We get the memory-selected context.

### Why can't we send everything?

Because LLMs have a context window. There is a maximum amount of input the model can process. 
The exact limit depends on the model.

The LLM context may contain several other data apart from conversation history

```
┌──────────────────────────────┐
│ System instructions          │
├──────────────────────────────┤
│ Conversation history         │
├──────────────────────────────┤
│ Retrieved documents          │
├──────────────────────────────┤
│ Tool information/results     │
├──────────────────────────────┤
│ Current user message         │
└──────────────────────────────┘
```
# Short-term vs Long-term Memory

So far, we've been dealing primarily with conversation history. For example:

```
conversationId = kafka-001

User:
What is Kafka?

Assistant:
Kafka is an event streaming platform.

User:
What are its main components?

Assistant:
...
```

This is useful, but there's a fundamental limitation:

> Conversation history is not necessarily the same thing as knowledge about the user.

Consider this conversation:

```
User:
I'm building a payment service using Spring Boot.

Assistant:
Great...

...many messages later...

User:
What database should I use?
```
Do we really want the application to reconstruct that fact by scanning thousands of previous conversation messages? - Probably not.

We may want to store important information separately:

```
User preferences / facts
        ↓
Long-term memory
```
## Short-term memory

Short-term memory represents the **current conversational context**.
For example:
```
User:
What is Kafka?

Assistant:
Kafka is...

User:
What are its main components?

Assistant:
The main components are...

User:
Which one provides scalability?
```
The last question depends on the immediately preceding conversation. That's exactly what our ChatMemory is good at.

## Long-term memory

Long-term memory represents information that should potentially remain useful across conversations. 

For example:

```
User
 │
 ├── prefers Java examples
 ├── is building a Spring Boot application
 ├── prefers concise explanations
 └── interested in system design
```

## Short-term vs Long-term vs RAG
* Short-term memory: What happened in this conversation?
* Long-term memory: What useful information do we know about this user/application?
* RAG: What relevant external knowledge should we retrieve?

For example

```
                 LLM
                  ▲
                  │
        ┌─────────┼──────────┐
        │         │          │
        │         │          │
 Short-term   Long-term     RAG
 memory       memory        knowledge
        │         │          │
        ▼         ▼          ▼
    Current     User       Documents /
    chat       facts       knowledge base
```

## Memory lifecycle

Long term memory also requires memory management as time to time information may change.

```
Create -> Update -> Supersede -> Delete
```