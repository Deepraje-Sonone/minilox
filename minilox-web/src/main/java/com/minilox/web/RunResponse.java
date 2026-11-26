package com.minilox.web;

import com.minilox.error.Diagnostic;

import java.util.List;

public record RunResponse(
        boolean ok,
        List<String> output,
        List<Diagnostic> errors,
        long durationMs
) {
    public RunResponse {
        output = List.copyOf(output);
        errors = List.copyOf(errors);
    }
}