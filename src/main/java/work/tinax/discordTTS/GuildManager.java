package work.tinax.discordTTS;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class GuildManager {
	private final BotAudioManager botAudioManager;
	private boolean inVoiceChannel = false;
	
	private final String guildId;
	private Blocker blocker;
	
	public GuildManager(String guildId) {
		botAudioManager = new BotAudioManager(guildId);
		this.guildId = guildId;
		blocker = BlockerFactory.emptyBlocker();
	}
	
	public String getGuildId() {
		return guildId;
	}
	
	public void onMessageReceived(MessageReceivedEvent event) {
		if (inVoiceChannel) {
			if (handleTextMessage(event)) return;
		}
		
		Message msg = event.getMessage();
		String[] raw = msg.getContentRaw().split(" ");
		for (int i=0; i<raw.length; i++) {
			raw[i] = raw[i].strip();
		}
		
		if (raw.length < 1) return;
		if (raw[0].equals("!ttsbird")) {
			MessageChannel chan = event.getChannel();
			if (raw.length > 1) {
				if (raw[1].equals("join")) {
					processJoin(event, raw);
				} else if (raw[1].equals("leave")) {
					processLeave(event);
				} else if (raw[1].equals("block")) {
					processBlock(event, raw);
				} else if (raw[1].equals("blocklist")) {
					processBlocklist(event);
				} else if (raw[1].equals("unblock")) {
					processUnblock(event, raw);
				} else {
					chan.sendMessage(String.format("%s っていう命令は知らないなー", raw[1])).submit();
				}
			} else {
				Member sender = event.getMember();
				MessageAction action = chan.sendMessage(String.format("%s くん、聞こえてるよ！", sender.getEffectiveName()));
				action.submit();
			}
		}
	}
	
	private void processJoin(MessageReceivedEvent event, String[] raw) {
		try {
			Guild guild = event.getGuild();
			List<VoiceChannel> voices = guild.getVoiceChannels();
			// search a voice channel with the sender
			Optional<VoiceChannel> vc = voices.stream().filter(ch -> ch.getMembers().contains(event.getMember()))
			                 .findFirst();
			if (vc.isEmpty()) {
				event.getChannel().sendMessage("ボイスチャンネルに入ってから呼んでね").submit();
			} else {
				//chan.sendMessage(String.format("Joining %s voice channel...", vc.get().getName())).submit();
				joinVoiceChannel(guild.getAudioManager(), vc.get());
			}
		} catch (IllegalStateException ex) {
			System.out.println("user sent from outside text channels");
		}
	}
	
	private void processLeave(MessageReceivedEvent event) {
		leaveVoiceChannel(event.getGuild().getAudioManager());
	}
	
	private void processBlock(MessageReceivedEvent event, String[] raw) {
		MessageChannel chan = event.getChannel();
		if (raw.length < 3) {
			chan.sendMessage("何をブロックしてほしいのかな？").submit();
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (int i=2; i<raw.length; i++) {
			sb.append(raw[i]);
		}
		String blockPattern = sb.toString().trim();
		try {
			Pattern pattern = Pattern.compile(blockPattern);
			blocker.addPattern(pattern);
			chan.sendMessage("ブロックしたよ").submit();
		} catch (PatternSyntaxException ex) {
			chan.sendMessage("正規表現にミスがあるよ").submit();
		}
	}
	
	private void processBlocklist(MessageReceivedEvent event) {
		List<String> patterns = blocker.getCurrentPatterns()
				.stream().map(Pattern::toString).collect(Collectors.toList());
		int size = patterns.size();
		StringBuilder response = new StringBuilder();
		for (int i=0; i<size; i++) {
			response.append('[');
			response.append(i);
			response.append("] ");
			response.append(patterns.get(i));
		}
		event.getChannel().sendMessage("ブロック一覧だよ").submit();
		event.getChannel().sendMessage(response.toString()).submit();
	}

	private void processUnblock(MessageReceivedEvent event, String[] raw) {
		MessageChannel chan = event.getChannel();
		if (raw.length < 3) {
			chan.sendMessage("どれをブロック解除すればいいの？").submit();
			return;
		}
		String arg = raw[2];
		try {
			int indexToRemove = Integer.parseInt(arg);
			if (blocker.removePattern(indexToRemove)) {
				chan.sendMessage("ブロック解除したよ").submit();
			} else {
				chan.sendMessage("ブロック解除失敗").submit();
			}
		} catch (NumberFormatException ex) {
			chan.sendMessage("どれをブロック解除するのか、整数で教えてね").submit();
		}
	}
	
	private void joinVoiceChannel(AudioManager audioManager, VoiceChannel voiceChannel) {
		audioManager.setSendingHandler(botAudioManager.getSendHandler());
		audioManager.openAudioConnection(voiceChannel);
		inVoiceChannel = true;
	}
	
	private void leaveVoiceChannel(AudioManager audioManager) {
		audioManager.closeAudioConnection();
		inVoiceChannel = false;
	}
	
	private boolean handleTextMessage(MessageReceivedEvent event) {
		String msg = event.getMessage().getContentStripped();
		if (msg.startsWith("!")) return false;
		try {
			if (!blocker.testIfBlocked(msg)) {
				String ttsMsg = event.getMember().getEffectiveName() + " " + msg;
				botAudioManager.playTTS(ttsMsg);
			}
		} catch (IOException | TTSException e) {
			try {
				event.getTextChannel().sendMessage("内部エラーだよ: " + e.getMessage());
			} catch (IllegalStateException ex) {/* not from text channels */}
		}
		return true;
	}
}
