package com.poorknight.recipebook;

import com.mongodb.MongoClient;
import com.poorknight.mongo.setup.MongoSetupHelper;
import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;
import org.bson.types.ObjectId;
import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class MongoRecipeBookRepositoryTest {

	private static MongoClient mongo;
	private RecipeBookRepository recipeBookRepository;

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
		recipeBookRepository = new MongoRecipeBookRepository(mongo);
	}

	@After
	public void tearDown() {
		MongoSetupHelper.deleteAllRecipeBooks();
	}

	@Test
	public void addRecipeIdToRecipeBook_WithNoExistingRateBook_CreatesANewRateBook() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final RecipeId recipeId = new RecipeId(randomObjectId());

		assertThat(recipeBookRepository.getRecipeBook(userId)).isNull();

		final RecipeId result = recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId);
		assertThat(result).isEqualTo(recipeId);

		final RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
		assertThat(recipeBook.getUserId()).isEqualTo(userId);
		assertThat(recipeBook.getRecipeIds().size()).isEqualTo(1);
		assertThat(recipeBook.getRecipeIds().get(0)).isEqualTo(recipeId);
	}

	@Test
	public void addRecipeIdToRecipeBook_WithAnExistingRateBook_AddsTheNewRecipeIdToTheRateBook() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final RecipeId firstRecipeId = new RecipeId(randomObjectId());
		final RecipeId secondRecipeId = new RecipeId(randomObjectId());

		recipeBookRepository.addRecipeIdToRecipeBook(userId, firstRecipeId);
		recipeBookRepository.addRecipeIdToRecipeBook(userId, secondRecipeId);

		final RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
		assertThat(recipeBook.getUserId()).isEqualTo(userId);
		assertThat(recipeBook.getRecipeIds().size()).isEqualTo(2);
		assertThat(recipeBook.getRecipeIds().get(0)).isEqualTo(firstRecipeId);
		assertThat(recipeBook.getRecipeIds().get(1)).isEqualTo(secondRecipeId);
	}

	@Test
	public void addRecipeIdToRecipeBook_DoesNotAddRepeatIds() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final String repeatId = randomObjectId();
		final RecipeId firstRecipeId = new RecipeId(repeatId);
		final RecipeId secondRecipeId = new RecipeId(repeatId);

		recipeBookRepository.addRecipeIdToRecipeBook(userId, firstRecipeId);
		recipeBookRepository.addRecipeIdToRecipeBook(userId, secondRecipeId);

		final RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
		assertThat(recipeBook.getUserId()).isEqualTo(userId);
		assertThat(recipeBook.getRecipeIds().size()).isEqualTo(1);
		assertThat(recipeBook.getRecipeIds().get(0)).isEqualTo(new RecipeId(repeatId));
	}

	@Test
	public void addRecipeIdToRecipeBook_WithInvalidRecipeId_ThrowsInvalidIdException() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final RecipeId recipeId = new RecipeId("invalid");

		try {
			recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId);
			fail("expecting exception");
		} catch (final InvalidIdException e) {
			assertThat(e.getMessage()).isEqualTo("The passed ID is not valid: invalid");
		}
	}

	@Test
	public void addRecipeIdToRecipeBook_WithInvalidUserId_ThrowsInvalidIdException() throws Exception {
		final UserId userId = new UserId("invalid");
		final RecipeId recipeId = new RecipeId(randomObjectId());

		try {
			recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId);
			fail("expecting exception");
		} catch (final InvalidIdException e) {
			assertThat(e.getMessage()).isEqualTo("The passed ID is not valid: invalid");
		}
	}

	@Test
	public void getRecipeBook_FindsTheCorrectUsersBook_WhenMoreThanOneExist() throws Exception {
		final UserId firstUserId = new UserId(randomObjectId());
		final UserId secondUserId = new UserId(randomObjectId());
		final RecipeId firstRecipeId = new RecipeId(randomObjectId());
		final RecipeId secondRecipeId = new RecipeId(randomObjectId());

		recipeBookRepository.addRecipeIdToRecipeBook(firstUserId, firstRecipeId);
		recipeBookRepository.addRecipeIdToRecipeBook(secondUserId, secondRecipeId);

		final RecipeBook recipeBook = recipeBookRepository.getRecipeBook(secondUserId);
		assertThat(recipeBook.getUserId()).isEqualTo(secondUserId);
		assertThat(recipeBook.getRecipeIds().size()).isEqualTo(1);
		assertThat(recipeBook.getRecipeIds().get(0)).isEqualTo(secondRecipeId);
	}

	@Test
	public void getRecipeBook_WithInvalidUserId_FindsTheCorrectUsersBook__ThrowsInvalidIdException() throws Exception {
		final UserId userId = new UserId("invalid");

		try {
			recipeBookRepository.getRecipeBook(userId);
			fail("expecting exception");
		} catch (final InvalidIdException e) {
			assertThat(e.getMessage()).isEqualTo("The passed ID is not valid: invalid");
		}
	}

	@Test
	public void deleteRecipeFromRecipeBook_WithASingleRecipe_RemovesRecipe() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final RecipeId recipeId = new RecipeId(randomObjectId());

		recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId);
		assertThat(recipeBookRepository.getRecipeBook(userId).getRecipeIds().size()).isEqualTo(1);

		recipeBookRepository.deleteRecipeFromRecipeBook(userId, recipeId);
		assertThat(recipeBookRepository.getRecipeBook(userId).getRecipeIds().size()).isEqualTo(0);
	}

	@Test
	public void deleteRecipeFromRecipeBook_WithMultipleRecipes_RemovesRecipe() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final String firstRecipeIdString = randomObjectId();
		final String recipeIdToBeDeletedString = randomObjectId();
		final RecipeId firstRecipeId = new RecipeId(firstRecipeIdString);
		final RecipeId recipeIdToBeDeleted = new RecipeId(recipeIdToBeDeletedString);

		recipeBookRepository.addRecipeIdToRecipeBook(userId, firstRecipeId);
		recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeIdToBeDeleted);

		final RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
		assertThat(recipeBook.getRecipeIds().size()).isEqualTo(2);

		recipeBookRepository.deleteRecipeFromRecipeBook(userId, recipeIdToBeDeleted);
		assertThat(recipeBookRepository.getRecipeBook(userId).getRecipeIds().size()).isEqualTo(1);
		assertThat(recipeBookRepository.getRecipeBook(userId).getRecipeIds().get(0)).isEqualTo(firstRecipeId);
	}

	@Test
	public void deleteRecipeFromRecipeBook_WithRecipeIdThatIsNotInTheBook_ThrowsException() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final String firstRecipeIdString = randomObjectId();
		final RecipeId firstRecipeId = new RecipeId(firstRecipeIdString);

		recipeBookRepository.addRecipeIdToRecipeBook(userId, firstRecipeId);
		final RecipeId unknownId = new RecipeId(randomObjectId());

		try {
			recipeBookRepository.deleteRecipeFromRecipeBook(userId, unknownId);
			fail("expecting exception");

		} catch (final RecipeNotInBookException e) {
			assertThat(e.getMessage()).contains("Recipe does not exist in recipe book: " + unknownId.getValue());
		}
	}

	@Test
	public void deleteRecipeFromRecipeBook_WithNoExistingRecipeBook_ThrowsException() throws Exception {
		final UserId userId = new UserId(randomObjectId());
		final RecipeId recipeId = new RecipeId(randomObjectId());

		try {
			recipeBookRepository.deleteRecipeFromRecipeBook(userId, recipeId);
			fail("expecting exception");

		} catch (final RecipeBookNotFoundException e) {
			assertThat(e.getMessage()).contains("No recipe book found for user with id: " + userId.getValue());
		}
	}

	private String randomObjectId() {
		return new ObjectId().toHexString();
	}
}