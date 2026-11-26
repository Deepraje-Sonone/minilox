package com.minilox.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void validCodeExecutesSuccessfully() throws Exception {
        mockMvc.perform(post("/api/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "print 1 + 1;"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.output[0]").value("2"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void syntaxErrorReturnsFailedResult() throws Exception {
        mockMvc.perform(post("/api/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "let = ;"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void runtimeErrorReturnsFailedResult() throws Exception {
        mockMvc.perform(post("/api/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "print unknownVariable;"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void missingCodeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankCodeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}