package com.poorknight.recipebook;

import com.mongodb.MongoClient;
import com.poorknight.mongo.setup.MongoSetupHelper;
import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;
import org.bson.types.ObjectId;
import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipeBookRepositoryTest {

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
		recipeBookRepository = new RecipeBookRepository(mongo);
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

		RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
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

		RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
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

		RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
		assertThat(recipeBook.getUserId()).isEqualTo(userId);
		assertThat(recipeBook.getRecipeIds().size()).isEqualTo(1);
		assertThat(recipeBook.getRecipeIds().get(0)).isEqualTo(new RecipeId(repeatId));
	}

	@Test
	public void getRecipeBook_FindsTheCorrectUsersBook_WhenMoreThanOneExist() throws Exception {
		final UserId firstUserId = new UserId(randomObjectId());
		final UserId secondtUserId = new UserId(randomObjectId());
		final RecipeId firstRecipeId = new RecipeId(randomObjectId());
		final RecipeId secondRecipeId = new RecipeId(randomObjectId());

		recipeBookRepository.addRecipeIdToRecipeBook(firstUserId, firstRecipeId);
		recipeBookRepository.addRecipeIdToRecipeBook(secondtUserId, secondRecipeId);

		RecipeBook recipeBook = recipeBookRepository.getRecipeBook(secondtUserId);
		assertThat(recipeBook.getUserId()).isEqualTo(secondtUserId);
		assertThat(recipeBook.getRecipeIds().size()).isEqualTo(1);
		assertThat(recipeBook.getRecipeIds().get(0)).isEqualTo(secondRecipeId);
	}

	private String randomObjectId() {
		return new ObjectId().toHexString();
	}
}