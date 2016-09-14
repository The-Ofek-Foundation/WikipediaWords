import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * WordList
 * Extends an {@link ArrayList} of {@link WordHistogram}s
 * to include sorted inserting and sorting function.
 * @author Ofek Gila
 * @since August 2016
 * @version September 6th, 2016
 */
class WordList extends ArrayList<WordHistogram> {

	/**
	 * The defaults constructor calling the
	 * {@link ArrayList} constructor.
	 */
	public WordList() {
		super();
	}

	public WordList(int capacity) {
		super(capacity);
	}

	/**
	 * Inserts a word into the sorted list using
	 * binary search.
	 * @param  word a {@link WordHistogram} word element to insert.
	 * @return      the inserted index.
	 */
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

	/**
	 * Inserts a word to the sorted list searching
	 * from a given startIndex.
	 * @param  word       a {@link WordHistogram} word element to insert.
	 * @param  startIndex The index to start searching from.
	 * @return            the inserted index.
	 */
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

	/**
	 * Adds a word to the histogram searching from a given startIndex.
	 * @param  word       a {@link WordHistogram} word element to insert.
	 * @param  startIndex The index to start searching from.
	 * @return            the inserted index.
	 */
	public int addWord(WordHistogram word, int startIndex) {
		return insert(word, startIndex);
	}

	/**
	 * Adds a word to the histogram using binary search.
	 * @param  word a {@link WordHistogram} word element to insert.
	 * @return      the inserted index.
	 */
	public int addWord(WordHistogram word) {
		return insertBinary(word);
	}

	/**
	 * Adds an array of words to the list.
	 * @param words the array of words to add
	 */
	public void addWords(String[] words) {
		Arrays.sort(words);
		int startIndex = 0;
		for (int i = 0; i < words.length; i++)
			startIndex = addWord(new WordHistogram(words[i]), startIndex);
	}

	/**
	 * Adds words from a wordList.
	 * @param wordList a {@link WordList} object.
	 */
	public void addWords(WordList wordList) {
		int startIndex = 0;
		for (WordHistogram wordHistogram : wordList)
			startIndex = 1 + addWord(wordHistogram, startIndex);
	}

	/**
	 * Adds words given words and occurrences to list.
	 * @param words       an array of words.
	 * @param occurrences an array of number occurrences.
	 */
	public void addWords(String[] words, int[] occurrences) {
		for (int i = 0; i < words.length; i++)
			addWord(new WordHistogram(words[i], occurrences[i]));
	}

	/**
	 * Prints a list of the words.
	 * @return String list of the words.
	 */
	@Override
	public String toString() {
		String output = "";
		for (WordHistogram word : this)
			output += word + "\n";
		return output;
	}

	/**
	 * Prints the first numWords most common words from list.
	 * @param  numWords A number of words to print.
	 * @return          String list of the words.
	 */
	public String toString(int numWords) {
		String output = "";
		for (WordHistogram word : getTopWords(numWords))
			output += word + "\n";
		return output;
	}

	/**
	 * Finds the numWords most common words from the list.
	 * @param  numWords A number of words to find.
	 * @return          An {@link ArrayList} of {@link WordHistogram}s containing the words in order.
	 */
	public ArrayList<WordHistogram> getTopWords(int numWords) {
		ArrayList<WordHistogram> topWords = new ArrayList<WordHistogram>(numWords);
		int		   topOccurences = 0;
		WordHistogram bestWord = null;
		for (int i = 0; i < numWords; i++) {
			topOccurences = 0;
			bestWord = null;
			for (WordHistogram word : this)
				if (word.getOccurrences() > topOccurences && !topWords.contains(word)) {
					topOccurences = word.getOccurrences();
					bestWord = word;
				}
			topWords.add(bestWord);
		}
		return topWords;
	}

	/**
	 * Sorts this list buy number of occurrences,
	 * rendering it useless for future adding words.
	 * @return A {@link WordList} instance of itself after sorting.
	 */
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