package ma.ebank.ebankbot.web;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import ma.ebank.ebankbot.agents.EbankAiAgent;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatRestController {

    private final EbankAiAgent ebankAiAgent;
    private final List<McpSyncClient> mcpSyncClients;

    @GetMapping("/chat")
    public String chatGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "C1") String conversationId) {
        return ebankAiAgent.chat(query, conversationId);
    }

    @PostMapping("/api/chat")
    public Map<String, String> chatPost(@RequestBody ChatRequest request) {
        String convId = (request.conversationId() == null || request.conversationId().isBlank())
                ? "C1"
                : request.conversationId();
        String answer = ebankAiAgent.chat(request.query(), convId);
        return Map.of(
                "conversationId", convId,
                "query", request.query(),
                "response", answer
        );
    }

    @GetMapping("/api/mcp/tools")
    public List<Map<String, Object>> listMcpTools() {
        return mcpSyncClients.stream()
                .map(client -> Map.<String, Object>of(
                        "serverInfo", client.getServerInfo() != null ? client.getServerInfo().name() : "unknown",
                        "tools", client.listTools().tools().stream()
                                .map(t -> Map.of(
                                        "name", t.name(),
                                        "description", t.description() != null ? t.description() : ""
                                ))
                                .toList()
                ))
                .toList();
    }

    @PostMapping("/api/mcp/call")
    public McpSchema.CallToolResult callMcpTool(@RequestBody McpToolCallRequest request) {
        Map<String, Object> args = request.arguments() != null ? request.arguments() : Map.of();
        for (McpSyncClient client : mcpSyncClients) {
            boolean hasTool = client.listTools().tools().stream()
                    .anyMatch(t -> t.name().equals(request.toolName()));
            if (hasTool) {
                return client.callTool(new McpSchema.CallToolRequest(request.toolName(), args));
            }
        }
        throw new IllegalArgumentException("MCP tool not found: " + request.toolName());
    }

    public record ChatRequest(String query, String conversationId) {
    }

    public record McpToolCallRequest(String toolName, Map<String, Object> arguments) {
    }
}
