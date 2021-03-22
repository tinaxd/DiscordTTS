package work.tinax.discordTTS;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordBot extends ListenerAdapter {
	private Set<GuildManager> onlineGuilds;
	
	public DiscordBot() {
		onlineGuilds = new HashSet<>();
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		getOrCreateGuild(event.getGuild().getId()).onMessageReceived(event);
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
