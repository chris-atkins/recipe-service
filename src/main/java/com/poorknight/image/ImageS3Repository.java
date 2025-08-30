package com.poorknight.image;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.net.URL;

public class ImageS3Repository {

	private static final String BUCKET_NAME = "myrecipeconnection.images";

	protected void deleteImage(final String imageId) {
		final AmazonS3 s3 = buildS3Client();
		s3.deleteObject(BUCKET_NAME, imageId);
	}

	private AmazonS3 buildS3Client() {
		return AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(
						new AwsClientBuilder.EndpointConfiguration(
								"https://nyc3.digitaloceanspaces.com",
								"us-east-1"))
				.build();
	}

	protected String saveNewImage(final InputStream imageInputStream, final String imageId) {
		final URL url = uploadImageToS3(imageInputStream, imageId);
		System.out.println("Image URL from DO: " + url.toString());
		return transformToCDNSubdomainUrl(url);
	}

	private URL uploadImageToS3(final @FormDataParam("file") InputStream imageInputStream, final String imageId) {
		final AmazonS3 s3 = buildS3Client();
		s3.putObject(buildS3PutImageRequest(imageInputStream, imageId));
		return s3.getUrl(BUCKET_NAME, imageId);
	}

	private PutObjectRequest buildS3PutImageRequest(final InputStream imageInputStream, final String imageId) {
		final PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, imageId, imageInputStream, new ObjectMetadata());
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		return request;
	}

	private String transformToCDNSubdomainUrl(final URL url) {
		final String original = url.toExternalForm();
		String[] splitsBySlash = original.split("/");
		return "https://images.myrecipeconnection.com/" + splitsBySlash[splitsBySlash.length - 1];
	}
}
