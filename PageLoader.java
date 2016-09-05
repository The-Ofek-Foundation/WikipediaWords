import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * PageLoader.java
 * Load the HTML content of a page.
 * @author Ofek Gila
 * @since  September 4th, 2016
 */
public class PageLoader {
	public static final Pattern CONTENT_TYPE_REGEX = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");

	private PageLoader() {}

	/**
	 * Gets the html content of a page at a specific
	 * url path.
	 * @param  path The string path for the page to search
	 * @return      A string representation of the HTML content of the page
	 */
	private static String getHTMLContent(String path) {
		try {
			URL url = getURL(path);
			URLConnection con = url.openConnection();
			Matcher m = CONTENT_TYPE_REGEX.matcher(con.getContentType());
			/* If Content-Type doesn't match this pre-conception, choose default and
			 * hope for the best. */
			String charset = m.matches() ? m.group(1) : "ISO-8859-1";
			Reader r = new InputStreamReader(con.getInputStream(), charset);
			StringBuilder buf = new StringBuilder();
			while (true) {
				int ch = r.read();
				if (ch < 0)
					break;
				buf.append((char) ch);
			}
			return buf.toString();
		}	catch (IOException e) {
			return null;
		}
	}

	public static Document getDocument(String path) {
		try {
			return Jsoup.connect(path).get();
		}	catch (IOException e) {
			return null;
		}
	}

	/**
	 * Get a URL instance from a url path
	 * @param  urlPath The string path for the url
	 * @return         A URL instance
	 */
	public static URL getURL(String urlPath) {
		try {
			return new URL(urlPath);
		}	catch (MalformedURLException e) {
			return null;
		}
	}
}