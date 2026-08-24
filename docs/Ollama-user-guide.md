
# Ollama Setup

## Download and install Ollama

* Download Ollama from Ollama official website's download page https://ollama.com/download/ and install.
* Verify that Ollama is installed using `ollama --version`
* Verify that Ollama server is up and running using command `ollama list`

You may see something like

```
NAME    ID    SIZE    MODIFIED
```

with no models listed.

## Download your first models

* Ollama https://ollama.com/library?sort=newest has a huge collection of models. For RAM size of 16GB or higher "Qwen3 0.6B" is a good model to for general purpose.
* Download model "Qwen3 0.6B" model:

```
ollama pull qwen3:0.6b
```

## Talk with the AI

* Run `ollama run qwen3:0.6b`


## Verify the model
* Exit the interactive Qwen session by pressing (Ctrl + D) then run following command: `ollama list`. You will see the output as follows:

```
NAME          ID              SIZE      MODIFIED
qwen3:0.6b    7df6b6e09427    522 MB    14 hours ago
```

* If you run `ollama ps`, you should see output as follows:

```
NAME    ID    SIZE    PROCESSOR    CONTEXT    UNTIL
```

This shows following:

ollama list → What models do I have?
ollama ps → What models are running right now?


* Run `ollama ps` in another terminal while running the ollama model using command `ollama run qwen3:4b`. You would see something as follows:

```
NAME          ID              SIZE      PROCESSOR    CONTEXT    UNTIL
qwen3:0.6b    7df6b6e09427    956MB     100% CPU     4096       4 minutes from now
```

You would notice that the running model size(956MB) is higher than the downloaded model (522MB) when it is running. This is normal as running model requires additional memory.


* We have so far seen the following lifecycle of the model:

```
                DISK
                 │
                 │ ollama pull
                 ▼
           ┌───────────┐
           │ Qwen3 4B  │
           │  2.5 GB   │
           └─────┬─────┘
                 │
                 │ ollama run
                 ▼
              MEMORY
                 │
                 ▼
          ┌─────────────┐
          │ Qwen3 4B    │
          │ 3.2 GB      │
          │ 100% CPU    │
          │ Context 4096│
          └─────────────┘
```

# Talk to Ollama through it's REST API

So far you've interacted with Ollama like this:

```
You
 │
 │  ollama run qwen3:4b
 ▼
Ollama CLI
 │
 ▼
Qwen3 4B
```

But applications don't normally execute the CLI.

Instead, they can communicate with Ollama through its HTTP API:

```
Java / Spring Boot application
          │
          │ HTTP
          ▼
   Ollama REST API
          │
          ▼
       Qwen3 4B
	   
```

* Keep your current `ollama run qwen3:4b` session running.

* Send a POST request to http://localhost:11434/api/generate using following request body:

```json
{
  "model": "qwen3:4b",
  "prompt": "Explain Java Virtual Machine in one sentence.",
  "stream": false
}

```
* You will see output as follows:

```
model                : qwen3:4b
created_at           : 2026-08-19T15:40:45.6592598Z
response             : The Java Virtual Machine (JVM) is a platform-independent runtime environment that executes Java
                       bytecode, enabling Java applications to run consistently across any hardware or operating
                       system.
thinking             : We are to explain the Java Virtual Machine (JVM) in one sentence.
                        The JVM is a virtual machine that enables Java code to run on any device, regardless of the
                       underlying hardware or operating system.
                        ....

                        I'll output:

                        "The Java Virtual Machine (JVM) is a virtual machine that enables Java code to run on any
                       hardware or operating system platform regardless of the underlying system."

done                 : True
done_reason          : stop
context              : {151644, 872, 198, 840...}
total_duration       : 245196795700
load_duration        : 445681100
prompt_eval_count    : 19
prompt_eval_duration : 301815000
eval_count           : 1500
eval_duration        : 244430927000
```

In the response the following keys are important:

- prompt_eval_count: Token used to process the input
- eval_count: Tokens used to process the output
- load_duration: Time spent loading the model
- prompt_eval_duration: Time evaluating the prompt before processing
- eval_duration: Time spent generating tokens
- total_duration: Total execution time
- context: The list of tokens. Length = prompt_eval_count + eval_count

Most of the keys in these key-value pairs are obvious from the names.

## Relationship between Ollama, the model, and the REST API

```
┌──────────────────────────┐
│       Your PC            │
│                          │
│  ┌────────────────────┐  │
│  │ Ollama Server      │  │
│  │ localhost:11434    │  │
│  └─────────┬──────────┘  │
│            │             │
│            ▼             │
│       ┌─────────┐        │
│       │ Qwen3 4B│        │
│       └─────────┘        │
│                          │
└──────────────────────────┘
          ▲
          │ HTTP
          │
     Your application

```

## Comparison of Ollama REST API responses

