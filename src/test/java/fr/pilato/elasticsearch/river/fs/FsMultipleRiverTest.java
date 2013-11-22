/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.river.fs;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class FsMultipleRiverTest extends AbstractFsRiverTest {

	@Override
	public long waitingTime() throws Exception {
		return 5;
	}

	/**
	 * We use the default mapping
	 */
	@Override
	public String mapping() throws Exception {
		return null;
	}
	
	@Override @Before
	public void setUp() throws Exception {
		super.setUp();
		
		// In this case, we want to add another river
		// We update every five seconds
		int updateRate = 5 * 1000;
		String dir = "testfs2";
		
		// First we check that filesystem to be analyzed exists...
		File dataDir = new File("./target/test-classes/" + dir);
		if(!dataDir.exists()) {
			throw new RuntimeException("src/test/resources/" + dir + " doesn't seem to exist. Check your JUnit tests."); 
		}
		String url = dataDir.getAbsoluteFile().getAbsolutePath();
		
		XContentBuilder xb = jsonBuilder()
				.startObject()
					.field("type", "fs")
					.startObject("fs")
						.field("url", url)
						.field("update_rate", updateRate)
					.endObject()
					.startObject("index")
						.field("index", indexName())
						.field("type", "otherdocs")
						.field("bulk_size", 1)
					.endObject()
				.endObject();

		addARiver(dir, xb);
		
		// Let's wait x seconds 
		Thread.sleep(waitingTime() * 1000);
	}

	/**
	 * 
	 * <ul>
	 *   <li>TODO Fill the use case
	 * </ul>
	 */
	@Override
	public XContentBuilder fsRiver() throws Exception {
		// We update every ten seconds
		int updateRate = 10 * 1000;
		String dir = "testfs1";
		
		// First we check that filesystem to be analyzed exists...
		File dataDir = new File("./target/test-classes/" + dir);
		if(!dataDir.exists()) {
			throw new RuntimeException("src/test/resources/" + dir + " doesn't seem to exist. Check your JUnit tests."); 
		}
		String url = dataDir.getAbsoluteFile().getAbsolutePath();
		
		XContentBuilder xb = jsonBuilder()
				.startObject()
					.field("type", "fs")
					.startObject("fs")
						.field("url", url)
						.field("update_rate", updateRate)
					.endObject()
                    .startObject("index")
                        .field("index", indexName())
                        .field("type", "doc")
                        .field("bulk_size", 1)
                    .endObject()
				.endObject();
		return xb;
	}
	

	@Test
	public void index_must_have_two_files() throws Exception {
		// Let's search for entries
		CountResponse response = node.client().prepareCount(indexName()).setTypes("otherdocs","doc")
				.setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
		Assert.assertEquals("We should have two docs...", 2, response.getCount());
	}
}
