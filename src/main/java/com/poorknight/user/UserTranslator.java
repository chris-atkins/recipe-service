package com.poorknight.user;

public class UserTranslator {

	public UserId userIdFor(final String idValue) {
		return idValue == null ? null : new UserId(idValue);
	}

	public ApiUser toApi(final User user) {
		return new ApiUser(user.getId().getValue(), user.getName(), user.getEmail());
	}

	public User toDomain(final ApiUser apiUser) {
		return new User(userIdFor(apiUser.getUserId()), apiUser.getUserName(), apiUser.getUserEmail());
	}
}
