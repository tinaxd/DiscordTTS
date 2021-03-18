package work.tinax.discordTTS;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class BotAudioManager {
	private AudioPlayer player;
	private AudioPlayerManager playerManager;
	private TrackScheduler trackScheduler;
	private final JapaneseTalk tts;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public BotAudioManager() {
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(playerManager);
		player = playerManager.createPlayer();
		trackScheduler = new TrackScheduler(player);
		player.addListener(trackScheduler);
		tts = JapaneseTalk.getInstance();
	}
	
	private void loadAndPlayWavTrack() {
		playerManager.loadItem("/home/tinaxd/output.wav", new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				logger.debug("trackLoaded");
				trackScheduler.playNow(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				logger.debug("playlistLoaded - NOP");
			}

			@Override
			public void noMatches() {
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				exception.printStackTrace();
			}
			
		});
	}
	
	public void playTTS(String msg) throws IOException, TTSException {
		tts.createTTSFile(msg);
		loadAndPlayWavTrack();
	}
	
	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(player);
	}
}
