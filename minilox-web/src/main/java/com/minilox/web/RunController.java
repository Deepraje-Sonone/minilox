package com.minilox.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of("status", "ok")
        );
    }

    @PostMapping("/run")
    public ResponseEntity<?> run(
            @RequestBody(required = false) RunRequest request
    ) {
        if (request == null
                || request.code() == null
                || request.code().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error",
                            "Code must not be empty."
                    ));
        }

        RunResponse response = runService.execute(request.code());

        return ResponseEntity.ok(response);
    }
}