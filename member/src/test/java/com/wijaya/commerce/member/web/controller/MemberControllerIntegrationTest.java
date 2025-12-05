package com.wijaya.commerce.member.web.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import com.wijaya.commerce.member.constant.MemberApiPath;
import com.wijaya.commerce.member.repository.MemberRepository;
import com.wijaya.commerce.member.repository.SessionManagerRepository;
import com.wijaya.commerce.member.restWebModel.request.RegisterRequestWebModel;
import com.wijaya.commerce.member.restWebModel.request.LoginRequestWebModel;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SessionManagerRepository sessionManagerRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        memberRepository.deleteAll();
        sessionManagerRepository.deleteAll();

        // Flush Redis cache before each test
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up database after each test
        memberRepository.deleteAll();
        sessionManagerRepository.deleteAll();

        // Flush Redis cache after each test
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        }
    }

    @Test
    void register_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Arrange
        RegisterRequestWebModel request = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.data.createdAt").exists());
        Assertions.assertNotNull(memberRepository.findByEmail("john.doe@example.com"));
    }

    @Test
    void register_WithExistingEmail_ShouldReturnError() throws Exception {
        // Arrange - Register a user first
        RegisterRequestWebModel firstRequest = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // Attempt to register with the same email
        RegisterRequestWebModel duplicateRequest = RegisterRequestWebModel.builder()
                .name("Jane Doe")
                .email("john.doe@example.com") // Same email
                .password("password456")
                .phoneNumber("+0987654321")
                .build();

        // Act & Assert - Should fail due to duplicate email
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Email already exists"));
    }

    @Test
    void register_WithExistingPhoneNumber_ShouldReturnError() throws Exception {
        // Arrange - Register a user first
        RegisterRequestWebModel firstRequest = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // Attempt to register with the same phone number
        RegisterRequestWebModel duplicateRequest = RegisterRequestWebModel.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .password("password456")
                .phoneNumber("+1234567890") // Same phone number
                .build();

        // Act & Assert - Should fail due to duplicate phone number
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Phone number already exists"));
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnValidationError() throws Exception {
        // Arrange
        RegisterRequestWebModel request = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("invalid-email") // Invalid email format
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").value("Email is not valid"));
    }

    @Test
    void register_WithShortPassword_ShouldReturnValidationError() throws Exception {
        // Arrange
        RegisterRequestWebModel request = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("12345") // Password too short (less than 6 characters)
                .phoneNumber("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.password").value("Password must be at least 6 characters long"));
    }

    @Test
    void register_WithMissingName_ShouldReturnValidationError() throws Exception {
        // Arrange
        RegisterRequestWebModel request = RegisterRequestWebModel.builder()
                .name("") // Empty name
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").value("Name is required"));
    }

    @Test
    void register_WithMissingPhoneNumber_ShouldReturnValidationError() throws Exception {
        // Arrange
        RegisterRequestWebModel request = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("") // Empty phone number
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.phoneNumber").value("Phone number is required"));
    }

    @Test
    void register_WithMultipleUsers_ShouldCreateSeparateRecords() throws Exception {
        // Arrange
        RegisterRequestWebModel request1 = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        RegisterRequestWebModel request2 = RegisterRequestWebModel.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .password("password456")
                .phoneNumber("+0987654321")
                .build();

        // Act - Register first user
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

        // Act - Register second user
        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("jane.smith@example.com"));

        // Assert - Verify both users exist in database
        Assertions.assertTrue(memberRepository.count() == 2);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnSuccess() throws Exception {
        // Arrange - Register a user first
        RegisterRequestWebModel registerRequest = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login request
        LoginRequestWebModel loginRequest = LoginRequestWebModel.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.user.name").value("John Doe"));
        Assertions.assertTrue(sessionManagerRepository.count() == 1);
    }

    @Test
    void login_WithNonExistentEmail_ShouldReturnError() throws Exception {
        // Arrange
        LoginRequestWebModel loginRequest = LoginRequestWebModel.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Member not found"));
    }

    @Test
    void login_WithWrongPassword_ShouldReturnError() throws Exception {
        // Arrange - Register a user first
        RegisterRequestWebModel registerRequest = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login with wrong password
        LoginRequestWebModel loginRequest = LoginRequestWebModel.builder()
                .email("john.doe@example.com")
                .password("wrongpassword")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Invalid password"));
    }

    @Test
    void login_WithInvalidEmailFormat_ShouldReturnValidationError() throws Exception {
        // Arrange
        LoginRequestWebModel loginRequest = LoginRequestWebModel.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").value("Email is not valid"));
    }

    @Test
    void login_WithMissingPassword_ShouldReturnValidationError() throws Exception {
        // Arrange
        LoginRequestWebModel loginRequest = LoginRequestWebModel.builder()
                .email("john.doe@example.com")
                .password("")
                .build();

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.password").value("Password is required"));
    }

    @Test
    void logout_WithValidToken_ShouldReturnSuccess() throws Exception {
        // Arrange - Register and login to get access token
        RegisterRequestWebModel registerRequest = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequestWebModel loginRequest = LoginRequestWebModel.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        String loginResponse = mockMvc.perform(post(MemberApiPath.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertTrue(sessionManagerRepository.count() == 1);

        String accessToken = objectMapper.readTree(loginResponse)
                .get("data")
                .get("accessToken")
                .asText();

        // Act & Assert - Logout with valid token
        mockMvc.perform(post(MemberApiPath.LOGOUT)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Logged out successfully"));

        Assertions.assertTrue(sessionManagerRepository.count() == 0);
    }

    @Test
    void logout_WithInvalidToken_ShouldReturnError() throws Exception {
        // Arrange
        String invalidToken = "invalid-token-12345";

        // Act & Assert
        mockMvc.perform(post(MemberApiPath.LOGOUT)
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Invalid token"));
    }

    @Test
    void logout_WithMissingToken_ShouldReturnError() throws Exception {
        // Act & Assert
        mockMvc.perform(post(MemberApiPath.LOGOUT)
                .header("Authorization", ""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Invalid token"));
    }

    @Test
    void getUserDetail_WithValidUserId_ShouldReturnSuccess() throws Exception {
        // Arrange - Register a user
        RegisterRequestWebModel registerRequest = RegisterRequestWebModel.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();

        mockMvc.perform(post(MemberApiPath.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
        // Get the user ID from repository
        String userId = memberRepository.findByEmail("john.doe@example.com")
                .orElseThrow()
                .getId();

        // Act & Assert
        mockMvc.perform(get(MemberApiPath.GET_USER_DETAIL)
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Verify cache was created
        Object cachedValue = redisTemplate.opsForValue().get(userId);
        Assertions.assertNotNull(cachedValue);
    }

    @Test
    void getUserDetail_WithNonExistentUserId_ShouldReturnError() throws Exception {
        // Arrange
        String nonExistentUserId = "507f1f77bcf86cd799439011";

        // Act & Assert
        mockMvc.perform(get(MemberApiPath.GET_USER_DETAIL)
                .param("userId", nonExistentUserId))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("User not found"));

        Object cachedValue = redisTemplate.opsForValue().get(nonExistentUserId);
        Assertions.assertNull(cachedValue);
    }

    @Test
    void getUserDetail_WithMissingUserId_ShouldReturnError() throws Exception {
        // Act & Assert
        mockMvc.perform(get(MemberApiPath.GET_USER_DETAIL))
                .andExpect(status().isBadRequest());
    }
}
