import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * WikipediaPage
 * Parses a Wikipedia page (article) for its title,
 * headings, and content words.
 *
 * @author Ofek Gila
 * @since September 3rd, 2016
 * @version September 6th, 2016
 */
class WikipediaPage {

	private String   title;
	private String[] headings;
	private String[] words, wordslc;
	private boolean badArticle;

	/**
	 * Constructor with passed {@link Document} article.
	 * @param  article A {@link Document} element containing the html
	 *                 content of a Wikipedia article.
	 */
	public WikipediaPage(Document article) {
		badArticle = false;
		parseWikipediaPage(article);
	}

	/**
	 * Parses a Wikipedia page given a {@link Document} article.
	 * @param article A {@link Document} element containing the html
	 *                content of a Wikipedia article.
	 */
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

	/**
	 * Parses the title of a Wikipedia page given its content.
	 * @param content An {@link Element} object containing the content
	 *                of a Wikipedia page.
	 */
	private void parseTitle(Element content) {
		title = content.getElementById("firstHeading").text().replaceAll("[^\\w- ']", "").replaceAll("  +", " ").replaceAll("(?<=[^\\w])[-']+|[-']+(?=[^\\w])", "");
	}

	/**
	 * Parses the headings of a Wikipedia page given its content.
	 * @param content An {@link Element} object containing the content
	 *                of a Wikipedia page.
	 */
	private void parseHeadings(Element content) {
		Elements headingsDOM = content.getElementsByClass("mw-headline");
		headings = new String[headingsDOM.size()];
		for (int i = 0; i < headings.length; i++)
			headings[i] = headingsDOM.eq(i).text().replaceAll("[^\\w- ']", "").replaceAll("  +", " ").replaceAll("(?<=[^\\w])[-']+|[-']+(?=[^\\w])", "");
	}

	/**
	 * Parses the words of a Wikipedia page given its content.
	 * @param content An {@link Element} object containing the content
	 *                of a Wikipedia page.
	 */
	private void parseWords(Element content) {
		String articleContents = content.select("#mw-content-text p, #mw-content-text ul").text();
		articleContents = articleContents.replaceAll("[^A-Za-z ]", " ").replaceAll("  +", " ");
		words = articleContents.split(" ");
		wordslc = articleContents.toLowerCase().split(" ");
	}

	/**
	 * Returns the validity of the Wikipedia article.
	 * @return true if valid, false otherwise.
	 */
	public boolean isValid() {
		return !badArticle;
	}

	/**
	 * Returns the title of the Wikipedia article.
	 * @return the title of the Wikipedia article.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the headings of the Wikipedia article.
	 * @return the headings of the Wikipedia article.
	 */
	public String[] getHeadings() {
		return headings;
	}

	/**
	 * Returns the words of the Wikipedia article.
	 * @return the words of the Wikipedia article.
	 */
	public String[] getWords() {
		return words;
	}

	/**
	 * Returns the words lowercase of the Wikipedia article.
	 * @return the words lowercase of the Wikipedia article.
	 */
	public String[] getWordsLowercase() {
		return wordslc;
	}
}