package com.galasia.transactionledger.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.galasia.transactionledger.model.Transaction;
import com.galasia.transactionledger.repository.TransactionRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

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
    @DisplayName("GET /transactions/types/{type} should return a single transaction id when only one exists for the given type")
    void findIdsByTypeReturnsIdWhenOneTransactionOfThatType() throws Exception {
        // Given: one transaction of type "groceries"
        mockMvc.perform(put("/transactions/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 2500, "type": "groceries"}
                                """))
                .andExpect(status().isOk());

        // When & Then: querying by type returns exactly one id
        mockMvc.perform(get("/transactions/types/groceries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value(30));
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

    @Test
    @DisplayName("GET /transactions/sum/{id} should return transitive sum including children")
    void shouldReturnTransitiveSumIncludingChildren() throws Exception {
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000, "type": "cars"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/transactions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 10000, "type": "shopping", "parent_id": 10}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/transactions/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000, "type": "shopping", "parent_id": 11}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20000.0));

        mockMvc.perform(get("/transactions/sum/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(15000.0));
    }

    @Test
    @DisplayName("PUT /transactions/{id} should return error when parent_id does not exist")
    void shouldReturnErrorWhenParentIdDoesNotExist() throws Exception {
        mockMvc.perform(put("/transactions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 10000, "type": "shopping", "parent_id": 999}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /transactions/sum/{id} should return 404 when transaction does not exist")
    void shouldReturn404WhenTransactionNotFound() throws Exception {
        mockMvc.perform(get("/transactions/sum/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /transactions/{id} should return 409 Conflict when transaction already exists")
    void shouldReturnConflictWhenTransactionAlreadyExists() throws Exception {
        // Create it once
        mockMvc.perform(put("/transactions/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000, "type": "cars"}
                                """))
                .andExpect(status().isOk());

        // Try to create it again with the same ID
        mockMvc.perform(put("/transactions/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 10000, "type": "shopping"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /transactions/types/{type} should return empty list when no transactions of that type exist")
    void findIdsByType_returnsEmptyList_whenNoTransactionsOfThatType() throws Exception {
        mockMvc.perform(get("/transactions/types/nonexistent_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /transactions/sum/{id} should not infinite loop if a cyclic reference exists in the data")
    void sumSubtree_doesNotInfiniteLoop_onCyclicParentReferences() throws Exception {
        // Since the API prevents creating cycles (parent must exist and transactions are immutable),
        // we must inject the corrupted data directly into the repository to test the recursion safety.
        Transaction tx1 = new Transaction(100L, 10.0, "cycle", 101L);
        Transaction tx2 = new Transaction(101L, 20.0, "cycle", 100L);
        transactionRepository.save(tx1);
        transactionRepository.save(tx2);

        // It should return a valid sum (10.0 + 20.0 = 30.0) breaking the cycle and NOT StackOverflow.
        mockMvc.perform(get("/transactions/sum/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(30.0));
    }

    @Test
    @DisplayName("GET /transactions/sum/{id} should correctly sum multiple siblings (children of the same parent)")
    void sumSubtreeIncludesMultipleSiblings() throws Exception {
        // Parent: 200 (amount: 100)
        mockMvc.perform(put("/transactions/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 100, "type": "parent"}
                                """))
                .andExpect(status().isOk());

        // Child 1: 201 (amount: 50, parent: 200)
        mockMvc.perform(put("/transactions/201")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 50, "type": "child", "parent_id": 200}
                                """))
                .andExpect(status().isOk());

        // Child 2: 202 (amount: 25, parent: 200)
        mockMvc.perform(put("/transactions/202")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 25, "type": "child", "parent_id": 200}
                                """))
                .andExpect(status().isOk());

        // Sum of 200 should be 100 + 50 + 25 = 175
        mockMvc.perform(get("/transactions/sum/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(175.0));
    }
}
