package com.poorknight.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.api.entities.ApiUser;
import com.poorknight.domain.User;
import com.poorknight.persistence.UserRepository;
import com.poorknight.transform.api.domain.UserTranslator;

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
		return userTranslator.toApi(user);
	}

	@GET
	@Timed(name = "getUserByEmail")
	@Path("/")
	public ApiUser getUserByEmail(@QueryParam("email") final String userEmail) {
		final User user = userRepository.findUserByEmail(userEmail);
		return userTranslator.toApi(user);
	}

	@POST
	@Timed(name = "postUser")
	@Path("/")
	public ApiUser postUser(final ApiUser user) {
		final User userToSave = userTranslator.toDomain(user);
		final User savedUser = userRepository.saveUser(userToSave);
		return userTranslator.toApi(savedUser);
	}
}
