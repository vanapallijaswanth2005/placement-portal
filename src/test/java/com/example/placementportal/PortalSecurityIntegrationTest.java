package com.example.placementportal;

import com.example.placementportal.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortalSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.placementportal.repository.UserRepository userRepository;

    @Test
    void registerRejectsAdminRole() throws Exception {
        Map<String, Object> body = Map.of(
                "username", "admin_user",
                "password", "Password123!",
                "email", "admin@example.com",
                "role", "ADMIN"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotApplyTwiceForSameJob() throws Exception {
        registerUser("student_dup", "student@example.com", Role.STUDENT);
        registerUser("recruiter_dup", "recruiter@example.com", Role.RECRUITER);
        approveRecruiter("recruiter_dup");

        String studentToken = login("student_dup");
        String recruiterToken = login("recruiter_dup");

        Long jobId = createJob(recruiterToken, "Backend Engineer", "Acme Corp", 800000);

        mockMvc.perform(post("/apply")
                        .header("Authorization", "Bearer " + studentToken)
                        .param("jobId", jobId.toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/apply")
                        .header("Authorization", "Bearer " + studentToken)
                        .param("jobId", jobId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void recruiterCannotUpdateApplicationForAnotherRecruitersJob() throws Exception {
        registerUser("student_own", "student2@example.com", Role.STUDENT);
        registerUser("recruiter_a", "rec_a@example.com", Role.RECRUITER);
        registerUser("recruiter_b", "rec_b@example.com", Role.RECRUITER);
        approveRecruiter("recruiter_a");
        approveRecruiter("recruiter_b");

        String studentToken = login("student_own");
        String recruiterAToken = login("recruiter_a");
        String recruiterBToken = login("recruiter_b");

        Long jobId = createJob(recruiterAToken, "Frontend Engineer", "Beta Inc", 700000);

        MvcResult applyResult = mockMvc.perform(post("/apply")
                        .header("Authorization", "Bearer " + studentToken)
                        .param("jobId", jobId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        String response = applyResult.getResponse().getContentAsString();
        Long applicationId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/apply/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterBToken)
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isForbidden());
    }

    private void registerUser(String username, String email, Role role) throws Exception {
        Map<String, Object> body = Map.of(
                "username", username,
                "password", "Password123!",
                "email", email,
                "role", role.name()
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        com.example.placementportal.entity.User savedUser = userRepository.findFirstByUsername(username).orElseThrow();
        savedUser.setEmailVerified(true);
        userRepository.save(savedUser);
    }

    private String login(String username) throws Exception {
        Map<String, String> body = Map.of(
                "username", username,
                "password", "Password123!"
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private void approveRecruiter(String username) throws Exception {
        String adminToken = loginAdmin();
        Long recruiterId = findRecruiterId(username, adminToken);

        mockMvc.perform(put("/admin/recruiters/" + recruiterId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private String loginAdmin() throws Exception {
        Map<String, String> body = Map.of(
                "username", "admin",
                "password", "Adminpass123456!"
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private Long findRecruiterId(String username, String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/recruiters")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        com.fasterxml.jackson.databind.JsonNode response =
                objectMapper.readTree(result.getResponse().getContentAsString());
        com.fasterxml.jackson.databind.JsonNode recruiters =
                response.has("content") ? response.get("content") : response;
        for (com.fasterxml.jackson.databind.JsonNode recruiter : recruiters) {
            if (username.equals(recruiter.get("username").asText())) {
                return recruiter.get("id").asLong();
            }
        }
        throw new IllegalStateException("Recruiter not found: " + username);
    }

    private Long createJob(String recruiterToken, String title, String company, double salary) throws Exception {
        Map<String, Object> job = Map.of(
                "title", title,
                "company", company,
                "salary", salary
        );

        MvcResult result = mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
