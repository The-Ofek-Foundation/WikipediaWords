import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

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
	private WordsHistogram wordsHistogram;
	private WordsHistogram headingsHistogram;
	private WordsHistogram titleWordsHistogram;
	private int     articlesParsed;
	private int     threadsCompleted, threadsParsed, threadsWritten;
	private double  startTime, elapsedTime;
	private boolean saveToFile;

	/**
	 * A constructor for WikipediaWords accepting the runTime in seconds,
	 * a numThreads to create and whether or not to saveToFile all the
	 * results.
	 * @param  runTime    The time in seconds to run the threads for.
	 * @param  numThreads The number of threads to create.
	 * @param  saveToFile Whether or not to save the results sorted onto files.
	 */
	public WikipediaWordsRunner(double runTime, int numThreads, boolean saveToFile) {
		wikipediaWordsThreads = new WikipediaWordsThread[numThreads];
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			wikipediaWordsThreads[i] = new WikipediaWordsThread(this, i, runTime);
		wordsHistogram = new WordsHistogram();
		headingsHistogram = new WordsHistogram();
		titleWordsHistogram = new WordsHistogram();
		articlesParsed = 0;
		threadsCompleted = threadsParsed = threadsWritten = 0;
		startTime = elapsedTime = 0;
		this.saveToFile = saveToFile;
	}

	/**
	 * Runs the program, creating and starting the threads.
	 */
	public void run() {
		System.out.printf("\n%-30s", "Parsing random articles...");
		startTime = System.nanoTime();
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
			System.out.printf("Done parsing %,d articles in %.1f seconds!\n", this.articlesParsed, elapsedTime);
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
	 * Saves the results of program execution to files.
	 */
	private void saveToFile() {
		String duplicatePrevention = "";
		// Prevents duplicates
		for (int i = 0; OpenFile.fileExists(String.format("results/article-words%s.txt", duplicatePrevention)); i++, duplicatePrevention = String.format("(%d)", i));
		saveListToOutput(String.format("results/article-words%s.txt", duplicatePrevention), wordsHistogram.getWords().sortOccurences(), "Words in article:");
		saveListToOutput(String.format("results/headings%s.txt", duplicatePrevention), headingsHistogram.getWords().sortOccurences(), "Headings:");
		saveListToOutput(String.format("results/title-words%s.txt", duplicatePrevention), titleWordsHistogram.getWords().sortOccurences(), "Words in titles:");
	}

	/**
	 * Used by {@link saveToFile} function to save a single wordList
	 * with a specific headingText to a file at a specific filePath.
	 * @param filePath    The path to where to save the file.
	 * @param wordList    A {@link WordList} array of words sorted by numOccurrences to save to the file.
	 * @param headingText A line of explanatory text to write to the top of the file.
	 */
	private void saveListToOutput(String filePath, WordList wordList, String headingText) {
		PrintWriter printWriter = OpenFile.openFileToWrite(filePath);
		printWriter.println(headingText);
		for (WordHistogram word : wordList)
			printWriter.println(word.getWord() + "\t" + word.getOccurrences());
		printWriter.close();
	}

	/**
	 * Deletes all used temp files in the project.
	 */
	private void cleanUp() {
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			OpenFile.deleteFile(wikipediaWordsThreads[i].getFileName());
	}

	/**
	 * Loads results obtained by {@link WikipediaWordThread}s
	 * after they are done saving their results to files. This
	 * greatly increases the communication speed for results.
	 */
	private void loadResultsFromFiles() {
		BufferedReader reader = null;
		for (int i = 0; i < wikipediaWordsThreads.length; i++) {
			reader = OpenFile.openFileToReader(wikipediaWordsThreads[i].getFileName());
			loadResultsFromFile(reader, wordsHistogram);
			loadResultsFromFile(reader, headingsHistogram);
			loadResultsFromFile(reader, titleWordsHistogram);
			try {
				reader.close();
			}	catch (IOException e) {}
		}
	}

	/**
	 * A helper method for {@link loadResultsFromFiles} that loads
	 * words onto a specific {@link WordsHistogram} from a
	 * {@link BufferedReads} reader.
	 * @param reader         A {@link BufferedReader} used to load results
	 *                       from a file.
	 * @param wordsHistogram A {@link WordsHistogram} array of words to save to.
	 */
	private void loadResultsFromFile(BufferedReader reader, WordsHistogram wordsHistogram) {
		try {
			int numWords = Integer.parseInt(reader.readLine());
			int currIndex = 0;
			for (int i = 0; i < numWords; i++)
				currIndex = 1 + wordsHistogram.addWord(new WordHistogram(reader.readLine(), Integer.parseInt(reader.readLine())), currIndex);
		}	catch (IOException e) {}
	}

	/**
	 * Prints top 10 results for words of all parsed articles.
	 */
	private void printResults() {
		System.out.printf("\nParsed %,d articles in %,.1f seconds!\n", articlesParsed, elapsedTime);
		System.out.printf("Parsed %,.2f articles per second!\n\n", articlesParsed / elapsedTime);

		System.out.printf("Top 10 words:\n%s\n", wordsHistogram.toString(10));
		System.out.printf("Top 10 headings:\n%s\n", headingsHistogram.toString(10));
		System.out.printf("Top 10 title words:\n%s\n", titleWordsHistogram.toString(10));
		OpenFile.appendToFile("results/threads.txt", String.format("%,d threads\t%,.0f seconds\t%,.1f articles per second\n", wikipediaWordsThreads.length, elapsedTime, articlesParsed / elapsedTime));
	}
}
