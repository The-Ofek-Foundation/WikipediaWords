import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class WikipediaWords {

	private WordsHistogram wordsHistogram;
	private WordsHistogram headingsHistogram;
	private WordsHistogram titleWordsHistogram;

	public WikipediaWords() {
		wordsHistogram = new WordsHistogram();
		headingsHistogram = new WordsHistogram();
		titleWordsHistogram = new WordsHistogram();
	}

	public static void main(String... pumpkins) {
		WikipediaWords WW = new WikipediaWords();
		WW.run();
	}

	public void run() {
		for (int i = 0; i < 10; i++)
			parseRandomArticle();
		// System.out.println(wordsHistogram);
		// System.out.println(String.join(", ", wordsHistogram.getTopWords(10)));
		printResults();
	}

	public void printResults() {
		System.out.println(wordsHistogram.toString(10));
		System.out.println(headingsHistogram.toString(10));
		System.out.println(titleWordsHistogram.toString(10));
	}

	public void parseRandomArticle() {
		Document doc = getRandomWikipediaArticle();
		WikipediaPage wikipediaPage = new WikipediaPage(doc);
		wordsHistogram.addWords(wikipediaPage.getWordsLowercase());
		headingsHistogram.addWords(wikipediaPage.getHeadings());
		titleWordsHistogram.addWords(wikipediaPage.getTitle().split(" "));
		// System.out.println(wikipediaPage.getTitle());
		// System.out.println(String.join(", ", wikipediaPage.getHeadings()));
		// System.out.println(String.join(", ", wikipediaPage.getWords()));
		// WordsHistogram wordHistogram = new WordsHistogram();
		// wordHistogram.addWords(wikipediaPage.getWords());
		// System.out.println(wordHistogram);
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

	public void incrementOccurences() {
		occurrences++;
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
		return String.format("%s - %d", word, occurrences);
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
				get(i).incrementOccurences();
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

	public void addWord(String word) {
		words.insert(new WordHistogram(word));
		// int index = binarySearch(word);
		// if (index == -1) {
		// }
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
		title = content.getElementById("firstHeading").text();
	}

	private void parseHeadings(Element content) {
		Elements headingsDOM = content.getElementsByClass("mw-headline");
		headings = new String[headingsDOM.size()];
		for (int i = 0; i < headings.length; i++)
			headings[i] = headingsDOM.eq(i).text();
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