package com.minilox.web;

import com.minilox.MiniLoxRunner;
import com.minilox.RunResult;
import com.minilox.interpreter.RunLimits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RunService {

    private final MiniLoxRunner runner;

    public RunService() {
        this.runner = new MiniLoxRunner(RunLimits.playground());
    }

    public RunResponse execute(String code) {

        long startTime = System.nanoTime();

        List<String> output = new ArrayList<>();

        RunResult result = runner.run(code, output::add);

        long durationMs =
                (System.nanoTime() - startTime) / 1_000_000;

        return new RunResponse(
                result.ok(),
                output,
                result.errors(),
                durationMs
        );
    }
}