package work.tinax.DiscordTTS;

public class TTSException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6647211554466086367L;

	public TTSException(String msg) {
		super(msg);
	}
	
	public TTSException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
