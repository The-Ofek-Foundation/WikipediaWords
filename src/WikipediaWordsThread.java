import java.io.PrintWriter;
import org.jsoup.nodes.Document;

class WikipediaWordsThread extends Thread {

	private WordList wordsList;
	private WordList headingsList;
	private WordList titleWordList;
	private int			   articlesParsed;

	private WikipediaWordsRunner runner;
	private Thread thread;
	private int    threadNum;
	private String threadName;
	private String threadPath;
	private double runTime;

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

	public void run() {
		for (double startTime = System.nanoTime(), elapsedTime = 0; elapsedTime < runTime; elapsedTime = (System.nanoTime() - startTime) / 1E9, articlesParsed++)
			parseRandomArticle();
		runner.DoneParsing(articlesParsed);
		saveResultsToFile();
		runner.DoneWriting();
	}

	private void saveResultsToFile() {
		PrintWriter printWriter = OpenFile.openFileToWrite(threadPath);
		saveListToFile(printWriter, wordsList);
		saveListToFile(printWriter, headingsList);
		saveListToFile(printWriter, titleWordList);
		printWriter.close();
	}

	private void saveListToFile(PrintWriter printWriter, WordList wordList) {
		printWriter.println(wordList.size());
		for (WordHistogram wordHistogram : wordList)
			printWriter.print(wordHistogram.getWord() + "\n" + wordHistogram.getOccurrences() + "\n");
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}

	private void parseRandomArticle() {
		Document doc = getRandomWikipediaArticle();
		WikipediaPage wikipediaPage = new WikipediaPage(doc);
		if (wikipediaPage.isValid()) {
			wordsList.addWords(wikipediaPage.getWordsLowercase());
			headingsList.addWords(wikipediaPage.getHeadings());
			titleWordList.addWords(wikipediaPage.getTitle().toLowerCase().split(" "));
		}	else articlesParsed--;
	}

	public static Document getRandomWikipediaArticle() {
		return PageLoader.getDocument("https://en.wikipedia.org/wiki/Special:Random");
	}

	public String getFileName() {
		return threadPath;
	}
}