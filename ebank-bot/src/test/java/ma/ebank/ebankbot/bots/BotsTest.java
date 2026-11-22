package ma.ebank.ebankbot.bots;

import ma.ebank.ebankbot.agents.EbankAiAgent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BotsTest {

    @Test
    void telegramBotDelegatesToAgentWithConversationId() {
        EbankAiAgent agent = mock(EbankAiAgent.class);
        when(agent.chat("Bonjour", "tg-12345")).thenReturn("Salut !");

        EbankTelegramBot bot = spy(new EbankTelegramBot(agent, "dummy-token"));
        doNothing().when(bot).sendTextMessage(anyLong(), anyString());

        Update update = new Update();
        org.telegram.telegrambots.meta.api.objects.message.Message msg =
                new org.telegram.telegrambots.meta.api.objects.message.Message();
        Chat chat = new Chat(12345L, "private");
        msg.setChat(chat);
        msg.setText("Bonjour");
        update.setMessage(msg);

        bot.consume(update);

        verify(agent).chat("Bonjour", "tg-12345");
        verify(bot).sendTextMessage(12345L, "Salut !");
    }

    @Test
    void discordBotDelegatesToAgentWithConversationId() {
        EbankAiAgent agent = mock(EbankAiAgent.class);
        when(agent.chat("Solde ?", "dc-999")).thenReturn("25000 MAD");

        EbankDiscordBot bot = new EbankDiscordBot(agent, "dummy-token");

        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        User author = mock(User.class);
        when(author.isBot()).thenReturn(false);
        when(event.getAuthor()).thenReturn(author);

        Message message = mock(Message.class);
        when(message.getContentRaw()).thenReturn("Solde ?");
        when(event.getMessage()).thenReturn(message);

        MessageChannelUnion channel = mock(MessageChannelUnion.class);
        when(channel.getId()).thenReturn("999");
        RestAction<Void> typingAction = mock(RestAction.class);
        when(channel.sendTyping()).thenReturn(typingAction);
        MessageCreateAction msgAction = mock(MessageCreateAction.class);
        when(channel.sendMessage("25000 MAD")).thenReturn(msgAction);
        when(event.getChannel()).thenReturn(channel);

        bot.onMessageReceived(event);

        verify(agent).chat("Solde ?", "dc-999");
        verify(channel).sendMessage("25000 MAD");
        verify(msgAction).queue();
    }
}
