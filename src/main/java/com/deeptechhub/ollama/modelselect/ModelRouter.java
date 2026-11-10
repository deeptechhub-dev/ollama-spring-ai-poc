package com.deeptechhub.ollama.modelselect;

import org.springframework.stereotype.Component;

@Component
public class ModelRouter {

    public String selectModel(ModelType modelType) {
        return switch (modelType) {
            case GENERAL -> "qwen3:0.6b";
            case VISION -> "qwen3-vl:2b-instruct";
        };
    }
}
