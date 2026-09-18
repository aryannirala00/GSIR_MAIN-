package com.gsir.workflow.goal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsir.common.error.CommonErrors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;

@SpringBootTest(classes = {com.gsir.GsirApplication.class, GoalControllerTest.FakeConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoalControllerTest {

    @TestConfiguration
    static class FakeConfig {
        @Bean
        @Primary
        public GoalService fakeGoalService() {
            return new GoalService(null, null, null, null) {
                @Override
                public Goal establishGoal(UUID projectId, UUID userId, String description, double priority) {
                    if (description.equals("Desc")) {
                        return new Goal(UUID.randomUUID(), "Desc", 1.0, GoalStatus.ACTIVE);
                    }
                    throw CommonErrors.forbidden();
                }

                @Override
                public Goal getGoal(UUID goalId, UUID projectId, UUID userId) {
                    throw CommonErrors.notFound("Goal");
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GoalService goalService;

    @Test
    @WithMockUser
    void testEstablishGoal_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        
        GoalController.CreateGoalRequest request = new GoalController.CreateGoalRequest("Desc", 1.0);

        mockMvc.perform(post("/api/v1/projects/" + projectId + "/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("Desc"));
    }

    @Test
    @WithMockUser
    void testEstablishGoal_UnauthorizedProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        
        GoalController.CreateGoalRequest request = new GoalController.CreateGoalRequest("ForbiddenDesc", 1.0);

        mockMvc.perform(post("/api/v1/projects/" + projectId + "/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
    
    @Test
    @WithMockUser
    void testGetGoal_NotFound() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/projects/" + projectId + "/goals/" + goalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
