package ma.ebank.ebankbot.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class EbankAiAgent {

    private final ChatClient chatClient;

    public EbankAiAgent(ChatClient.Builder chatClient, ToolCallbackProvider tools, ChatMemory chatMemory) {
        this.chatClient = chatClient
                .defaultSystem("""
                        Vous êtes un assistant bancaire intelligent pour la plateforme E-Bank.
                        Vous disposez d'outils MCP connectés aux microservices customer-service (clients)
                        et ebank-service (comptes bancaires et opérations).
                        Utilisez toujours ces outils pour consulter les vraies données ou effectuer des opérations
                        (création de client/compte, crédit, débit, virement).
                        Répondez de manière claire, concise et dans la langue de l'utilisateur.
                        """)
                .defaultToolCallbacks(tools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public String chat(String query) {
        return chat(query, "C1");
    }

    public String chat(String query, String conversationId) {
        String convId = (conversationId == null || conversationId.isBlank()) ? "C1" : conversationId;
        return chatClient.prompt(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, convId))
                .call()
                .content();
    }
}
