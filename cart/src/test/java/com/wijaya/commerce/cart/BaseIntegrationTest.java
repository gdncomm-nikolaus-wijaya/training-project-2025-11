package com.wijaya.commerce.cart;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.wijaya.commerce.cart.repository.CartRepository;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected CartRepository cartRepository;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        cartRepository.deleteAll();
    }
}
