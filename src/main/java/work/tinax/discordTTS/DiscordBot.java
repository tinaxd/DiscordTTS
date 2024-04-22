package work.tinax.discordTTS;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordBot extends ListenerAdapter {
	private Set<GuildManager> onlineGuilds;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public DiscordBot() {
		onlineGuilds = new HashSet<>();
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		getOrCreateGuild(event.getGuild().getId()).onMessageReceived(event);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getName()) {
			case "join_tts" -> {
				getOrCreateGuild(event.getGuild().getId()).onJoinTTS(event);
			}
			case "leave_tts" -> {
				getOrCreateGuild(event.getGuild().getId()).onLeaveTTS(event);
			}
			default -> {
				logger.warn("Unknown command: {}", event.getName());
			}
		}
	}

	private GuildManager getOrCreateGuild(String guildId) {
		Optional<GuildManager> gm =
				onlineGuilds.stream().filter(g -> g.getGuildId().equals(guildId)).findFirst();
		if (gm.isPresent()) return gm.get();
		GuildManager newGm = new GuildManager(guildId);
		onlineGuilds.add(newGm);
		return newGm;
	}
}
