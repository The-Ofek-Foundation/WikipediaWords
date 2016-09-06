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