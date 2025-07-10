package com.poorknight.image;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.poorknight.application.init.DatabaseSetup;
import org.bson.Document;
import org.bson.conversions.Bson;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ImageDBRepository {


	private final MongoClient mongoClient;

	public ImageDBRepository(final MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	Image saveNewImage(Image image) {
		throwExceptionIfImageIsNotValid(image);
		final MongoCollection<Document> collection = getImageCollection();
		final Document document = toDocumentForSave(image);
		collection.insertOne(document);
		return toImage(document);
	}

	private void throwExceptionIfImageIsNotValid(final Image image) {
		if(image == null || isBlank(image.getImageId()) || isBlank(image.getImageUrl()) || isBlank(image.getOwningUserId())) {
			throw new RuntimeException("In order to save an Image, it must have a non-null and non-empty value for every field.  This was the image received: " + image);
		}
	}

	Image findImage(final String imageId) {
		final MongoCollection<Document> collection = getImageCollection();
		final Bson idFilter = createFilterOnId(imageId);
		final Document document = collection.find(idFilter).first();
		return toImage(document);
	}

	void deleteImage(final String imageId) {
		final MongoCollection<Document> collection = getImageCollection();
		final Bson idFilter = createFilterOnId(imageId);
		collection.deleteOne(idFilter);
	}

	private MongoCollection<Document> getImageCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(DatabaseSetup.DB_NAME);
		return database.getCollection(ImageCollectionInitializer.IMAGE_COLLECTION);
	}

	private Document toDocumentForSave(final Image image) {
		return new Document("imageId", image.getImageId()) //
				.append("url", image.getImageUrl()) //
				.append("owningUserId", image.getOwningUserId());
	}

	private Image toImage(final Document document) {
		if (document == null) {
			return null;
		}

		final String imageId = document.getString("imageId");
		final String url = document.getString("url");
		final String owningUserId = document.getString("owningUserId");

		return new Image(imageId, url, owningUserId);
	}

	private Bson createFilterOnId(final String imageId) {
		return Filters.eq("imageId", imageId);
	}
}

