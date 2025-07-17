package com.jiggycode.service;

import com.jiggycode.request.PasswordUpdateRequest;
import com.jiggycode.response.UserResponse;

public interface UserService {
    UserResponse getUserInfo();
    void deleteUser();
    void updatePassword(PasswordUpdateRequest passwordUpdateRequest);
}
