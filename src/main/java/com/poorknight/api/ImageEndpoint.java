package com.poorknight.api;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.image.ApiImage;
import com.poorknight.image.Image;
import com.poorknight.image.ImageRepository;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Path("/image")
public class ImageEndpoint {

	private ImageRepository imageRepository;

	public ImageEndpoint(final ImageRepository imageRepository) {
		this.imageRepository = imageRepository;
	}

	@POST
	@Timed(name = "postImage")
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public ApiImage postImage(@FormDataParam("file") final InputStream imageInputStream, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final Image image = imageRepository.saveNewImage(imageInputStream, requestingUserIdString);
		return toApi(image);
	}

	private ApiImage toApi(final Image image) {
		return new ApiImage(image.getImageId(), image.getImageUrl());
	}

	@DELETE
	@Timed(name = "deleteImage")
	@Path("/{imageId}")
	public void deleteImage(@PathParam("imageId") final String imageId, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		imageRepository.deleteImage(imageId, requestingUserIdString);
	}
}
