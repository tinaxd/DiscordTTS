package work.tinax.discordTTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public TrackScheduler(AudioPlayer player) {
		this.player = player;
	}
	
	public void playNow(AudioTrack track) {
		if (track == null) logger.debug("track is null");
		else logger.debug("playing track: " + track);
		player.startTrack(track, false);
	}
	
	/**
	 * @param player Audio player
	 */
	@Override
	public void onPlayerPause(AudioPlayer player) {
		logger.debug("player pause");
	}

	/**
	 * @param player Audio player
	 */
	@Override
	public void onPlayerResume(AudioPlayer player) {
		logger.debug("player resume");
	}

	/**
	 * @param player Audio player
	 * @param track  Audio track that started
	 */
	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		logger.debug("player start");
	}

	/**
	 * @param player    Audio player
	 * @param track     Audio track that ended
	 * @param endReason The reason why the track stopped playing
	 */
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		logger.debug("player track end");
	}

	/**
	 * @param player    Audio player
	 * @param track     Audio track where the exception occurred
	 * @param exception The exception that occurred
	 */
	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		logger.debug(exception.getMessage());
	}

	/**
	 * @param player      Audio player
	 * @param track       Audio track where the exception occurred
	 * @param thresholdMs The wait threshold that was exceeded for this event to
	 *                    trigger
	 */
	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		logger.debug("player stuck");
	}
}
