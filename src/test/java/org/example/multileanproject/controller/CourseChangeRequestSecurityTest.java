package org.example.multileanproject.controller;

import org.example.multileanproject.config.JwtAuthenticationFilter;
import org.example.multileanproject.config.SecurityConfig;
import org.example.multileanproject.service.CourseChangeRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        InstructorCourseChangeRequestController.class,
        AdminCourseChangeRequestController.class
})
@Import(SecurityConfig.class)
class CourseChangeRequestSecurityTest {

    @Autowired MockMvc mockMvc;

    @MockBean CourseChangeRequestService changeRequestService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean AuthenticationProvider authenticationProvider;

    // ============================================================
    // TEST-15: STUDENT gọi endpoint instructor → 403
    // ============================================================

    @Test
    @WithMockUser(authorities = "STUDENT")
    void TEST15_studentRole_callsInstructorEndpoint_gets403() throws Exception {
        mockMvc.perform(post("/api/instructor/courses/1/basic-info-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // TEST-16: INSTRUCTOR gọi endpoint admin → 403
    // ============================================================

    @Test
    @WithMockUser(authorities = "INSTRUCTOR")
    void TEST16_instructorRole_callsAdminEndpoint_gets403() throws Exception {
        mockMvc.perform(post("/api/admin/course-change-requests/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // TEST-17: Không có authentication → 401
    // ============================================================

    @Test
    void TEST17_noAuthentication_gets401() throws Exception {
        mockMvc.perform(get("/api/instructor/courses/1/basic-info-requests/latest"))
                .andExpect(status().isUnauthorized());
    }
}
