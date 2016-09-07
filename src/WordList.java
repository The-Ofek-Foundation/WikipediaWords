import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;

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

	public int addWord(WordHistogram wordHistogram, int startIndex) {
		return insert(wordHistogram, startIndex);
	}

	public int addWord(WordHistogram wordHistogram) {
		return insertBinary(wordHistogram);
	}

	public void addWords(String[] words) {
		Arrays.sort(words);
		int startIndex = 0;
		for (int i = 0; i < words.length; i++)
			startIndex = addWord(new WordHistogram(words[i]), startIndex);
	}

	public void addWords(WordList wordList) {
		int startIndex = 0;
		for (WordHistogram wordHistogram : wordList)
			startIndex = 1 + addWord(wordHistogram, startIndex);
	}

	public void addWords(String[] words, int[] occurrences) {
		for (int i = 0; i < words.length; i++)
			addWord(new WordHistogram(words[i], occurrences[i]));
	}

	@Override
	public String toString() {
		String output = "";
		for (WordHistogram word : this)
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
			for (WordHistogram word : this)
				if (word.getOccurrences() > topOccurences && !topWords.contains(word)) {
					topOccurences = word.getOccurrences();
					bestWord = word;
				}
			topWords.add(bestWord);
		}
		return topWords;
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