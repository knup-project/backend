package com.knupbackend.ai.dto.response;

public record AiExplainResponse(
        String explanation,
        String hint,
        String generatedAt
) {}
