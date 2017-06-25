package com.poorknight.image;

import com.mongodb.MongoClient;
import com.poorknight.mongo.setup.MongoSetupHelper;
import org.junit.*;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.assertj.core.api.Assertions.assertThat;

public class ImageDBRepositoryTest {
	private static MongoClient mongo;
	private ImageDBRepository repository;

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
		repository = new ImageDBRepository(mongo);
	}

	@After
	public void tearDown() {
		MongoSetupHelper.deleteAllImages();
	}

	@Test
	public void simpleSaveAndFindWorks() throws Exception {
		final String imageId = random(20);
		final String imageUrl = random(50);
		final String owningUserId = random(15);

		final Image imageToSave = new Image(imageId, imageUrl, owningUserId);
		final Image savedImage = repository.saveNewImage(imageToSave);
		assertThat(savedImage.getImageId()).isEqualTo(imageId);
		assertThat(savedImage.getImageUrl()).isEqualTo(imageUrl);
		assertThat(savedImage.getOwningUserId()).isEqualTo(owningUserId);

		final Image foundImage = repository.findImage(imageId);
		assertThat(foundImage.getImageId()).isEqualTo(imageId);
		assertThat(foundImage.getImageUrl()).isEqualTo(imageUrl);
		assertThat(foundImage.getOwningUserId()).isEqualTo(owningUserId);
	}

	@Test
	public void findImageReturnsNullIfNotFound() throws Exception {
		final Image response = repository.findImage("not found");
		assertThat(response).isNull();
	}

	@Test
	public void deleteWorks() throws Exception {
		final String imageId = random(20);
		final String imageUrl = random(50);
		final String owningUserId = random(15);
		final Image imageToSave = new Image(imageId, imageUrl, owningUserId);

		repository.saveNewImage(imageToSave);
		final Image foundImage = repository.findImage(imageId);
		assertThat(foundImage).isNotNull();

		repository.deleteImage(imageId);

		final Image deletedImage = repository.findImage(imageId);
		assertThat(deletedImage).isNull();
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfImageIsNull() throws Exception {
		final Image imageToSave = null;
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfIdIsNull() throws Exception {
		final Image imageToSave = new Image(null, random(5), random(5));
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfUrlIsNull() throws Exception {
		final Image imageToSave = new Image(random(5), null, random(5));
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfOwningUserIdIsNull() throws Exception {
		final Image imageToSave = new Image(random(5), random(5), null);
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfIdIsEmpty() throws Exception {
		final Image imageToSave = new Image("", random(5), random(5));
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfUrlIsEmpty() throws Exception {
		final Image imageToSave = new Image(random(5), "", random(5));
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfOwningUserIdIsEmpty() throws Exception {
		final Image imageToSave = new Image(random(5), random(5), "");
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfIdIsWhitespace() throws Exception {
		final Image imageToSave = new Image(" 	", random(5), random(5));
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfUrlIsWhitespace() throws Exception {
		final Image imageToSave = new Image(random(5), " 	", random(5));
		repository.saveNewImage(imageToSave);
	}

	@Test(expected = RuntimeException.class)
	public void saveNewImageThrowsExceptionIfOwningUserIdIsWhitespace() throws Exception {
		final Image imageToSave = new Image(random(5), random(5), "		 ");
		repository.saveNewImage(imageToSave);
	}
}