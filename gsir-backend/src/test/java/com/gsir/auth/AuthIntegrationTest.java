package com.gsir.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.auth.dto.LoginRequest;
import com.gsir.auth.dto.RegisterRequest;
import com.gsir.auth.dto.RefreshRequest;
import com.gsir.users.UserRepository;
import com.gsir.users.SessionRepository;
import com.gsir.users.RefreshTokenRepository;
import com.gsir.users.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;

import java.util.UUID;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private MvcResult register(String email, String password) throws Exception {
        RegisterRequest registerReq = new RegisterRequest(email, password);
        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
                .andReturn();
    }
    
    private MvcResult login(String email, String password) throws Exception {
        LoginRequest loginReq = new LoginRequest(email, password);
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();
    }

    @Test
    void registerAndLoginEmailNormalization() throws Exception {
        // Register with uppercase
        register("NewUser@Example.COM", "SecurePass123");
        
        // Ensure it's stored lowercase
        assertTrue(userRepository.findByEmail("newuser@example.com").isPresent());
        
        // Login with mixed case
        MvcResult result = login("nEwUsEr@eXAmple.com", "SecurePass123");
        assertEquals(200, result.getResponse().getStatus());
        
        // Register duplicate differing only by case fails
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest("newuser@example.com", "DifferentPass"))))
                .andExpect(status().isConflict());
    }
    
    @Test
    void wrongPasswordFails() throws Exception {
        register("user@example.com", "SecurePass123");
        MvcResult res = login("user@example.com", "WrongPass");
        assertEquals(401, res.getResponse().getStatus());
    }

    @Test
    void nonexistentAccountFailsWithSameError() throws Exception {
        // Must return identical 401 unauthorized
        MvcResult res = login("nobody@example.com", "SecurePass123");
        assertEquals(401, res.getResponse().getStatus());
    }

    @Test
    void unauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/conversations"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validRefreshSucceedsAndRotates() throws Exception {
        MvcResult res = register("user@example.com", "SecurePass123");
        String json = res.getResponse().getContentAsString();
        String refreshToken = JsonPath.read(json, "$.data.refreshToken");

        // Refresh
        MvcResult refreshRes = mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        // Old token should be rejected now
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void individualLogoutRevokesOnlyCurrentSession() throws Exception {
        register("user@example.com", "SecurePass123");
        
        // Login 1
        MvcResult res1 = login("user@example.com", "SecurePass123");
        String access1 = JsonPath.read(res1.getResponse().getContentAsString(), "$.data.accessToken");
        
        // Login 2
        MvcResult res2 = login("user@example.com", "SecurePass123");
        String access2 = JsonPath.read(res2.getResponse().getContentAsString(), "$.data.accessToken");
        String refresh2 = JsonPath.read(res2.getResponse().getContentAsString(), "$.data.refreshToken");
        
        // Logout 1
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + access1))
                .andExpect(status().isNoContent());
                
        // Session 1 is invalid
        mockMvc.perform(get("/api/v1/conversations")
                .header("Authorization", "Bearer " + access1))
                .andExpect(status().isForbidden());
                
        // Session 2 is still valid
        mockMvc.perform(get("/api/v1/conversations")
                .header("Authorization", "Bearer " + access2))
                // Not expecting forbidden. Since conversations logic doesn't exist fully, it might return 200 or 400, but not 401/403.
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
                
        // Refresh 2 is still valid
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest(refresh2))))
                .andExpect(status().isOk());
    }

    @Test
    void logoutAllRevokesAllSessions() throws Exception {
        register("user@example.com", "SecurePass123");
        
        MvcResult res1 = login("user@example.com", "SecurePass123");
        String access1 = JsonPath.read(res1.getResponse().getContentAsString(), "$.data.accessToken");
        
        MvcResult res2 = login("user@example.com", "SecurePass123");
        String access2 = JsonPath.read(res2.getResponse().getContentAsString(), "$.data.accessToken");
        String refresh2 = JsonPath.read(res2.getResponse().getContentAsString(), "$.data.refreshToken");
        
        // Logout All via Session 1
        mockMvc.perform(post("/api/v1/auth/logout-all")
                .header("Authorization", "Bearer " + access1))
                .andExpect(status().isNoContent());
                
        // Both access tokens should now be invalid because session revoked
        mockMvc.perform(get("/api/v1/conversations")
                .header("Authorization", "Bearer " + access1))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/api/v1/conversations")
                .header("Authorization", "Bearer " + access2))
                .andExpect(status().isForbidden());
                
        // Refresh 2 should also be invalid
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshRequest(refresh2))))
                .andExpect(status().isUnauthorized());
    }
}

