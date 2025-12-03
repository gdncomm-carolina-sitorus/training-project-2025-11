package com.marketplace.api_gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.api_gateway.security.JwtUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

class LoginHandlerTest {

    private static MockWebServer mockWebServer;
    private static LoginHandler loginHandler;
    private static WebTestClient webTestClient;
    private static JwtUtils jwtUtils;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient.Builder webClientBuilder = WebClient.builder();
        jwtUtils = mock(JwtUtils.class);
        ObjectMapper objectMapper = new ObjectMapper();

        // Mock JwtUtils to avoid actual token validation/generation logic issues during
        // test
        when(jwtUtils.generateToken(any(), any())).thenReturn("generated_token");

        loginHandler = new LoginHandler(webClientBuilder, jwtUtils, objectMapper);

        // Inject the mock server URL into the handler via reflection
        try {
            java.lang.reflect.Field field = LoginHandler.class.getDeclaredField("memberServiceUrl");
            field.setAccessible(true);
            field.set(loginHandler, mockWebServer.url("/").toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        RouterFunction<ServerResponse> route = RouterFunctions.route(
                POST("/login"),
                loginHandler::handleLogin);

        webTestClient = WebTestClient.bindToRouterFunction(route).build();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void handleLogin_shouldReturnErrorBody_whenDownstreamReturnsError() {
        String errorBody = "{\"error\": \"Invalid credentials\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody(errorBody)
                .addHeader("Content-Type", "application/json"));

        webTestClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\": \"test\", \"password\": \"wrong\"}")
                .exchange()
                .expectStatus().isEqualTo(401)
                .expectBody(String.class).isEqualTo(errorBody);
    }

    @Test
    void handleLogin_shouldGenerateToken_whenDownstreamReturnsSuccess() {
        String successBody = "{\"success\": true, \"data\": {\"id\": 1, \"username\": \"testuser\"}}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(successBody)
                .addHeader("Content-Type", "application/json"));

        webTestClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\": \"test\", \"password\": \"correct\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Authorization", "Bearer generated_token")
                .expectBody(String.class).isEqualTo(successBody);
    }
}
