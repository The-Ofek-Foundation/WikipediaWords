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