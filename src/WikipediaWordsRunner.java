import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedWriter;

/**
 * WikipediaWords
 *
 * This program creates multiple threads to run the
 * WikipediaWords program.
 *
 * @author Ofek Gila
 * @since September 3rd, 2016
 * @version September 6th, 2016
 */

public class WikipediaWordsRunner {

	private WikipediaWordsThread[] wikipediaWordsThreads;
	private WordList wordsList;
	private WordList headingsList;
	private WordList titleWordList;
	private int     articlesParsed;
	private int     threadsCompleted, threadsParsed, threadsWritten;
	private double  startTime, elapsedTime;
	private boolean saveToFile;
	private boolean cumulative;

	/**
	 * A constructor for WikipediaWords accepting the runTime in seconds,
	 * a numThreads to create and whether or not to saveToFile all the
	 * results.
	 * @param  runTime    The time in seconds to run the threads for.
	 * @param  numThreads The number of threads to create.
	 * @param  saveToFile Whether or not to save the results sorted onto files.
	 */
	public WikipediaWordsRunner(double runTime, int numThreads, boolean saveToFile, boolean cumulative) {
		wikipediaWordsThreads = new WikipediaWordsThread[numThreads];
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			wikipediaWordsThreads[i] = new WikipediaWordsThread(this, i, runTime);
		wordsList = new WordList();
		headingsList = new WordList();
		titleWordList = new WordList();
		articlesParsed = 0;
		threadsCompleted = threadsParsed = threadsWritten = 0;
		startTime = elapsedTime = 0;
		this.saveToFile = saveToFile;
		this.cumulative = cumulative;
	}

	/**
	 * Runs the program, creating and starting the threads.
	 */
	public void run() {
		System.out.println();
		startTime = System.nanoTime();
		if (cumulative) {
			System.out.printf("%-30s", "Loading previous results...");
			if (OpenFile.fileExists("results/cumulative-results.txt"))
				loadResultsFromFile("results/cumulative-results.txt", true);
			elapsedTime = (System.nanoTime() - startTime) / 1E9;
			System.out.printf("Done loading in %.1f seconds!\n", elapsedTime);
		}
		System.out.printf("%-30s", "Parsing random articles...");
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			wikipediaWordsThreads[i].start();
	}

	/**
	 * Called by individual threads to notify completion of
	 * parsing articles (after specified time).
	 * @param articlesParsed The number of articles successfully
	 *                       parsed by the thread.
	 */
	public synchronized void DoneParsing(int articlesParsed) {
		this.articlesParsed += articlesParsed;
		threadsParsed++;
		double elapsedTime = (System.nanoTime() - startTime) / 1E9;
		if (threadsParsed == wikipediaWordsThreads.length) {
			System.out.printf("Done parsing in %.1f seconds!\n", elapsedTime);
			System.out.printf("%-30s", "Writing results...");
		}
	}

	/**
	 * Called by individual threads to notify completion of
	 * writing the results to a file.
	 */
	public synchronized void DoneWriting() {
		threadsWritten++;
		double elapsedTime = (System.nanoTime() - startTime) / 1E9;
		if (threadsWritten == wikipediaWordsThreads.length) {
			System.out.printf("Done writing in %.1f seconds!\n", elapsedTime);
			System.out.printf("%-30s", "Loading results...");
			loadResultsFromFiles();
			this.elapsedTime = (System.nanoTime() - startTime) / 1E9;
			System.out.printf("Done loading in %.1f seconds!\n", this.elapsedTime);
			System.out.printf("%-30s", "Cleaning up...");
			cleanUp();
			this.elapsedTime = (System.nanoTime() - startTime) / 1E9;
			System.out.printf("Done cleaning in %.1f seconds!\n", this.elapsedTime);
			if (cumulative) {
				System.out.printf("%-30s", "Saving to cumulative file...");
				saveToCumulativeFile();
				this.elapsedTime = (System.nanoTime() - startTime) / 1E9;
				System.out.printf("Done saving in %.1f seconds!\n", this.elapsedTime);

			}
			if (saveToFile) {
				System.out.printf("%-30s", "Saving results to file...");
				saveToFile();
				this.elapsedTime = (System.nanoTime() - startTime) / 1E9;
				System.out.printf("Done saving in %.1f seconds!\n", this.elapsedTime);
			}
			printResults();
		}
	}

	/**
	 * Function used to save the results from the lists
	 * to a file.
	 */
	private void saveToCumulativeFile() {
		BufferedWriter bufferedWriter = OpenFile.openFileToWriter("results/cumulative-results.txt");
		saveListToFile(bufferedWriter, wordsList);
		saveListToFile(bufferedWriter, headingsList);
		saveListToFile(bufferedWriter, titleWordList);
		try {
			bufferedWriter.close();
		}	catch (IOException e) {}
	}

