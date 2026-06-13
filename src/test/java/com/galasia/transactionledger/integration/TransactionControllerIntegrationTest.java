package com.galasia.transactionledger.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("PUT /transactions/{id} should create a transaction and return status ok")
    void shouldCreateTransaction() throws Exception {
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000, "type": "cars"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @DisplayName("GET /transactions/types/{type} should return transaction ids for the given type")
    void shouldReturnTransactionIdsByType() throws Exception {
        // Given: two transactions of type "shopping"
        mockMvc.perform(put("/transactions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 10000, "type": "shopping"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/transactions/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000, "type": "shopping"}
                                """))
                .andExpect(status().isOk());

        // When & Then: querying by type returns both ids
        mockMvc.perform(get("/transactions/types/shopping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value(11))
                .andExpect(jsonPath("$[1]").value(12));
    }

    @Test
    @DisplayName("GET /transactions/sum/{id} should return the transaction amount when it has no children")
    void shouldReturnSumForSingleTransaction() throws Exception {
        // Given: a single transaction
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000, "type": "cars"}
                                """))
                .andExpect(status().isOk());

        // When & Then: sum is just its own amount
        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(5000.0));
    }
}
