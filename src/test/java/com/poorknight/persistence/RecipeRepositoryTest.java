package com.poorknight.persistence;

import static org.junit.Assert.assertTrue;

import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.poorknight.mongo.setup.MongoSetupHelper;

@RunWith(JUnit4.class)
public class RecipeRepositoryTest {

	private static MongoClient mongo;

	@BeforeClass
	public static void setup() throws Exception {
		mongo = MongoSetupHelper.startMongoInstance();
	}

	@AfterClass
	public static void teardown() {
		MongoSetupHelper.cleanupMongo();
	}

	@Before
	public void beforeEach() {
		final MongoDatabase db = mongo.getDatabase("test");
		final MongoCollection<Document> col = db.getCollection("testCol");
		col.insertOne(new Document("hi", "there"));
	}

	@Test
	public void integrationTestsAreExecuting() throws Exception {
		System.out.println("HI!");
		assertTrue(true);
	}

	@Test
	public void getDocument() throws Exception {
		final MongoDatabase db = mongo.getDatabase("test");
		final MongoCollection<Document> col = db.getCollection("testCol");
		final Document document = col.find().first();
		assertTrue(document.get("hi").equals("there"));
		// assertThat(document.get("hi"), equalTo("there"));
	}
}
