import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;

public class WikipediaWords {

	public static Option help = new Option("h", "help", false, "print this message");
	public static Option runTimeOption = Option.builder("t")
			.longOpt("run-time")
			.hasArg()
			.type(Double.class)
			.required()
			.desc("number of seconds to run for (float)")
			.build();
	public static Option numThreadsOption = Option.builder("n")
			.longOpt("num-threads")
			.hasArg()
			.type(Integer.class)
			.desc("number of threads to use (int)")
			.build();
	public static Option cleanupOption = Option.builder("c")
			.longOpt("cleanup")
			.hasArg(false)
			.desc("remove files after done?")
			.build();
	public static Option saveToFileOption = Option.builder("o")
			.longOpt("output")
			.hasArg(false)
			.desc("save sorted list to results file")
			.build();
	public static Options options = new Options()
		.addOption(help)
		.addOption(runTimeOption)
		.addOption(numThreadsOption)
		.addOption(cleanupOption)
		.addOption(saveToFileOption);
	public static CommandLineParser parser = new DefaultParser();


	public static void main(String... pumpkins) {

		// try {
		// 	for (int i = 1; i < 200; i++) {
		// 		WikipediaWordsRunner WWR = new WikipediaWordsRunner(1000f, i, false, false);
		// 		WWR.run();
		// 		Thread.sleep(1100000);
		// 	}
		// }	catch (Exception e) {}

		// System.exit(0);
		boolean printHelp = true;

		try {
			CommandLine line = parser.parse(options, pumpkins);
			if (!line.hasOption("help"))	{
				double runTime = Double.parseDouble(line.getOptionValue("run-time"));
				int numThreads = line.hasOption("num-threads") ? Integer.parseInt(line.getOptionValue("num-threads")):1;
				boolean cleanup = line.hasOption("cleanup");
				boolean saveToFile = line.hasOption("output");

				WikipediaWordsRunner WWR = new WikipediaWordsRunner(runTime, numThreads, cleanup, saveToFile);
				WWR.run();
				printHelp = false;
			}
		}	catch (MissingArgumentException maException) {
			System.err.printf("Argument required for %s!!!\n\n", maException.getOption().getOpt());
		}	catch (MissingOptionException moException) {
			System.err.printf("Runtime option required %s!!!\n\n", moException.getMissingOptions());
		}	catch (ParseException pException)	{
			System.err.println("Error parsing args\n");
			System.exit(51232);
		}	catch (NumberFormatException nfException) {
			System.err.println("Input format mismatch!!!\n");
			System.exit(51233);
		}
		if (printHelp)
			printHelpText();
	}

	public static void printHelpText() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("./run <params>", options);
	}
}

class WikipediaWordsRunner {

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

class WikipediaWordsThread extends Thread {

	private WordsHistogram wordsHistogram;
	private WordsHistogram headingsHistogram;
	private WordsHistogram titleWordsHistogram;
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
		wordsHistogram = new WordsHistogram();
		headingsHistogram = new WordsHistogram();
		titleWordsHistogram = new WordsHistogram();
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
		saveListToFile(printWriter, wordsHistogram.getWords());
		saveListToFile(printWriter, headingsHistogram.getWords());
		saveListToFile(printWriter, titleWordsHistogram.getWords());
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
			wordsHistogram.addWords(wikipediaPage.getWordsLowercase());
			headingsHistogram.addWords(wikipediaPage.getHeadings());
			titleWordsHistogram.addWords(wikipediaPage.getTitle().toLowerCase().split(" "));
		}	else articlesParsed--;
	}

	public static Document getRandomWikipediaArticle() {
		return PageLoader.getDocument("https://en.wikipedia.org/wiki/Special:Random");
	}

	public String getFileName() {
		return threadPath;
	}
}

class WordHistogram implements Comparable<WordHistogram> {

	private String word;
	private int	occurrences;

	public WordHistogram(String word, int occurrences) {
		this.word = word;
		this.occurrences = occurrences;
	}

	public WordHistogram(String word) {
		this(word, 1);
	}

	public void incrementOccurences(int increment) {
		occurrences += increment;
	}

	public String getWord() {
		return word;
	}

	public int getOccurrences() {
		return occurrences;
	}

	@Override
	public int compareTo(WordHistogram wordHistogram) {
		return word.compareTo(wordHistogram.getWord());
	}

	public int compareTo(String word) {
		return this.word.compareTo(word);
	}

	@Override
	public String toString() {
		return String.format("%30s - %,d", word, occurrences);
	}
}

class WordList extends ArrayList<WordHistogram> {
	public WordList() {
		super();
	}

