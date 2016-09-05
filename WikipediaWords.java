import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class WikipediaWords {
	public static void main(String... pumpkins) {
		double runTime = pumpkins.length > 0 ? Double.parseDouble(pumpkins[0]):10f;
		int numThreads = pumpkins.length > 1 ? Integer.parseInt(pumpkins[1]):1;

		WikipediaWordsRunner WWR = new WikipediaWordsRunner(runTime, numThreads);
		WWR.run();
	}
}

class WikipediaWordsRunner {

	private WikipediaWordsThread[] wikipediaWordsThreads;
	private WordsHistogram wordsHistogram;
	private WordsHistogram headingsHistogram;
	private WordsHistogram titleWordsHistogram;
	private int            articlesParsed;
	private int            threadsCompleted;


	public WikipediaWordsRunner(double runTime, int numThreads) {
		wikipediaWordsThreads = new WikipediaWordsThread[numThreads];
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			wikipediaWordsThreads[i] = new WikipediaWordsThread(this, i, runTime);
		wordsHistogram = new WordsHistogram();
		headingsHistogram = new WordsHistogram();
		titleWordsHistogram = new WordsHistogram();
		articlesParsed = 0;
		threadsCompleted = 0;
	}

	public void run() {
		for (int i = 0; i < wikipediaWordsThreads.length; i++)
			wikipediaWordsThreads[i].start();
	}

	public synchronized void Results(WordsHistogram wordsHistogram, WordsHistogram headingsHistogram, WordsHistogram titleWordsHistogram, int articlesParsed) {
		this.wordsHistogram.addWords(wordsHistogram.getWords());
		this.headingsHistogram.addWords(headingsHistogram.getWords());
		this.titleWordsHistogram.addWords(titleWordsHistogram.getWords());
		this.articlesParsed += articlesParsed;
		threadsCompleted++;
		if (threadsCompleted == wikipediaWordsThreads.length)
			printResults();
	}

	public void printResults() {
		System.out.printf("\nParsed %d articles!\n\n", articlesParsed);

		System.out.printf("Top 10 words:\n%s\n", wordsHistogram.toString(10));
		System.out.printf("Top 10 headings:\n%s\n", headingsHistogram.toString(10));
		System.out.printf("Top 10 title words:\n%s\n", titleWordsHistogram.toString(10));
	}
}

class WikipediaWordsThread extends Thread {

	private WordsHistogram wordsHistogram;
	private WordsHistogram headingsHistogram;
	private WordsHistogram titleWordsHistogram;
	private int            articlesParsed;

	private Thread thread;
	private WikipediaWordsRunner runner;
	private int threadNum;
	private double runTime;

	WikipediaWordsThread(WikipediaWordsRunner runner, int threadNum, double runTime) {
		this.runner = runner;
		this.threadNum = threadNum;
		this.runTime = runTime;

		wordsHistogram = new WordsHistogram();
		headingsHistogram = new WordsHistogram();
		titleWordsHistogram = new WordsHistogram();
		articlesParsed = 0;
	}

	public void run() {
		for (double startTime = System.nanoTime(), elapsedTime = 0; elapsedTime < runTime; elapsedTime = (System.nanoTime() - startTime) / 1E9, articlesParsed++)
			parseRandomArticle();
		runner.Results(wordsHistogram, headingsHistogram, titleWordsHistogram, articlesParsed);
		// printResults();
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, "");
			thread.start();
		}
	}

	public void printResults() {
		System.out.printf("\nParsed %d articles!\n\n", articlesParsed);

		System.out.printf("Top 10 words:\n%s\n", wordsHistogram.toString(10));
		System.out.printf("Top 10 headings:\n%s\n", headingsHistogram.toString(10));
		System.out.printf("Top 10 title words:\n%s\n", titleWordsHistogram.toString(10));
	}

	public void parseRandomArticle() {
		Document doc = getRandomWikipediaArticle();
		WikipediaPage wikipediaPage = new WikipediaPage(doc);
		wordsHistogram.addWords(wikipediaPage.getWordsLowercase());
		headingsHistogram.addWords(wikipediaPage.getHeadings());
		titleWordsHistogram.addWords(wikipediaPage.getTitle().split(" "));
	}

	public static Document getRandomWikipediaArticle() {
		return PageLoader.getDocument("https://en.wikipedia.org/wiki/Special:Random");
	}
}

class WordHistogram implements Comparable<WordHistogram> {

	private String word;
	private int	occurrences;

	public WordHistogram(String word) {
		this.word = word;
		this.occurrences = 1;
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
		return String.format("%30s - %d", word, occurrences);
	}
}

class WordList extends ArrayList<WordHistogram> {
	public WordList() {
		super();
	}

	public void insert(WordHistogram word){
		for (int i = 0; i < size(); i++) {
			if (get(i).compareTo(word) < 0) continue;
			if (get(i).compareTo(word) == 0)
				get(i).incrementOccurences(word.getOccurrences());
			else add(i, word);
			return;
		}
		add(word);
	}
}

class WordsHistogram {
	private WordList words;

	public WordsHistogram() {
		words = new WordList();
	}

	public void addWord(WordHistogram wordHistogram) {
		words.insert(wordHistogram);
		// int index = binarySearch(word);
		// if (index == -1) {
		// }
	}

	public void addWord(String word) {
		addWord(new WordHistogram(word));
	}

	public int binarySearch(String word) {
		int min = 0;
		int max = words.size();
		int med = -1;
		while (min <= max) {
			med = (int)((min + max) / 2);
			if (words.get(med).compareTo(word) < 0)
				min = med + 1;
			else if (words.get(med).compareTo(word) > 0)
				max = med;
			else return med;
		}
		return -1;
	}

	public void addWords(String[] words) {
		for (int i = 0; i < words.length; i++)
			addWord(words[i]);
	}

	public void addWords(WordList wordList) {
		for (WordHistogram wordHistogram : wordList)
			addWord(wordHistogram);
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
		int           topOccurences = 0;
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

	public WikipediaPage(Document article) {
		parseWikipediaPage(article);
	}

	private void parseWikipediaPage(Document article) {
		Element content = article.getElementById("content");
		parseTitle(content);
		parseHeadings(content);
		parseWords(content);
	}

	private void parseTitle(Element content) {
		title = content.getElementById("firstHeading").text().replaceAll("[^A-Za-z\\- \\']", "").replaceAll("  +", " ");
	}

	private void parseHeadings(Element content) {
		Elements headingsDOM = content.getElementsByClass("mw-headline");
		headings = new String[headingsDOM.size()];
		for (int i = 0; i < headings.length; i++)
			headings[i] = headingsDOM.eq(i).text().replaceAll("[^A-Za-z\\- \\']", "").replaceAll("  +", " ");
	}

	private void parseWords(Element content) {
		String articleContents = content.select("#mw-content-text > p, #mw-content-text > ul").text();
		articleContents = articleContents.replaceAll("[^A-Za-z\\- \\']", "").replaceAll("  +", " ");
		words = articleContents.split(" ");
		wordslc = articleContents.toLowerCase().split(" ");
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