package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translate {

	public static final String ENGLISH = "en";
	public static final String VIETNAMESE = "vi";

	// outputName = "./database/geoqueries880vnnew"
	public static void translateDataToVietnamese(String outputName) {
		BufferedReader br = null;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputName, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		try {
			br = new BufferedReader(new FileReader("./database/geoqueries880"));
			String line = br.readLine();
			int count = 1;
			while (line != null) {
				System.out.println("Translating sentence number: " + count++);
				Pattern constituents = Pattern.compile("\\[.*\\]");
				Matcher matcher = constituents.matcher(line);
				matcher.find();
				String sentence = matcher.group();
				sentence = sentence.substring(1, sentence.length() - 1);
				writer.println(line.replace(
						sentence,
						translate(ENGLISH, VIETNAMESE,
								sentence.replaceAll(",", " "))));
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		writer.close();
	}

	public static String translate(String sl, String tl, String text)
			throws IOException {
		// fetch
		URL url = new URL(
				"http://translate.google.com.tw/translate_a/t?client=t&hl=en&sl="
						+ sl
						+ "&tl="
						+ tl
						+ "&ie=UTF-8&oe=UTF-8&multires=1&oc=1&otf=2&ssel=0&tsel=0&sc=1&q="
						+ URLEncoder.encode(text, "UTF-8"));
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("User-Agent", "Something Else");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				urlConnection.getInputStream()));
		String result = br.readLine();
		br.close();
		// parse
		// System.out.println(result);
		result = result.substring(2, result.indexOf("]]") + 1);
		StringBuilder sb = new StringBuilder();
		String[] splits = result.split("(?<!\\\\)\"");
		for (int i = 1; i < splits.length; i += 8)
			sb.append(splits[i]);
		return sb.toString().replace("\\n", "\n").replaceAll("\\\\(.)", "$1");
	}
}
