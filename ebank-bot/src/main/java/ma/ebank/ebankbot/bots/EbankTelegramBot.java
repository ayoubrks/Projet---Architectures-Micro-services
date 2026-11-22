package ma.ebank.ebankbot.bots;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import ma.ebank.ebankbot.agents.EbankAiAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class EbankTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final EbankAiAgent ebankAiAgent;
    private final String botToken;
    private TelegramClient telegramClient;

    public EbankTelegramBot(
            EbankAiAgent ebankAiAgent,
            @Value("${telegram.bot.token}") String botToken) {
        this.ebankAiAgent = ebankAiAgent;
        this.botToken = botToken;
    }

    @PostConstruct
    public void init() {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        log.info("EbankTelegramBot initialized");
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingSingleThreadUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        String userText = update.getMessage().getText();
        String conversationId = "tg-" + chatId;

        try {
            sendTypingAction(chatId);
            String response = ebankAiAgent.chat(userText, conversationId);
            sendTextMessage(chatId, response);
        } catch (Exception e) {
            log.error("Error processing Telegram message for chatId={}", chatId, e);
            sendTextMessage(chatId, "Désolé, une erreur est survenue lors du traitement de votre demande.");
        }
    }

    void sendTypingAction(Long chatId) throws TelegramApiException {
        if (telegramClient == null) return;
        SendChatAction action = SendChatAction.builder()
                .chatId(chatId.toString())
                .action(ActionType.TYPING.toString())
                .build();
        telegramClient.execute(action);
    }

    void sendTextMessage(Long chatId, String text) {
        if (telegramClient == null) return;
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chatId={}", chatId, e);
        }
    }
}
