package com.liwenjie.gmall1122.gmalllistservice;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	JestClient jestClient;

	@Test
	public void contextLoads() {

	}

	@Test
	public void TestJest() throws IOException {
		String query="{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"actorList.name\": \"张译\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie_type_chn").build();
		SearchResult searchResult = jestClient.execute(search);
		List<SearchResult.Hit<HashMap, Void>> resultHits = searchResult.getHits(HashMap.class);
		for (SearchResult.Hit<HashMap, Void> resultHit : resultHits) {
			HashMap source = resultHit.source;
			System.err.println("source :" + source);
		}

	}

}
