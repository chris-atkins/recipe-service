package com.poorknight.image;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ImageDeleteNotAllowedException extends WebApplicationException {

	public ImageDeleteNotAllowedException(String message) {
		super(message, Response.Status.FORBIDDEN);
	}
}
