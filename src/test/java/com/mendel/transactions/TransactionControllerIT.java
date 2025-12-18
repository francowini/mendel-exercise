package com.mendel.transactions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIT {

    @Autowired
    MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        // hierarchy from exercise.md: 10 -> 11 -> 12
        mvc.perform(put("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 5000, \"type\": \"cars\"}"));

        mvc.perform(put("/transactions/11")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 10000, \"type\": \"shopping\", \"parent_id\": 10}"));

        mvc.perform(put("/transactions/12")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 5000, \"type\": \"shopping\", \"parent_id\": 11}"));
    }

    // PUT /transactions/{id}

    @Test
    void putCreatesTransaction() throws Exception {
        mvc.perform(put("/transactions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1500, \"type\": \"food\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void putWithParent() throws Exception {
        mvc.perform(put("/transactions/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 2500, \"type\": \"food\", \"parent_id\": 10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void putUpdatesExisting() throws Exception {
        mvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 9999, \"type\": \"vehicles\"}"))
                .andExpect(status().isOk());

        // verify type changed
        mvc.perform(get("/transactions/types/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value(10));

        // old type no longer has it
        mvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // GET /transactions/types/{type}

    @Test
    void getByTypeCars() throws Exception {
        mvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value(10));
    }

    @Test
    void getByTypeShopping() throws Exception {
        mvc.perform(get("/transactions/types/shopping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", containsInAnyOrder(11, 12)));
    }

    @Test
    void getByTypeReturnsEmptyForUnknown() throws Exception {
        mvc.perform(get("/transactions/types/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // GET /transactions/sum/{id} - transitive sum

    @Test
    void sumForRootIncludesAllDescendants() throws Exception {
        // sum(10) = 5000 + 10000 + 5000 = 20000
        mvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20000.0));
    }

    @Test
    void sumForMiddleNode() throws Exception {
        // sum(11) = 10000 + 5000 = 15000
        mvc.perform(get("/transactions/sum/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(15000.0));
    }

    @Test
    void sumForLeafNode() throws Exception {
        // sum(12) = 5000 (no children)
        mvc.perform(get("/transactions/sum/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(5000.0));
    }

    @Test
    void sumReturnsZeroForNonExistent() throws Exception {
        mvc.perform(get("/transactions/sum/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(0.0));
    }

    @Test
    void sumWithMultipleChildren() throws Exception {
        // add another child to 10
        mvc.perform(put("/transactions/13")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 3000, \"type\": \"other\", \"parent_id\": 10}"));

        // sum(10) = 5000 + 10000 + 5000 + 3000 = 23000
        mvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(23000.0));
    }

    @Test
    void sumWithDeepHierarchy() throws Exception {
        // extend chain: 12 -> 20 -> 21
        mvc.perform(put("/transactions/20")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 100, \"type\": \"deep\", \"parent_id\": 12}"));

        mvc.perform(put("/transactions/21")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 50, \"type\": \"deeper\", \"parent_id\": 20}"));

        // sum(12) = 5000 + 100 + 50 = 5150
        mvc.perform(get("/transactions/sum/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(5150.0));

        // sum(10) should include everything: 5000+10000+5000+100+50 = 20150
        mvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20150.0));
    }

    // TODO: add tests for decimal precision if needed
    @Test
    void sumWithDecimalAmounts() throws Exception {
        mvc.perform(put("/transactions/30")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 100.50, \"type\": \"decimal\"}"));

        mvc.perform(put("/transactions/31")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 50.25, \"type\": \"decimal\", \"parent_id\": 30}"));

        mvc.perform(get("/transactions/sum/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(150.75));
    }

    @Test
    void sumHandlesSelfReference() throws Exception {
        mvc.perform(put("/transactions/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 500, \"type\": \"self\", \"parent_id\": 100}"));

        mvc.perform(get("/transactions/sum/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(500.0));
    }

    @Test
    void sumHandlesOrphanedParent() throws Exception {
        mvc.perform(put("/transactions/200")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 750, \"type\": \"orphan\", \"parent_id\": 9999}"));

        mvc.perform(get("/transactions/sum/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(750.0));
    }

    @Test
    void sumHandlesNegativeAmount() throws Exception {
        mvc.perform(put("/transactions/300")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 1000, \"type\": \"neg\"}"));

        mvc.perform(put("/transactions/301")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": -200, \"type\": \"neg\", \"parent_id\": 300}"));

        mvc.perform(get("/transactions/sum/300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(800.0));
    }

    @Test
    void sumHandlesZeroAmount() throws Exception {
        mvc.perform(put("/transactions/400")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 0, \"type\": \"zero\"}"));

        mvc.perform(get("/transactions/sum/400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(0.0));
    }
}
