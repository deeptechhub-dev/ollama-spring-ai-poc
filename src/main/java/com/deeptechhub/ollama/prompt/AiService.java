package com.deeptechhub.ollama.prompt;

import com.deeptechhub.ollama.prompt.dto.DesignReview;
import com.deeptechhub.ollama.prompt.dto.DesignReviewRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AiService {
    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final Validator validator;

    public AiService(ChatClient.Builder chatClientBuilder, ChatModel chatModel, Validator validator) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
        this.validator = validator;
    }

    public String chatBasic(String message) {
        return chatClient.prompt(message)
                .call()
                .content();
    }


    public String chat(String message) {
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage(
                                """
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
                                        """
                        ),
                        new UserMessage(message)
                )
        );

        return chatClient.prompt(prompt)
                .call()
                .content();

    }


    public String chatModel(String message) {
        Prompt prompt = new Prompt(
                new UserMessage(message)
        );

        ChatResponse response = chatModel.call(prompt);
        return response.getResult()
                .getOutput()
                .getText();
    }

    public String chatWithOptions(String message) {
        return chatClient.prompt()
                .user(message)
                .options(ChatOptions.builder())
                .call()
                .content();
    }

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
                .content();
    }

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

    public DesignReview designReviewValidStructured(String topic, String difficulty, String focus) {
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
        DesignReview review = responseSpec.entity(DesignReview.class);

        Set<ConstraintViolation<DesignReview>> validations = validator.validate(review);
        if(!validations.isEmpty()) {
            throw new IllegalArgumentException("Invalid AI response: " + validations);
        }

        return review;
    }

    public DesignReview doDesignReview(DesignReviewRequest request) {
        System.out.println("request= " + request);

        String template = """
                You are a senior software architect and system design interviewer.
                
                Generate a realistic system design interview scenario.

                Topic: <topic>
                Difficulty: <difficulty>
                Focus: <focus>

                Follow these examples.

                Example 1:

                Topic: URL shortener
                Difficulty: MEDIUM
                Focus: Scalability

                Scenario:
                Design a URL shortening service capable of handling high read traffic.

                Question:
                How would you generate unique short URLs while supporting horizontal scaling?


                Example 2:

                Topic: Payment service
                Difficulty: HARD
                Focus: Reliability

                Scenario:
                Design a payment processing service that remains reliable during network failures and duplicate requests.

                Question:
                How would you guarantee idempotency when processing payments?


                Generate a new scenario following the same style.

                Do not provide the solution.

                Ask exactly one architectural question.

               """;

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("topic", request.topic(), "difficulty", request.difficulty(), "focus", request.topic()))
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .build();

        Prompt prompt = promptTemplate.create();
        int maxAttempts = 3;
        IllegalArgumentException lastFailure = null;

        for(int attempt = 1; attempt <= maxAttempts; attempt++) {
            ChatClient.CallResponseSpec responseSpec = chatClient.prompt(prompt).call();

            System.out.println("Response=" + responseSpec.content());
            DesignReview review;

            try {
                review = responseSpec.entity(DesignReview.class);
            } catch (Exception e) {
                // e.g. malformed JSON from the model
                lastFailure = new IllegalArgumentException("Failed to parse AI response on attempt " + attempt, e);
                continue;
            }

            Set<ConstraintViolation<DesignReview>> validations = validator.validate(review);
            if(validations.isEmpty()) {
                return review;
            } else {
                lastFailure = new IllegalArgumentException("Invalid AI response: " + validations);
            }
        }

        throw lastFailure;
    }

}
