package com.poorknight.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.net.URL;

class ImageS3Repository {

	private static final String BUCKET_NAME = "myrecipeconnection.com.images";

	public String saveNewImage(final InputStream imageInputStream, final String imageId) {
		final URL url = uploadImageToS3(imageInputStream, imageId);
		return makeUrlHttp(url);
	}

	private URL uploadImageToS3(final @FormDataParam("file") InputStream imageInputStream, final String imageId) {
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		s3.putObject(buildS3PutImageRequest(imageInputStream, imageId));
		return s3.getUrl(BUCKET_NAME, imageId);
	}

	private PutObjectRequest buildS3PutImageRequest(final InputStream imageInputStream, final String imageId) {
		final PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, imageId, imageInputStream, new ObjectMetadata());
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		return request;
	}

	private String makeUrlHttp(final URL url) {
		final String original = url.toExternalForm();
		return StringUtils.replace(original, "https:", "http:");
	}
}
