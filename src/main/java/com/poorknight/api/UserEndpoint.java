package com.poorknight.api;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.user.ApiUser;
import com.poorknight.user.User;
import com.poorknight.user.UserRepository;
import com.poorknight.user.UserTranslator;
import com.poorknight.user.save.NonUniqueEmailException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserEndpoint {

	private final UserRepository userRepository;
	private final UserTranslator userTranslator;

	public UserEndpoint(final UserRepository userRepository, final UserTranslator userTranslator) {
		this.userRepository = userRepository;
		this.userTranslator = userTranslator;
	}

	@GET
	@Timed(name = "getUser")
	@Path("/{id}")
	public ApiUser getUser(@PathParam("id") final String userId) {
		final User user = userRepository.findUserById(userTranslator.userIdFor(userId));
		throwNotFoundExceptionIfUserIsNull(user, userId);
		return userTranslator.toApi(user);
	}

	@GET
	@Timed(name = "getUserByEmail")
	@Path("/")
	public ApiUser getUserByEmail(@QueryParam("email") final String userEmail) {
		final User user = userRepository.findUserByEmail(userEmail);
		throwNotFoundExceptionIfUserIsNull(user, userEmail);
		return userTranslator.toApi(user);
	}

	@POST
	@Timed(name = "postUser")
	@Path("/")
	public ApiUser postUser(final ApiUser user) {
		try {
			return postUserThrowingException(user);
		} catch (final NonUniqueEmailException e) {
			throw new ForbiddenException(e.getMessage(), e);
		}
	}

	private ApiUser postUserThrowingException(final ApiUser user) {
		final User userToSave = userTranslator.toDomain(user);
		final User savedUser = userRepository.saveNewUser(userToSave);
		return userTranslator.toApi(savedUser);
	}

	private void throwNotFoundExceptionIfUserIsNull(final User user, final String userIdentifier) {
		if(user == null) {
			throw new NotFoundException("User not found: " + userIdentifier);
		}
	}
}
