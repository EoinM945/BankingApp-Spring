package com.masterbank.masterbank.authUsers.service;

import com.masterbank.masterbank.authUsers.dtos.UpdatePasswordRequest;
import com.masterbank.masterbank.authUsers.dtos.UserDTO;
import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.response.Response;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User getCurrentLoggedInUser();

    Response<UserDTO> getMyProfile();

    Response<Page<UserDTO>> getAllUsers(int page, int size);

    Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest);

    Response<?> uploadProfilePicture(MultipartFile file);

    Response<?> uploadProfilePictureToS3(MultipartFile file);
}
