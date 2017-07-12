package com.poorknight.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonS3ClientBuilder.class, ImageS3Repository.class})
public class ImageS3RepositoryTest {

	private static final String HTTPS_URL = "https://helloThere";
	private static final String HTTP_ONLY_URL = "http://helloThere";

	private String bucketName;
	private String imageId;
	private AmazonS3 s3;
	private InputStream imageInputStream;

	@InjectMocks
	private ImageS3Repository repository;

	@Captor
	private ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor;

	@Before
	public void setUp() throws Exception {
		final UUID uuid = UUID.randomUUID();
		bucketName = "myrecipeconnection.com.images";
		imageId = uuid.toString();
		s3 = Mockito.mock(AmazonS3.class);
		imageInputStream = new ByteArrayInputStream("image".getBytes(Charset.defaultCharset()));

		PowerMockito.mockStatic(AmazonS3ClientBuilder.class);

		when(AmazonS3ClientBuilder.defaultClient()).thenReturn(s3);
		when(s3.getUrl(bucketName, imageId)).thenReturn(new URL(HTTPS_URL));
	}

	@Test
	public void sameImageStoresItToAWSWWhileReplacingURLWithHttp() throws Exception {
		final String imageUrl = repository.saveNewImage(imageInputStream, imageId);

		verify(s3).putObject(putObjectRequestArgumentCaptor.capture());
		final PutObjectRequest s3RequestObject = putObjectRequestArgumentCaptor.getValue();
		assertThat(s3RequestObject.getBucketName()).isEqualTo(bucketName);
		assertThat(s3RequestObject.getKey()).isEqualTo(imageId);
		assertThat(s3RequestObject.getInputStream()).isEqualTo(imageInputStream);
		assertThat(s3RequestObject.getCannedAcl()).isEqualTo(CannedAccessControlList.PublicRead);

		assertThat(imageUrl).isEqualTo(HTTP_ONLY_URL);
	}

	@Test
	public void deleteRecipeCoordinatesCorrectly() throws Exception {
		final String imageId = RandomStringUtils.random(20);
		repository.deleteImage(imageId);
		verify(s3).deleteObject(bucketName, imageId);
	}
}