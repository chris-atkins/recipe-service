package com.poorknight.image;

import com.poorknight.mongo.setup.PostgresTestHelper;
import com.poorknight.recipe.PostgresConnectionInfo;
import org.junit.jupiter.api.*;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PostgresImageDBRepositoryTest {

	private ImageDBRepository repository;

	@BeforeAll
	public static void setupMongo() throws Exception {
		PostgresTestHelper.startPostgresAndMigrateTables();
	}

	@AfterAll
	public static void teardown() {
		PostgresTestHelper.stopPostgres();
	}

	@BeforeEach
	public void setup() {
		PostgresConnectionInfo connectionInfo = PostgresTestHelper.buildCoonnectionInfo();
		repository = new PostgresImageDBRepository(connectionInfo);
	}

	@AfterEach
	public void tearDown() {
		PostgresTestHelper.deleteAllImages();
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

	@Test
	public void saveNewImageThrowsExceptionIfImageIsNull() throws Exception {
		final Image imageToSave = null;
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfIdIsNull() throws Exception {
		final Image imageToSave = new Image(null, random(5), random(5));
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfUrlIsNull() throws Exception {
		final Image imageToSave = new Image(random(5), null, random(5));
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfOwningUserIdIsNull() throws Exception {
		final Image imageToSave = new Image(random(5), random(5), null);
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfIdIsEmpty() throws Exception {
		final Image imageToSave = new Image("", random(5), random(5));
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfUrlIsEmpty() throws Exception {
		final Image imageToSave = new Image(random(5), "", random(5));
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfOwningUserIdIsEmpty() throws Exception {
		final Image imageToSave = new Image(random(5), random(5), "");
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfIdIsWhitespace() throws Exception {
		final Image imageToSave = new Image(" 	", random(5), random(5));
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfUrlIsWhitespace() throws Exception {
		final Image imageToSave = new Image(random(5), " 	", random(5));
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}

	@Test
	public void saveNewImageThrowsExceptionIfOwningUserIdIsWhitespace() throws Exception {
		final Image imageToSave = new Image(random(5), random(5), "		 ");
		assertThrows(RuntimeException.class, () -> repository.saveNewImage(imageToSave));
	}
}