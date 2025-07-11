package com.poorknight.user;

public interface UserRepository {
    User saveNewUser(User userToSave);

    User findUserById(User.UserId id);

    User findUserByEmail(String email);
}
