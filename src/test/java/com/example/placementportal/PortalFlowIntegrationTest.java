package com.example.placementportal;

import com.example.placementportal.entity.Role;
import com.fasterxml.jackson.databind.JsonNode;
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
class PortalFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.placementportal.repository.UserRepository userRepository;

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        registerUser("login_user", "login@example.com", Role.STUDENT);

        Map<String, String> body = Map.of(
                "username", "login_user",
                "password", "wrong-password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentCannotCreateJob() throws Exception {
        registerUser("student_job", "sj@example.com", Role.STUDENT);
        String studentToken = login("student_job");

        Map<String, Object> job = Map.of(
                "title", "Blocked Job",
                "company", "Test Co",
                "salary", 500000
        );

        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unapprovedRecruiterCannotPostJob() throws Exception {
        registerUser("pending_rec", "pending@example.com", Role.RECRUITER);
        String recruiterToken = login("pending_rec");

        Map<String, Object> job = Map.of(
                "title", "Pending Job",
                "company", "Pending Co",
                "salary", 600000
        );

        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiterCannotUpdateAnotherRecruitersJob() throws Exception {
        registerUser("owner_rec", "owner@example.com", Role.RECRUITER);
        registerUser("other_rec", "other@example.com", Role.RECRUITER);

        approveRecruiter("owner_rec");
        approveRecruiter("other_rec");

        String ownerToken = login("owner_rec");
        String otherToken = login("other_rec");

        Long jobId = createJob(ownerToken, "Owned Job", "Owner Co", 700000);

        Map<String, Object> update = Map.of(
                "title", "Hijacked",
                "company", "Hacker Co",
                "salary", 1
        );

        mockMvc.perform(put("/jobs/" + jobId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanViewDashboardStats() throws Exception {
        String adminToken = login("admin");

        mockMvc.perform(get("/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanApproveRecruiter() throws Exception {
        registerUser("approve_me", "approve@example.com", Role.RECRUITER);
        String adminToken = login("admin");

        Long recruiterId = findRecruiterId("approve_me", adminToken);

        mockMvc.perform(put("/admin/recruiters/" + recruiterId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        String recruiterToken = login("approve_me");
        Map<String, Object> job = Map.of(
                "title", "Approved Job",
                "company", "Approved Co",
                "salary", 900000,
                "location", "Remote",
                "jobType", "Full-time"
        );

        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isOk());
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
                "password", username.equals("admin") ? "Adminpass123456!" : "Password123!"
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private void approveRecruiter(String username) throws Exception {
        String adminToken = login("admin");
        Long recruiterId = findRecruiterId(username, adminToken);

        mockMvc.perform(put("/admin/recruiters/" + recruiterId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private Long findRecruiterId(String username, String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/recruiters")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode recruiters = response.has("content") ? response.get("content") : response;
        for (JsonNode recruiter : recruiters) {
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
