package work.tinax.DiscordTTS;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class JapaneseTalk {
	private List<Path> createdTempFiles = new LinkedList<Path>();
	private static JapaneseTalk instance = null;
	
	private JapaneseTalk() {

	}
	
	public static synchronized JapaneseTalk getInstance() {
		if (instance == null) {
			instance = new JapaneseTalk();
		}
		return instance;
	}
	
	private final static String HTS_VOICE_PATH = "/usr/share/hts-voice/nitech-jp-atr503-m001/nitech_jp_atr503_m001.htsvoice";
	private final static String DICT_PATH = "/var/lib/mecab/dic/open-jtalk/naist-jdic";
	
	public synchronized Path createTTSFile(String msg) throws IOException, TTSException {
		makeTTSFile(msg);
		return Paths.get("~/output.wav");
	}
	
	private void makeTTSFile(String msg) throws IOException, TTSException {
		Runtime runtime = Runtime.getRuntime();
		String[] cmds = new String[] {
			"open_jtalk",
			"-s", "48000",
			"-p", "240",
			"-a", "0.55",
			"-m", HTS_VOICE_PATH,
			"-ow", "/home/tinaxd/output.wav",
			"-x", DICT_PATH
		};
		Process proc = runtime.exec(cmds);
		
		BufferedOutputStream stdin = new BufferedOutputStream(proc.getOutputStream());
		stdin.write(msg.getBytes());
		stdin.close();
		
		try {
			if (!proc.waitFor(5, TimeUnit.SECONDS)) {
				throw new TTSTimeoutException(msg);
			}
			System.out.print(new String(proc.getInputStream().readAllBytes()));
			System.err.print(new String(proc.getErrorStream().readAllBytes()));
			if (proc.exitValue() != 0) {
				throw new TTSException("exit code is not 0");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static FileAttribute<Set<PosixFilePermission>> defaultTmpPermission() {
		return PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r-----"));
	}
	
	@SuppressWarnings("unused")
	private Path makeInputTextFile(String msg) throws IOException {
		Path tempFilePath = Files.createTempFile("ttsbird-", null, defaultTmpPermission());
		createdTempFiles.add(tempFilePath);
		BufferedWriter bos = Files.newBufferedWriter(tempFilePath, StandardOpenOption.CREATE);
		bos.append(msg);
		bos.close();
		return tempFilePath;
	}
	
	public void disposeTempFiles() {
		for (Path tempFile : createdTempFiles) {
			try {
				Files.delete(tempFile);
			} catch (IOException e) {}
		}
		createdTempFiles.clear();
	}
}
