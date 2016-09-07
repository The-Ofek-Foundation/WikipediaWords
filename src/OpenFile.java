import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * OpenFile
 * Opens files to read/write conveniently.
 * @author Ofek Gila
 * @since August 2016
 * @version September 6th, 2016
 */
public class OpenFile {

	private OpenFile() {}

	/**
	 * Opens a file to write from a path.
	 * @param  path The relative path to the file.
	 * @return      A {@link PrintWriter} object with the open file.
	 */
	public static PrintWriter openFileToWrite(String path) {
		File file = new File(path);
		try {
			return new PrintWriter(file);
		}	catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * Writes contents to a file in a given path.
	 * @param path     The relative path to the file.
	 * @param contents The contents to write to the file.
	 * @return         Whether or not writing was successful.
	 */
	public static boolean writeToFile(String path, String contents) {
		PrintWriter pw = openFileToWrite(path);
		if (pw == null)
			return false;
		pw.print(contents);
		pw.close();
		return true;
	}

	/**
	 * Appends contents to file at path.
	 * @param path     The relative path to the file.
	 * @param contents The contents to append to the file.
	 * @return         Whether or not appending was successful.
	 */
	public static boolean appendToFile(String path, String contents) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path,true));
			bufferedWriter.write(contents);
			bufferedWriter.close();
			return true;
		}	catch (IOException e) {
			return false;
		}
	}

	/**
	 * Opens a file to a {@link Scanner} to read from a given path.
	 * @param  path The relative path to the file.
	 * @return      The opened file.
	 */
	public static Scanner openFileToRead(String path) {
		File file = new File(path);
		if (file.exists() && !file.isDirectory()) {
			try {
				return new Scanner(file);
			}	catch (FileNotFoundException e) {}
		}
		return null;
	}

	/**
	 * Opens a file to a {@link BufferedReader} object.
	 * @param  path The relative path to the file.
	 * @return      The opened reader object.
	 */
	public static BufferedReader openFileToReader(String path) {
		try {
			return new BufferedReader(new FileReader(path));
		}	catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * Reads contents of file from path.
	 * @param  path The relative path to the file.
	 * @return      The contents of the file.
	 */
	public static String readFromFile(String path) {
		Scanner reader = openFileToRead(path);
		String contents = "";
		while (reader.hasNext())
			contents += reader.nextLine() + '\n';
		return contents;
	}

	/**
	 * Deletes a file from a given path.
	 * @param  path The relative path to the file.
	 * @return      Whether or not the file was deleted.
	 */
	public static boolean deleteFile(String path) {
		File file = new File(path);
		return file.delete();
	}

	/**
	 * Checks if file exists at a given path.
	 * @param  path The relative path to the file.
	 * @return      Whether or not the file exists.
	 */
	public static boolean fileExists(String path) {
		File file = new File(path);
		return file.exists();
	}
}