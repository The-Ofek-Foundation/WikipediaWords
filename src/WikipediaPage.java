import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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