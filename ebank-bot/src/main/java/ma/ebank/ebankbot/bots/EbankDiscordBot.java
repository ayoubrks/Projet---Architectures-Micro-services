package ma.ebank.ebankbot.bots;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import ma.ebank.ebankbot.agents.EbankAiAgent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "discord.bot.enabled", havingValue = "true")
public class EbankDiscordBot extends ListenerAdapter {

    private final EbankAiAgent ebankAiAgent;
    private final String botToken;
    private JDA jda;

    public EbankDiscordBot(
            EbankAiAgent ebankAiAgent,
            @Value("${discord.bot.token}") String botToken) {
        this.ebankAiAgent = ebankAiAgent;
        this.botToken = botToken;
    }

    @PostConstruct
    public void start() {
        this.jda = JDABuilder.createDefault(botToken)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build();
        log.info("EbankDiscordBot started");
    }

    @PreDestroy
    public void stop() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String content = event.getMessage().getContentRaw();
        if (content.isBlank()) {
            return;
        }

        String conversationId = "dc-" + event.getChannel().getId();
        event.getChannel().sendTyping().queue();
        try {
            String answer = ebankAiAgent.chat(content, conversationId);
            if (answer.length() <= 2000) {
                event.getChannel().sendMessage(answer).queue();
            } else {
                for (int i = 0; i < answer.length(); i += 2000) {
                    event.getChannel().sendMessage(answer.substring(i, Math.min(answer.length(), i + 2000))).queue();
                }
            }
        } catch (Exception e) {
            log.error("Error processing Discord message in channel={}", event.getChannel().getId(), e);
            event.getChannel().sendMessage("Désolé, une erreur est survenue lors du traitement de votre demande.").queue();
        }
    }
}
