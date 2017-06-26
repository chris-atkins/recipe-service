package com.poorknight.image;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ImageOperationNotAllowedException extends WebApplicationException {

	public ImageOperationNotAllowedException(String message, Reason reason) {
		super(message, reason.equals(Reason.NO_USER) ? Response.Status.UNAUTHORIZED : Response.Status.FORBIDDEN);
	}

	public enum Reason {
		NO_USER, NOT_IMAGE_OWNER
	}
}
