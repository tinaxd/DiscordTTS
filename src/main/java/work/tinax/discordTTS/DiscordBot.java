package work.tinax.discordTTS;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class DiscordBot extends ListenerAdapter {
	private final BotAudioManager botAudioManager;
	private boolean inVoiceChannel = false;
	
	public DiscordBot() {
		botAudioManager = new BotAudioManager();
	}
	
	@Override
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
			User sender = event.getAuthor();
			
			if (raw.length > 1) {
				if (raw[1].equals("join")) {
					try {
						Guild guild = event.getGuild();
						List<VoiceChannel> voices = guild.getVoiceChannels();
						// search a voice channel with the sender
						Optional<VoiceChannel> vc = voices.stream().filter(ch -> ch.getMembers().contains(event.getMember()))
						                 .findFirst();
						if (vc.isEmpty()) {
							chan.sendMessage("ボイスチャンネルに入ってから呼んでね").submit();
						} else {
							//chan.sendMessage(String.format("Joining %s voice channel...", vc.get().getName())).submit();
							joinVoiceChannel(guild.getAudioManager(), vc.get());
						}
					} catch (IllegalStateException ex) {
						System.out.println("user sent from outside text channels");
					}
				} else if (raw[1].equals("leave")) {
					leaveVoiceChannel(event.getGuild().getAudioManager());
				} else {
					chan.sendMessage(String.format("%s っていう命令は知らないなー", raw[1])).submit();
				}
			} else {
				MessageAction action = chan.sendMessage(String.format("%s くん、聞こえてるよ！", sender.getName()));
				action.submit();
			}
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
			String ttsMsg = event.getAuthor().getName() + " " + msg;
			System.out.println("TTS: " + ttsMsg);
			botAudioManager.playTTS(ttsMsg);
		} catch (IOException | TTSException e) {
			try {
				event.getTextChannel().sendMessage("内部エラーだよ: " + e.getMessage());
			} catch (IllegalStateException ex) {}
		}
		return true;
	}
}
