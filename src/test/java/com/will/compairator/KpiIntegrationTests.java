package com.will.compairator;

import com.will.compairator.ai.dto.*;
import com.will.compairator.ai.enums.*;
import com.will.compairator.ai.exception.AiProviderCallException;
import com.will.compairator.ai.persistence.AiApiRequestLogRepository;
import com.will.compairator.ai.services.AiApiRequestCachedService;
import com.will.compairator.comparison.enums.ComparisonStatus;
import com.will.compairator.comparison.persistence.ComparisonRepository;
import com.will.compairator.configuration.CompairatorApplication;
import com.will.compairator.user.persistence.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import java.net.*;
import java.net.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CompairatorApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TestDatabaseGuard.class)
class KpiIntegrationTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired ComparisonRepository comparisons;
    @Autowired AiApiRequestLogRepository logs;
    @Autowired CacheManager cacheManager;
    @MockitoBean AiApiRequestCachedService providerCalls;
    final ObjectMapper json = new JsonMapper();
    HttpClient client;
    String csrf;
    String csrfHeader;

    @BeforeEach void setup() throws Exception {
        logs.deleteAll(); comparisons.deleteAll(); users.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("aiResponse")).clear();
        newClient();
        when(providerCalls.sendRequest(any(), any())).thenAnswer(invocation -> {
            AiChatDTO.PostInput input = invocation.getArgument(1);
            return answer(input.provider());
        });
    }

    private AiApiDTO.PostOutput answer(AiProvider provider) {
        return new AiApiDTO.PostOutput(List.of(new AiApiDTO.Choice(0, new AiApiDTO.Message("assistant", "Même réponse correcte"))),
                provider == AiProvider.GROQ ? "test-groq" : "test-mistral", new AiApiDTO.Usage(10L, 20L));
    }

    void newClient() throws Exception {
        client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        token();
    }
    void token() throws Exception {
        JsonNode result = json.readTree(request("GET", "/auth/csrf", null, null).body());
        csrf = result.get("token").asText(); csrfHeader = result.get("headerName").asText();
    }
    HttpResponse<String> request(String method, String path, String body, String contentType) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (body != null) {
            request.header("Content-Type", contentType).header(csrfHeader, csrf);
            request.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else request.method(method, HttpRequest.BodyPublishers.noBody());
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    HttpResponse<String> post(String path, String body) throws Exception {
        return request("POST", path, body, "application/json");
    }
    void registerAndLogin(String email) throws Exception {
        assertEquals(201, post("/auth/register", "{\"email\":\"" + email + "\",\"password\":\"a secure password 123\"}").statusCode());
        login(email);
    }
    void login(String email) throws Exception {
        assertEquals(204, request("POST", "/auth/login", "username=" + URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8)
                + "&password=a+secure+password+123", "application/x-www-form-urlencoded").statusCode());
        token();
    }
    JsonNode compare() throws Exception {
        HttpResponse<String> response = post("/ai/compare", "{\"prompt\":\"Question\",\"providers\":[\"GROQ\",\"MISTRAL\"],\"taskType\":\"CODING\"}");
        assertEquals(200, response.statusCode(), response.body());
        return json.readTree(response.body());
    }

    @Test void authenticationCsrfAndLogoutAreEnforced() throws Exception {
        assertEquals(401, post("/ai/compare", "{}").statusCode());
        assertEquals(401, request("GET", "/stats", null, null).statusCode());
        assertEquals(401, request("GET", "/admin/stats", null, null).statusCode());
        assertEquals(401, request("GET", "/compairator-ui/admin.html", null, null).statusCode());
        HttpRequest withoutCsrf = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/auth/register"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{}")).build();
        assertEquals(403, client.send(withoutCsrf, HttpResponse.BodyHandlers.ofString()).statusCode());
        registerAndLogin("User@example.com");
        assertEquals("user@example.com", json.readTree(request("GET", "/auth/me", null, null).body()).get("email").asText());
        assertNotNull(users.findByEmail("user@example.com").orElseThrow().getLastLoginAt());
        assertNotEquals("a secure password 123", users.findByEmail("user@example.com").orElseThrow().getPasswordHash());
        assertEquals(403, request("GET", "/stats", null, null).statusCode());
        assertEquals(403, request("GET", "/admin/stats", null, null).statusCode());
        assertEquals(403, request("GET", "/compairator-ui/admin.html", null, null).statusCode());
        assertEquals(204, post("/auth/logout", "{}").statusCode());
        token();
        assertEquals(401, post("/ai/chat", "{\"prompt\":\"hello\",\"provider\":\"GROQ\"}").statusCode());
    }

    @Test void duplicateRegistrationInvalidFieldsAndWrongPassword() throws Exception {
        assertEquals(400, post("/auth/register", "{\"email\":\"invalid\",\"password\":\"short\"}").statusCode());
        registerAndLogin("user@example.com");
        assertEquals(409, post("/auth/register", "{\"email\":\"USER@example.com\",\"password\":\"another password\"}").statusCode());
        assertEquals(401, request("POST", "/auth/login", "username=user%40example.com&password=wrong", "application/x-www-form-urlencoded").statusCode());
    }

    @Test void twoAnswersMakeOneComparisonAndFeedbackIsOptionalAndReplaceable() throws Exception {
        registerAndLogin("user@example.com");
        JsonNode result = compare();
        assertEquals(2, result.get("output").size());
        assertEquals(1, comparisons.count()); assertEquals(2, logs.count());
        var comparison = comparisons.findAll().getFirst();
        assertEquals(ComparisonStatus.SUCCESS, comparison.getStatus());
        assertNull(comparison.getUseful()); assertNull(comparison.getPreference());
        assertEquals(20L, logs.findAll().stream().mapToLong(l -> l.getPromptTokens()).sum());
        assertTrue(logs.findAll().stream().allMatch(l -> l.getEstimatedCostUsd() != null));
        String path = "/comparisons/" + result.get("comparisonId").asText() + "/feedback";
        assertEquals(400, request("PUT", path, "{}", "application/json").statusCode());
        assertEquals(200, request("PUT", path, "{\"preference\":\"EQUIVALENT\"}", "application/json").statusCode());
        assertEquals(200, request("PUT", path, "{\"useful\":true}", "application/json").statusCode());
        assertEquals(200, request("PUT", path, "{\"preference\":\"GROQ\",\"useful\":false}", "application/json").statusCode());
        assertEquals(1, comparisons.count());
        assertFalse(comparisons.findAll().getFirst().getUseful());
        newClient(); registerAndLogin("other@example.com");
        assertEquals(404, request("PUT", path, "{\"useful\":true}", "application/json").statusCode());
    }

    @Test void secondProviderFailurePersistsFailedComparisonAndFirstCall() throws Exception {
        registerAndLogin("user@example.com");
        doAnswer(invocation -> {
            AiChatDTO.PostInput input = invocation.getArgument(1);
            if (input.provider() == AiProvider.MISTRAL) throw new AiProviderCallException("Test provider failure", new RuntimeException());
            return answer(input.provider());
        }).when(providerCalls).sendRequest(any(), any());
        assertEquals(502, post("/ai/compare", "{\"prompt\":\"Question\",\"providers\":[\"GROQ\",\"MISTRAL\"]}").statusCode());
        assertEquals(ComparisonStatus.ERROR, comparisons.findAll().getFirst().getStatus());
        assertEquals(2, logs.count());
        assertEquals(1, logs.findAll().stream().filter(l -> l.getStatus() == AiApiRequestStatus.SUCCESS).count());
    }

    @Test void cachedAnswersConsumeNoAdditionalTokens() throws Exception {
        registerAndLogin("user@example.com");
        var cache = Objects.requireNonNull(cacheManager.getCache("aiResponse"));
        cache.put(new AiChatDTO.PostInput("Question", AiProvider.GROQ), answer(AiProvider.GROQ));
        cache.put(new AiChatDTO.PostInput("Question", AiProvider.MISTRAL), answer(AiProvider.MISTRAL));
        compare();
        verifyNoInteractions(providerCalls);
        assertTrue(logs.findAll().stream().allMatch(l -> l.getOrigin() == AiApiResponseOrigin.CACHE
                && l.getPromptTokens() == 0 && l.getCompletionTokens() == 0 && l.getEstimatedCostUsd().signum() == 0));
    }

    @Test void adminStatisticsHaveCorrectVotesAndNoPasswordsOrPrompts() throws Exception {
        registerAndLogin("admin@example.com");
        JsonNode comparison = compare();
        String path = "/comparisons/" + comparison.get("comparisonId").asText() + "/feedback";
        request("PUT", path, "{\"preference\":\"GROQ\",\"useful\":true}", "application/json");
        var admin = users.findByEmail("admin@example.com").orElseThrow();
        admin.setRole("ADMIN"); users.saveAndFlush(admin);
        post("/auth/logout", "{}"); token(); login("admin@example.com");
        HttpResponse<String> dashboard = request("GET", "/admin/stats", null, null);
        assertEquals(200, dashboard.statusCode());
        assertTrue(dashboard.body().contains("Les chiffres du projet."));
        assertEquals(200, request("GET", "/compairator-ui/admin.html", null, null).statusCode());
        HttpResponse<String> response = request("GET", "/stats", null, null);
        assertEquals(200, response.statusCode(), response.body());
        JsonNode stats = json.readTree(response.body());
        assertEquals(1, stats.get("registeredUsers").asInt());
        assertEquals(1, stats.get("comparisonsStarted").asInt());
        assertEquals(1, stats.get("activeUsers").asInt());
        assertEquals(1.0, stats.get("usefulness").get("rate").asDouble());
        assertTrue(stats.get("activationWithin7Days").get("rate").isNull());
        assertEquals(2, stats.get("modelPreferences").size());
        assertFalse(response.body().contains("password")); assertFalse(response.body().contains("Question"));
        assertEquals(400, request("GET", "/stats?from=2026-01-02&to=2026-01-01", null, null).statusCode());
    }
}
