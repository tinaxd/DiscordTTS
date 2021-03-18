package work.tinax.discordTTS;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
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
		htsVoicePath = System.getenv("HTS_VOICE_PATH");
		if (htsVoicePath == null || htsVoicePath.isBlank()) {
			throw new IllegalStateException("HTS_VOICE_PATH is not set");
		}
		naistDictDir = System.getenv("NAIST_DICT_DIR");
		if (naistDictDir == null || naistDictDir.isBlank()) {
			throw new IllegalStateException("NAIST_DICT_DIR is not set");
		}
		wavTargetPath = System.getenv("WAV_TARGET_PATH");
		if (wavTargetPath == null || wavTargetPath.isBlank()) {
			throw new IllegalStateException("WAV_TARGET_PATH is not set (must be an absolute path)");
		}
	}
	
	public static synchronized JapaneseTalk getInstance() {
		if (instance == null) {
			instance = new JapaneseTalk();
		}
		return instance;
	}
	
	private final String htsVoicePath;
	private final String naistDictDir;
	private final String wavTargetPath;
	
	public String getWavTargetPath() {
		return wavTargetPath;
	}
	
	public synchronized Path createTTSFile(String msg) throws IOException, TTSException {
		makeTTSFile(msg);
		return Paths.get(wavTargetPath);
	}
	
	private void makeTTSFile(String msg) throws IOException, TTSException {
		Runtime runtime = Runtime.getRuntime();
		String[] cmds = new String[] {
			"open_jtalk",
			"-s", "48000",
			"-p", "240",
			"-a", "0.55",
			"-m", htsVoicePath,
			"-ow", wavTargetPath,
			"-x", naistDictDir
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