	/**
	 * Used to save results from a single list to a file.
	 * @param bufferedWriter The {@link BufferedWriter} object used for
	 *                    writing to the list.
	 * @param wordList    The {@link WordList} object containing the
	 *                    list.
	 */
	private void saveListToFile(BufferedWriter bufferedWriter, WordList wordList) {
		try {
			bufferedWriter.write(wordList.size() + "\n");
			for (WordHistogram wordHistogram : wordList)
				bufferedWriter.write(wordHistogram.getWord() + "\n" + wordHistogram.getOccurrences() + "\n");
		}	catch (IOException e) {}
	}

	/**
	 * Saves the results of program execution to files.
	 */
	private void saveToFile() {
		String duplicatePrevention = "";
		// // Prevents duplicates
		// for (int i = 0; OpenFile.fileExists(String.format("results/article-words%s.txt", duplicatePrevention)); i++, duplicatePrevention = String.format("(%d)", i));
		saveListToOutput(String.format("results/article-words%s.txt", duplicatePrevention), wordsList.sortOccurences(), "Words in article:");
		saveListToOutput(String.format("results/headings%s.txt", duplicatePrevention), headingsList.sortOccurences(), "Headings:");
		saveListToOutput(String.format("results/title-words%s.txt", duplicatePrevention), titleWordList.sortOccurences(), "Words in titles:");
	}

	/**
	 * Used by the saveToFile function to save a single wordList
	 * with a specific headingText to a file at a specific filePath.
	 * @param filePath    The path to where to save the file.
	 * @param wordList    A {@link WordList} array of words sorted by numOccurrences to save to the file.
	 * @param headingText A line of explanatory text to write to the top of the file.
	 */
	private void saveListToOutput(String filePath, WordList wordList, String headingText) {
		BufferedWriter bufferedWriter = OpenFile.openFileToWriter(filePath);
		try {
			bufferedWriter.write(headingText + "\n");
			for (WordHistogram word : wordList)
				bufferedWriter.write(String.format("%-15s %,d\n", word.getWord(), word.getOccurrences()));
			bufferedWriter.close();
		}	catch (IOException e) {}
	}

	/**
	 * Deletes all used temp files in the project.
	 */
	private void cleanUp() {
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			OpenFile.deleteFile(wikipediaWordsThreads[i].getFileName());
	}

	/**
	 * Loads results obtained by {@link WikipediaWordsThread}s
	 * after they are done saving their results to files. This
	 * greatly increases the communication speed for results.
	 */
	private void loadResultsFromFiles() {
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			loadResultsFromFile(wikipediaWordsThreads[i].getFileName(), false);
	}

	private void loadResultsFromFile(String filePath, boolean overrideList) {
		BufferedReader reader = OpenFile.openFileToReader(filePath);
		wordsList = loadResultFromFile(reader, wordsList, overrideList);
		headingsList = loadResultFromFile(reader, headingsList, overrideList);
		titleWordList = loadResultFromFile(reader, titleWordList, overrideList);
		try {
			reader.close();
		}	catch (IOException e) {}
	}

	/**
	 * A helper method for loadResultsFromFiles that loads
	 * words onto a specific {@link WordList} from a
	 * {@link BufferedReader} reader.
	 * @param reader         A {@link BufferedReader} used to load results
	 *                       from a file.
	 * @param wordsList A {@link WordList} array of words to save to.
	 */
	private WordList loadResultFromFile(BufferedReader reader, WordList wordsList, boolean overrideList) {
		try {
			WordList tempList = wordsList;
			int numWords = Integer.parseInt(reader.readLine());
			if (overrideList)
				tempList = new WordList(numWords);
			int currIndex = 0;
			for (int i = 0; i < numWords; i++)
				currIndex = 1 + tempList.addWord(new WordHistogram(reader.readLine(), Integer.parseInt(reader.readLine())), currIndex);
			return tempList;
		}	catch (IOException e) {}	catch (NumberFormatException e) {
			System.err.println("Misformatted file, skipping");
		}
		return null;
	}

	/**
	 * Prints top 10 results for words of all parsed articles.
	 */
	private void printResults() {
		System.out.printf("\nParsed %,d articles in %,.1f seconds!\n", articlesParsed, elapsedTime);
		System.out.printf("Parsed %,.2f articles per second!\n\n", articlesParsed / elapsedTime);

		System.out.printf("Top 10 words:\n%s\n", wordsList.toString(10));
		System.out.printf("Top 10 headings:\n%s\n", headingsList.toString(10));
		System.out.printf("Top 10 title words:\n%s\n", titleWordList.toString(10));
		OpenFile.appendToFile("results/threads.txt", String.format("%,d threads\t%,.0f seconds\t%,.1f articles per second\n", wikipediaWordsThreads.length, elapsedTime, articlesParsed / elapsedTime));
	}
}
