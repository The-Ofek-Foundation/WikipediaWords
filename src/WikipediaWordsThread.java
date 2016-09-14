import java.io.BufferedWriter;
import java.io.IOException;
import org.jsoup.nodes.Document;

/**
 * WikipediaWordsThread
 *
 * A single {@link Thread} running the
 * WikipediaWords program.
 *
 * @author Ofek Gila
 * @since September 3rd, 2016
 * @version September 6th, 2016
 */
class WikipediaWordsThread extends Thread {

	private WordList wordsList;
	private WordList headingsList;
	private WordList titleWordList;
	private int      articlesParsed;

	private WikipediaWordsRunner runner;
	private Thread thread;
	private int    threadNum;
	private String threadName;
	private String threadPath;
	private double runTime;

	/**
	 * Constructor with an instance of a {@link WikipediaWordsRunner} runner,
	 * the threadNum of this thread, and the runTime.
	 *
	 * @param runner    An instance of the {@link WikipediaWordsRunner} runner.
	 * @param threadNum The number of this thread.
	 * @param runTime   The run time in seconds for this thread to find
	 *                  Wikipedia articles.
	 */
	WikipediaWordsThread(WikipediaWordsRunner runner, int threadNum, double runTime) {
		this.runner = runner;
		this.threadNum = threadNum;
		this.runTime = runTime;

		threadName = String.format("Thread Number - %d", threadNum);
		threadPath = "results/" + threadName + ".txt";
		wordsList = new WordList();
		headingsList = new WordList();
		titleWordList = new WordList();
		articlesParsed = 0;
	}

	/**
	 * Runs this thread by parsing random Wikipedia
	 * articles and saving the results to a file.
	 */
	public void run() {
		for (double startTime = System.nanoTime(), elapsedTime = 0; elapsedTime < runTime; elapsedTime = (System.nanoTime() - startTime) / 1E9, articlesParsed++)
			parseRandomArticle();
		runner.DoneParsing(articlesParsed);
		saveResultsToFile();
		runner.DoneWriting();
	}

	/**
	 * Function used to save the results from the lists
	 * to a file.
	 */
	private void saveResultsToFile() {
		BufferedWriter bufferedWriter = OpenFile.openFileToWriter(threadPath);
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
	 * Starts this thread following convention.
	 */
	public void start() {
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}

	/**
	 * Parses a random Wikipedia article using {@link WikipediaPage}
	 * and saves the parsed results to a {@link WordList} object.
	 */
	private void parseRandomArticle() {
		Document doc = getRandomWikipediaArticle();
		WikipediaPage wikipediaPage = new WikipediaPage(doc);
		if (wikipediaPage.isValid()) {
			wordsList.addWords(wikipediaPage.getWordsLowercase());
			headingsList.addWords(wikipediaPage.getHeadings());
			titleWordList.addWords(wikipediaPage.getTitle().toLowerCase().split(" "));
		}	else articlesParsed--;
	}

	/**
	 * Gets a random Wikipedia article by going to Wikipedia's
	 * Special:Random page.
	 * @return a {@link Document} object for the article.
	 */
	public static Document getRandomWikipediaArticle() {
		return PageLoader.getDocument("https://en.wikipedia.org/wiki/Special:Random");
	}

	/**
	 * Gets the path to the file where the results
	 * are stored.
	 * @return the path to the results file.
	 */
	public String getFileName() {
		return threadPath;
	}
}