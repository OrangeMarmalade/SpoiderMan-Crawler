package WebCrawler.SpoiderMan;

import java.io.IOException;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.*;
import com.google.common.hash.*;


// given a URL to a web page and a word to search for, spoiderman will
// do whatever a spoiderman does and fetch  all words on the page as all URL on page
// if word isn't found on that page, it will go to the next page and repeat
// etc...
public class SpoiderMan {
	private static final int MAX_PAGES_TO_SEARCH = 10;
	//private Set<String> pagesVisited = new HashSet<String>();
	private List<String> pagesToVisit = new LinkedList<String>();
	
	public static SpoiderMan killUncleBen() {
		return new SpoiderMan();
	}
	
	private SpoiderMan() {
		
	}
	
	private BloomFilter<String> pagesVisited = BloomFilter.create(new Funnel<String>(){
			public void funnel(String input, PrimitiveSink into) {
				into 
					.putString(input, Charsets.UTF_8);
			}
		}, 10000,0.001);
	
	private String nextUrl() {
		String nextUrl;
		
		do {
			nextUrl = this.pagesToVisit.remove(0);
			
		} while (pagesVisited.mightContain(nextUrl));
		
		pagesVisited.put(nextUrl);
		return nextUrl;
	}
	
	public void search(String url, String searchWord) {
		while (true) {
			String currentUrl;
			if (pagesToVisit.isEmpty()) {
				currentUrl = url;
				pagesVisited.put(url);
			} else {
				currentUrl = nextUrl();
			}
			crawl(currentUrl);
			boolean success = searchForWord(searchWord);
			
			if(success) {
				System.out.println(String.format("**Success** Word is %s found at %s", searchWord, currentUrl));
				break;
			}
			pagesToVisit.addAll(getLinks());
		}
		System.out.println(String.format("**Done** No more pages to traverse"));
	}
	
	private List<String> links = new LinkedList<String>();
	
	private Document htmlDoc;
	
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) "
			+ "AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";;
	
	
	// give it a url and spoiderman will start crawling
	// makes HTTP request for a web page
	public void crawl(String url) {
		try {
			Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
			Document htmlDocument = connection.get();
			
			htmlDoc = htmlDocument;
			
			System.out.println("Received web page at " + url);
			
			Elements linksOnPage = htmlDocument.select("a[href]");
			System.out.println("Found (" + linksOnPage.size() + ") links");
			
			for (Element link : linksOnPage) {
				links.add(link.absUrl("href"));
			}
			
		} catch(IOException ioe) {
			System.out.println("Error in out HTTP request " + ioe);
		}
		
	}
	
	// tries to find a word on the page
	public boolean searchForWord(String word) {
		System.out.println("Searching for the word " + word + "...");
		String bodyText = htmlDoc.body().text();
		return bodyText.toLowerCase().contains(word.toLowerCase());
	}
	
	public List<String> getLinks() {
		return links;
	}
	
}
