package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class Utils {

	private static String workingDir;

	public static String getDir() {
		if (workingDir == null) {
			File s = new File(System.getProperty("user.dir"));
			if (s.canWrite())
				workingDir = s.getAbsolutePath();
			else
				workingDir = System.getProperty("user.home");
		}
		return workingDir;
	}

	public static String readFile(String path) {
		String content = null;
		File file = new File(path);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static void writeFile(String path, String text, boolean append) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(path, append));
			writer.write(text);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void runAfter(Runnable r, long millis) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				r.run();
			}
		}, millis);
	}

	public static short[] parseShortArray(String[] arr) {
		return parseShortArray(arr, 10);
	}

	public static short[] parseShortArray(String[] arr, int radix) {
		short[] res = new short[arr.length];
		int i = 0;
		try {
			for (i = 0; i < arr.length; ++i) {
				if (arr[i].length() == 0)
					continue;
				res[i] = (short) Integer.parseInt(arr[i], radix);
			}
		} catch (Exception e) {
			throw new RuntimeException(i + ": " + e.getMessage());
		}
		return res;
	}

	public static int getTerminalLines() {
		int lines;
		try {
			lines = Integer.parseInt(
					new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("tput lines").getInputStream()))
							.readLine());
		} catch (Exception e) {
			lines = 10;
		}
		return lines;
	}

	public static int getTerminalCols() {
		int cols;
		try {
			cols = Integer.parseInt(
					new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("tput cols").getInputStream()))
							.readLine());
		} catch (Exception e) {
			cols = 50;
		}
		return cols;
	}

}
