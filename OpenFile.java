import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

public class OpenFile {

	public static PrintWriter openFileToWrite(String path) {
		File file = new File(path);
		try {
			return new PrintWriter(file);
		}	catch (FileNotFoundException e) {
			return null;
		}
	}

	public static void writeToFile(String path, String contents) {
		PrintWriter pw = openFileToWrite(path);
		pw.print(contents);
		pw.close();
	}

	public static Scanner openFileToRead(String path) {
		File file = new File(path);
		if (file.exists() && !file.isDirectory()) {
			try {
				return new Scanner(file);
			}	catch (FileNotFoundException e) {}
		}
		return null;
	}

	public static BufferedReader openFileToReader(String path) {
		try {
			return new BufferedReader(new FileReader(path));
		}	catch (FileNotFoundException e) {
			return null;
		}
	}

	public static String readToFile(String path) {
		Scanner reader = openFileToRead(path);
		String contents = "";
		while (reader.hasNext())
			contents += reader.nextLine() + '\n';
		return contents;
	}
}