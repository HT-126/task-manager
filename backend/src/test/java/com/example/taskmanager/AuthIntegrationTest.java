package com.example.taskmanager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Full application (real security, JWT, services, H2): register -> login -> use -> logout.
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    MockMvc mvc;

    private void register(String username, String password) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void registerLoginUseAndLogout() throws Exception {
        register("alice", "correct-horse");
        String token = login("alice", "correct-horse");

        mvc.perform(post("/api/tasks").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"mine\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/tasks").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("mine"));

        mvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        // The same token is rejected after logout.
        mvc.perform(get("/api/tasks").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersCannotSeeOrChangeEachOthersTasks() throws Exception {
        register("bob", "password-bob");
        register("carol", "password-carol");
        String bob = login("bob", "password-bob");
        String carol = login("carol", "password-carol");

        MvcResult created = mvc.perform(post("/api/tasks").header(HttpHeaders.AUTHORIZATION, bearer(bob))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"bob's\"}"))
                .andExpect(status().isCreated()).andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(get("/api/tasks/" + id).header(HttpHeaders.AUTHORIZATION, bearer(carol)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/tasks/" + id).header(HttpHeaders.AUTHORIZATION, bearer(carol)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/tasks").header(HttpHeaders.AUTHORIZATION, bearer(carol)))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/tasks/" + id).header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(status().isOk());
    }

    @Test
    void duplicateUsernameReturns409() throws Exception {
        register("dave", "password-dave");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"DAVE\",\"password\":\"another-pass\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordAndUnknownUserBothReturn401() throws Exception {
        register("erin", "password-erin");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"erin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"whatever-pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shortPasswordReturns400WithoutEchoingIt() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"frank\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void garbageTokenReturns401() throws Exception {
        mvc.perform(get("/api/tasks").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }
}
