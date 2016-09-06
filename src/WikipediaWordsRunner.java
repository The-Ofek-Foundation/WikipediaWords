import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class WikipediaWordsRunner {

	private WikipediaWordsThread[] wikipediaWordsThreads;
	private WordsHistogram wordsHistogram;
	private WordsHistogram headingsHistogram;
	private WordsHistogram titleWordsHistogram;
	private int			articlesParsed;
	private int			threadsCompleted, threadsParsed, threadsWritten;
	private double		 startTime, elapsedTime;
	private boolean		cleanup;
	private boolean		saveToFile;


	public WikipediaWordsRunner(double runTime, int numThreads, boolean cleanup, boolean saveToFile) {
		wikipediaWordsThreads = new WikipediaWordsThread[numThreads];
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			wikipediaWordsThreads[i] = new WikipediaWordsThread(this, i, runTime);
		wordsHistogram = new WordsHistogram();
		headingsHistogram = new WordsHistogram();
		titleWordsHistogram = new WordsHistogram();
		articlesParsed = 0;
		threadsCompleted = threadsParsed = threadsWritten = 0;
		startTime = elapsedTime = 0;
		this.cleanup = cleanup;
		this.saveToFile = saveToFile;
	}

	public void run() {
		System.out.printf("\n%-30s", "Parsing random articles...");
		startTime = System.nanoTime();
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			wikipediaWordsThreads[i].start();
	}

	public synchronized void Results(WordsHistogram wordsHistogram, WordsHistogram headingsHistogram, WordsHistogram titleWordsHistogram, int articlesParsed) {
		this.wordsHistogram.addWords(wordsHistogram.getWords());
		this.headingsHistogram.addWords(headingsHistogram.getWords());
		this.titleWordsHistogram.addWords(titleWordsHistogram.getWords());
		this.articlesParsed += articlesParsed;
		threadsCompleted++;
		elapsedTime = (System.nanoTime() - startTime) / 1E9;
		if (threadsCompleted == wikipediaWordsThreads.length)
			printResults();
		else System.out.printf("\nParsed %,d articles in %,.1f seconds!", this.articlesParsed, elapsedTime);
	}

	public synchronized void DoneParsing(int articlesParsed) {
		this.articlesParsed += articlesParsed;
		threadsParsed++;
		double elapsedTime = (System.nanoTime() - startTime) / 1E9;
		if (threadsParsed == wikipediaWordsThreads.length) {
			System.out.printf("Done parsing %,d articles in %.1f seconds!\n", this.articlesParsed, elapsedTime);
			System.out.printf("%-30s", "Writing results...");
		}
	}

	public synchronized void DoneWriting() {
		threadsWritten++;
		double elapsedTime = (System.nanoTime() - startTime) / 1E9;
		if (threadsWritten == wikipediaWordsThreads.length) {
			System.out.printf("Done writing in %.1f seconds!\n", elapsedTime);
			System.out.printf("%-30s", "Loading results...");
			loadResultsFromFiles();
			this.elapsedTime = (System.nanoTime() - startTime) / 1E9;
			System.out.printf("Done loading in %.1f seconds!\n", this.elapsedTime);
			if (cleanup) {
				System.out.printf("%-30s", "Cleaning up...");
				cleanUp();
				this.elapsedTime = (System.nanoTime() - startTime) / 1E9;
				System.out.printf("Done cleaning in %.1f seconds!\n", this.elapsedTime);
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

	private void saveToFile() {
		String duplicatePrevention = "";
		// Prevents duplicates
		for (int i = 0; OpenFile.fileExists(String.format("results/article-words%s.txt", duplicatePrevention)); i++, duplicatePrevention = String.format("(%d)", i));
		saveListToOutput(String.format("results/article-words%s.txt", duplicatePrevention), wordsHistogram.getWords().sortOccurences(), "Words in article:");
		saveListToOutput(String.format("results/headings%s.txt", duplicatePrevention), headingsHistogram.getWords().sortOccurences(), "Headings:");
		saveListToOutput(String.format("results/title-words%s.txt", duplicatePrevention), titleWordsHistogram.getWords().sortOccurences(), "Words in titles:");
	}

	private void saveListToOutput(String filePath, WordList wordList, String headingText) {
		PrintWriter printWriter = OpenFile.openFileToWrite(filePath);
		printWriter.println(headingText);
		for (WordHistogram word : wordList)
			printWriter.println(word.getWord() + "\t" + word.getOccurrences());
		printWriter.close();
	}

	private void cleanUp() {
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			OpenFile.deleteFile(wikipediaWordsThreads[i].getFileName());
	}

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

	private void loadResultsFromFile(BufferedReader reader, WordsHistogram wordsHistogram) {
		try {
			int numWords = Integer.parseInt(reader.readLine());
			int currIndex = 0;
			for (int i = 0; i < numWords; i++)
				currIndex = 1 + wordsHistogram.addWord(new WordHistogram(reader.readLine(), Integer.parseInt(reader.readLine())), currIndex);
		}	catch (IOException e) {}
	}

	private void printResults() {
		System.out.printf("\nParsed %,d articles in %,.1f seconds!\n", articlesParsed, elapsedTime);
		System.out.printf("Parsed %,.2f articles per second!\n\n", articlesParsed / elapsedTime);

		System.out.printf("Top 10 words:\n%s\n", wordsHistogram.toString(10));
		System.out.printf("Top 10 headings:\n%s\n", headingsHistogram.toString(10));
		System.out.printf("Top 10 title words:\n%s\n", titleWordsHistogram.toString(10));
		OpenFile.appendToFile("results/threads.txt", String.format("%,d threads\t%,.0f seconds\t%,.1f articles per second\n", wikipediaWordsThreads.length, elapsedTime, articlesParsed / elapsedTime));
	}
}
