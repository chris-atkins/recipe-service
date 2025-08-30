package com.poorknight.image;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageS3RepositoryTest {

	private static final String HTTPS_URL = "https://helloThere/abcd";
	private static final String TRANSFORMED_URL = "https://images.myrecipeconnection.com/abcd";

	private String bucketName;
	private String imageId;
	private AmazonS3 s3;
	private InputStream imageInputStream;

	@InjectMocks
	private ImageS3Repository repository;

	@Mock
	AmazonS3ClientBuilder builder;

	@Captor
	private ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor;

	@BeforeEach
	public void setUp() throws Exception {
		final UUID uuid = UUID.randomUUID();
		bucketName = "myrecipeconnection.images";
		imageId = uuid.toString();
		s3 = Mockito.mock(AmazonS3.class);
		imageInputStream = new ByteArrayInputStream("image".getBytes(Charset.defaultCharset()));
	}

	@Test
	public void sameImageStoresItToAWSWWhileReplacingURLWithHttp() throws Exception {
		try (MockedStatic<AmazonS3ClientBuilder> s3Builder = Mockito.mockStatic(AmazonS3ClientBuilder.class)) {
			s3Builder.when(AmazonS3ClientBuilder::standard).thenReturn(builder);
			when(builder.withEndpointConfiguration(any())).thenReturn(builder);
			when(builder.build()).thenReturn(s3);
			Mockito.when(s3.getUrl(bucketName, imageId)).thenReturn(new URL(HTTPS_URL));

			final String imageUrl = repository.saveNewImage(imageInputStream, imageId);

			verify(s3).putObject(putObjectRequestArgumentCaptor.capture());
			final PutObjectRequest s3RequestObject = putObjectRequestArgumentCaptor.getValue();
			assertThat(s3RequestObject.getBucketName()).isEqualTo(bucketName);
			assertThat(s3RequestObject.getKey()).isEqualTo(imageId);
			assertThat(s3RequestObject.getInputStream()).isEqualTo(imageInputStream);
			assertThat(s3RequestObject.getCannedAcl()).isEqualTo(CannedAccessControlList.PublicRead);

			assertThat(imageUrl).isEqualTo(TRANSFORMED_URL);
		}
	}

	@Test
	public void deleteRecipeCoordinatesCorrectly() throws Exception {
		try (MockedStatic<AmazonS3ClientBuilder> s3Builder = Mockito.mockStatic(AmazonS3ClientBuilder.class)) {
			s3Builder.when(AmazonS3ClientBuilder::standard).thenReturn(builder);
			when(builder.withEndpointConfiguration(any())).thenReturn(builder);
			when(builder.build()).thenReturn(s3);

			final String imageId = RandomStringUtils.random(20);
			repository.deleteImage(imageId);
			verify(s3).deleteObject(bucketName, imageId);
		}
	}
}