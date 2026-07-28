package com.redmath.training.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rag")
public record RagProperties(
    @Positive int chunkMaxTokens,
    @Positive double similarityThreshold,
    @Positive int topK,
    String vectorStorePersistPath
) {

}
