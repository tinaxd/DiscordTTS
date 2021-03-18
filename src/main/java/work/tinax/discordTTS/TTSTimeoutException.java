package work.tinax.discordTTS;

public class TTSTimeoutException extends TTSException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 902130811487243030L;

	public TTSTimeoutException(String msgToTTS) {
		super(String.format("timeout TTS: %s", msgToTTS));
	}
}
