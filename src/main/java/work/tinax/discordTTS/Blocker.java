package work.tinax.discordTTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class Blocker {
	private final List<Pattern> blocks;
	
	public Blocker() {
		blocks = new ArrayList<>();
	}
	
	public Blocker(Collection<? extends Pattern> blockPatterns) {
		blocks = new ArrayList<>();
		blocks.addAll(blockPatterns);
	}
	
	public List<Pattern> getCurrentPatterns() {
		return blocks;
	}
	
	public void addPattern(Pattern pattern) {
		blocks.add(pattern);
	}
	
	public boolean testIfBlocked(String msg) {
		return blocks.parallelStream()
			          .anyMatch(p -> p.matcher(msg).find());
	}
	
	public boolean removePattern(int index) {
		if (index < 0 || index >= blocks.size()) {
			return false;
		}
		blocks.remove(index);
		return true;
	}
}
