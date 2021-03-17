package work.tinax.DiscordTTS;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class DiscordBot extends ListenerAdapter {
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message msg = event.getMessage();
		String raw = msg.getContentRaw();
		if (raw.startsWith("!ttsbird")) {
			MessageChannel chan = event.getChannel();
			String sender = event.getAuthor().getName();
			MessageAction action = chan.sendMessage(String.format("I can hear you well, %s!", sender));
			action.submit();
		}
	}
}
