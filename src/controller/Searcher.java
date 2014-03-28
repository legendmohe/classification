package controller;

import java.util.ArrayList;

import model.SearchResult;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

public class Searcher {
	private IndexSearcher shareIndexSearcher;
	
	public Searcher(IndexReader reader) {
		setShareIndexSearcher(new IndexSearcher(reader));
	}

	public IndexSearcher getShareIndexSearcher() {
		return shareIndexSearcher;
	}

	public void setShareIndexSearcher(IndexSearcher shareIndexSearcher) {
		this.shareIndexSearcher = shareIndexSearcher;
	}
	
	public ArrayList<SearchResult> searchForQuery(String string, int limit) throws Exception {
		Query query = this.queryForString(string);
		TopDocs results = getShareIndexSearcher().search(query, limit); 
		ScoreDoc[] hits = results.scoreDocs; 
		ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();
		int start = 0;
		int end = results.totalHits < limit ? results.totalHits : limit;
		for (int i = start; i < end; i++) { 
			SearchResult newResult = new SearchResult();
			newResult.doc = hits[i].doc;
			newResult.score = hits[i].score;
			resultList.add(newResult);
		}
		
		return resultList;
	}
	
	private Query queryForString(String queryString) throws ParseException {
		Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_36, true);
		QueryParser queryParser = new QueryParser(Version.LUCENE_36, "content", analyzer);
		return queryParser.parse(queryString);
	}
}
