package com.poorknight.user;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.poorknight.application.init.DatabaseSetup;
import com.poorknight.mongo.setup.MongoSetupHelper;
import com.poorknight.user.save.NonUniqueEmailException;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class MongoUserRepositoryTest {

	private static MongoClient mongo;
	private UserRepository userRepository;
	private final UserTranslator userTranslator = new UserTranslator();

	@BeforeClass
	public static void setupMongo() throws Exception {
		mongo = MongoSetupHelper.startMongoInstance();
	}

	@AfterClass
	public static void teardown() {
		MongoSetupHelper.cleanupMongo();
	}

	@Before
	public void setup() {
		userRepository = new MongoUserRepository(mongo, userTranslator);
	}

	@After
	public void tearDown() {
		MongoSetupHelper.deleteAllUsers();
	}

	@Test
	public void savesAndGetsBasicUser() throws Exception {
		final User userToSave = new User("name", "email");

		final User savedUser = userRepository.saveNewUser(userToSave);
		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getName()).isEqualTo("name");
		assertThat(savedUser.getEmail()).isEqualTo("email");

		final User foundUser = userRepository.findUserById(savedUser.getId());
		assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
		assertThat(foundUser.getName()).isEqualTo("name");
		assertThat(foundUser.getEmail()).isEqualTo("email");
	}

	@Test
	public void canFindUserByEmail() throws Exception {
		final User userToSave = new User("thename", "theBest@EmailEver.com");
		final User savedUser = userRepository.saveNewUser(userToSave);

		final User foundUser = userRepository.findUserByEmail("theBest@EmailEver.com");
		assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
		assertThat(foundUser.getEmail()).isEqualTo("theBest@EmailEver.com");
		assertThat(foundUser.getName()).isEqualTo("thename");
	}

	@Test
	public void findUserByEmail_IsCaseSensitive() throws Exception {
		final User userToSave = new User("thename", "theBest@EmailEver.com");
		userRepository.saveNewUser(userToSave);

		final User foundUser = userRepository.findUserByEmail("thebest@emailever.com");
		assertThat(foundUser).isNull();
	}

	@Test
	public void saveNewUser_WithRepeatEmail_ThrowsException() throws Exception {
		final User firstUser = new User("anyName", "repeatEmail");
		final User secondUser = new User("differentName", "repeatEmail");

		userRepository.saveNewUser(firstUser);

		try {
			userRepository.saveNewUser(secondUser);
			fail("expecting exception");

		} catch (final NonUniqueEmailException e) {
			assertThat(e.getMessage()).isEqualTo("Attempting to save a new user with an email that already exists: repeatEmail");
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void throwsRawException_WithOtherException_NotDueToUniqueEmailViolation() throws Exception {
		final MongoClient client = mock(MongoClient.class);
		final MongoDatabase db = mock(MongoDatabase.class);
		final MongoCollection<Document> collection = mock(MongoCollection.class);
		when(client.getDatabase(DatabaseSetup.DB_NAME)).thenReturn(db);
		when(db.getCollection(UserCollectionInitializer.USER_COLLECTION)).thenReturn(collection);

		final UserRepository repository = new MongoUserRepository(client, new UserTranslator());

		final MongoWriteException toBeThrown = new MongoWriteException(new WriteError(11000, "duplicate key error _id", new BsonDocument()), new ServerAddress());
		Mockito.doThrow(toBeThrown).when(collection).insertOne(Mockito.any(Document.class));

		try {
			repository.saveNewUser(new User("name", "email"));
			fail("expecting exception");

		} catch (final Exception e) {
			assertThat(e).isEqualTo(toBeThrown);
		}
	}
}