* If you make POST API calls to the http://localhost:11434/api/generate endpoint one by one using the following payloads

payload-1

```
{
  "model": "qwen3:4b",
  "prompt": "What is a Java interface?",
  "stream": false
}
```
payload-2

```
{
  "model": "qwen3:4b",
  "prompt": "Reply with exactly three words.",
  "stream": false
}
```

You will notice following in the response:

|                  | Java interface | Three words |
| ---------------- | -------------: | ----------: |
| Total time       |       ~584 sec | **~40 sec** |
| Model load       |       ~5.4 sec |    ~7.2 sec |
| Generated tokens |          2,510 |     **210** |
| Generation time  |       ~578 sec | **~32 sec** |

* Execution time increases as number of tokens increases.

## Customised processing and response
* Model thinking time can be reduced by passing `"think":false` in the payload.
* In the above example, using the `"think":false` in the payload

``` 
{
  "model": "qwen3:4b",
  "prompt": "What is a Java interface?",
  "stream": false,
  "think": false
}
```
results in the following:
```text
total_duration      ≈ 348.3 seconds  → 5 min 48 sec
load_duration       ≈   1.6 seconds
eval_count          = 1615 tokens
eval_duration       ≈ 346.5 seconds
```
So think: false helped considerably - from ~9:43 down to ~5:48 — but it's still quite slow.

# Ollama Spring AI integration

## Create a Spring-boot project
* Create a Spring boot maven project "ollama-spring-ai-poc" with the following dependencies:
  * spring web
  * devtools

* Update application.yaml

```yaml
spring:
  application:
    name: ollama-spring-ai-poc
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: qwen3:4b
```

* Add a GET API endpoint to send prompt to Ollama model and return the response
```java
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt(message)
                .call()
                .content();
    }
}

```

* Verify the result by calling the endpoint http://localhost:8080/chat?message=What%20is%20Java%20Virtual%20Machine? from any browser.

## ChatClient
ChatClient is a high-level interface for communicating with an AI model. 
Applications don't need to care whether the model is:
```
Ollama
OpenAI
Anthropic
Azure OpenAI
...
```
The application talks to `ChatClient`.

* ChatClient.Builder: This allows to create ChatClient using Spring AI's builder mechanism.
Spring boot automatically configures the builder based on the configurations in application.yaml.

* .prompt(message): This establishes what we want to send to the model.
* .call(): This actually executes the interaction with the model.
* .content(): This extracts the actual textual response from the AI result.

### Set System behaviour and instructions
* System behaviour and instructions can be set using `system()` in the ChatClient.
Below is an example:

```java
import org.springframework.ai.chat.model.ChatResponse;

@GetMapping("/chat")
public String chat(@RequestParam String message) {

    return chatClient.prompt()
            .system("""
                    You are an expert Java software architect.
                    Always consider scalability, maintainability and security.
                    Prefer modern Java features.
                    Provide production-quality code.
                    """)
            .user(message)
            .call()
            .content();
}
```
### Get response content with ChatResponse
* ChatResponse can be retrieved using
```java
ChatResponse response = chatClient.prompt()
        .system("""
                Answer very briefly.
                """)
        .user(message)
        .call()
        .chatResponse();
```
Then we can get some important info in the response with methods such as
```
response.getResult()
or 
response.getMetadata()
```

## ChatModel
* While ChatClient is a high level developer friendly API, ChatModel is a lower-level abstraction.
* `ChatClient` ultimately delegates model interaction to the configured `ChatModel` implementation.
* Below example shows how ChatModel can be used to call the model

```java
@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    public ChatController(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
    }

    @GetMapping("/chat-model")
    public String chatModel(@RequestParam("message") String message) {
        Prompt prompt = new Prompt(
                new UserMessage(message)
        );

        ChatResponse response = chatModel.call(prompt);
        return response.getResult()
                .getOutput()
                .getText();
    }
}
```
* TODO: [ChatClient] vs [ChatModel] using figure

* Why does Spring AI needs both? Because, it needs a common abstraction for different providers

```text
                ChatModel
                   │
        ┌──────────┼──────────┐
        │          │          │
   Ollama model  OpenAI     Anthropic
        │          │          │
     Ollama      API        API
```

## ChatOptions
Model behaviour can be controlled using ChatOptions.

```java
@GetMapping("/chat-with-options")
public String chatWithOptions(@RequestParam String message) {
    return chatClient.prompt()
            .user(message)
            .options(ChatOptions.builder().temperature(1.0))
            .call()
            .content();
}
```
Common parameters include

| Option        | Purpose                                      |
| ------------- | -------------------------------------------- |
| `temperature` | Controls randomness/creativity               |
| `maxTokens`   | Limits generated output                      |
| `model`       | Selects the model                            |
| `topP`        | Controls probability sampling                |
| `topK`        | Controls candidate selection for some models |

temperature = 0 means more deterministic. temperature = 1 means more randomness