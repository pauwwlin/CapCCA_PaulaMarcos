package com.capgemini.test.code.service;

import com.capgemini.test.code.model.dto.UserDetailResponse;
import com.capgemini.test.code.model.dto.UserRequest;
import com.capgemini.test.code.model.dto.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserDetailResponse getUserById(Long id);
}
