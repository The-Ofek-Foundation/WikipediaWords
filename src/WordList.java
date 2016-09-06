import java.util.ArrayList;
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