	public int insertBinary(WordHistogram word) {
		int min = 0;
		int max = size() - 1;
		int mid = -1;
		int comparison = 0;
		while (min <= max) {
			mid = (min + max) / 2;
			comparison = get(mid).compareTo(word);
			if (comparison < 0)
				min = mid + 1;
			else if (comparison > 0)
				max = mid - 1;
			else {
				get(mid).incrementOccurences(word.getOccurrences());
				return mid;
			}
		}
		add(min, word);
		return min;
	}

	public int insert(WordHistogram word, int startIndex)	{
		int i = 0;
		int comparison = 0;
		for (i = startIndex; i < size(); i++) {
			comparison = get(i).compareTo(word);
			if (comparison < 0) continue;
			if (comparison == 0)
				get(i).incrementOccurences(word.getOccurrences());
			else add(i, word);
			return i;
		}
		add(word);
		return i;
	}

	public WordList sortOccurences() {
		Collections.sort(this, new Comparator<WordHistogram>() {
			@Override
			public int compare(WordHistogram word1, WordHistogram word2)	{
				return word2.getOccurrences() - word1.getOccurrences();
			}
		});
		return this;
	}
}

class WordsHistogram {
	private WordList words;

	public WordsHistogram() {
		words = new WordList();
	}

	public int addWord(WordHistogram wordHistogram, int startIndex) {
		return words.insert(wordHistogram, startIndex);
	}

	public int addWord(WordHistogram wordHistogram) {
		return words.insertBinary(wordHistogram);
	}

	public void addWords(String[] words) {
		Arrays.sort(words);
		int startIndex = 0;
		for (int i = 0; i < words.length; i++)
			startIndex = addWord(new WordHistogram(words[i]), startIndex);
	}

	public void addWords(WordList wordList) {
		for (WordHistogram wordHistogram : wordList)
			addWord(wordHistogram);
	}

	public void addWords(String[] words, int[] occurrences) {
		for (int i = 0; i < words.length; i++)
			addWord(new WordHistogram(words[i], occurrences[i]));
	}

	public int size() {
		return words.size();
	}

	public WordList getWords() {
		return words;
	}

	@Override
	public String toString() {
		String output = "";
		for (WordHistogram word : words)
			output += word + "\n";
		return output;
	}

	public String toString(int numWords) {
		String output = "";
		for (WordHistogram word : getTopWords(numWords))
			output += word + "\n";
		return output;
	}

	public ArrayList<WordHistogram> getTopWords(int numWords) {
		ArrayList<WordHistogram> topWords = new ArrayList<WordHistogram>(numWords);
		int		   topOccurences = 0;
		WordHistogram bestWord = null;
		for (int i = 0; i < numWords; i++) {
			topOccurences = 0;
			bestWord = null;
			for (WordHistogram word : words)
				if (word.getOccurrences() > topOccurences && !topWords.contains(word)) {
					topOccurences = word.getOccurrences();
					bestWord = word;
				}
			topWords.add(bestWord);
		}
		return topWords;
	}
}

class WikipediaPage {

	private String   title;
	private String[] headings;
	private String[] words, wordslc;
	private boolean badArticle;

	public WikipediaPage(Document article) {
		badArticle = false;
		parseWikipediaPage(article);
	}

	private void parseWikipediaPage(Document article) {
		try {
			Element content = article.getElementById("content");
			parseTitle(content);
			parseHeadings(content);
			parseWords(content);
		}	catch (NullPointerException e) {
			badArticle = true;
		}
	}

	private void parseTitle(Element content) {
		title = content.getElementById("firstHeading").text().replaceAll("[^A-Za-z0-9\\- \\']", "").replaceAll("  +", " ");
	}

	private void parseHeadings(Element content) {
		Elements headingsDOM = content.getElementsByClass("mw-headline");
		headings = new String[headingsDOM.size()];
		for (int i = 0; i < headings.length; i++)
			headings[i] = headingsDOM.eq(i).text().replaceAll("[^A-Za-z0-9\\- \\']", "").replaceAll("  +", " ");
	}

	private void parseWords(Element content) {
		String articleContents = content.select("#mw-content-text > p, #mw-content-text > ul").text();
		articleContents = articleContents.replaceAll("[^A-Za-z\\- \\']", "").replaceAll("  +", " ");
		words = articleContents.split(" ");
		wordslc = articleContents.toLowerCase().split(" ");
	}

	public boolean isValid() {
		return !badArticle;
	}

	public String getTitle() {
		return title;
	}

	public String[] getHeadings() {
		return headings;
	}

	public String[] getWords() {
		return words;
	}

	public String[] getWordsLowercase() {
		return wordslc;
	}
}