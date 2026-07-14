package com.masterbank.masterbank.authUsers.service.impl;

import com.masterbank.masterbank.authUsers.dtos.UpdatePasswordRequest;
import com.masterbank.masterbank.authUsers.dtos.UserDTO;
import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.authUsers.repository.UserRepository;
import com.masterbank.masterbank.authUsers.service.UserService;
import com.masterbank.masterbank.exceptions.BadRequestException;
import com.masterbank.masterbank.exceptions.NotFoundException;
import com.masterbank.masterbank.notifications.dtos.NotificationDTO;
import com.masterbank.masterbank.notifications.service.NotificationService;
import com.masterbank.masterbank.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    private final String uploadDir = "uploads/profile-pictures/";

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null) {
            throw new NotFoundException("User not Authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Override
    public Response<UserDTO> getMyProfile() {
        User user = getCurrentLoggedInUser();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User profile retrieved successfully")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<Page<UserDTO>> getAllUsers(int page, int size) {

        Page<User> users  = userRepository.findAll(PageRequest.of(page, size));
        Page<UserDTO> userDTOs = users.map(user -> modelMapper.map(user, UserDTO.class));

        return Response.<Page<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users retrieved successfully")
                .data(userDTOs)
                .build();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        User user = getCurrentLoggedInUser();

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();

        if(oldPassword == null || newPassword == null) {
            throw new BadRequestException("Old Password or New Password is null");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedDate(LocalDateTime.now());

        userRepository.save(user);

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Password Changed Successfully")
                .templateName("password-change")
                .templateVariables(templateVariables)
                .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password updated successfully")
                .build();
    }

    @Override
    public Response<?> uploadProfilePicture(MultipartFile file) {
        User user = getCurrentLoggedInUser();

        try{
             Path uploadPath = Paths.get(uploadDir);
             if(!Files.exists(uploadPath)) {
                 Files.createDirectories(uploadPath);
             }
             if(user.getProfilePictureUrl() != null) {
                 Path oldFilePath = Paths.get(user.getProfilePictureUrl());
                 if(!Files.exists(oldFilePath)) {
                     Files.delete(oldFilePath);
                 }
             }

             String originalFileName = file.getOriginalFilename();
             String fileExtension = "";
             if(originalFileName != null &&  originalFileName.contains(".")) {
                 fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
             }

             String newFileName = UUID.randomUUID() + fileExtension;
             Path filePath = uploadPath.resolve(newFileName);

             Files.copy(file.getInputStream(), filePath);

             String fileUrl = uploadDir + newFileName;

             user.setProfilePictureUrl(fileUrl);
             userRepository.save(user);

             return Response.builder()
                     .statusCode(HttpStatus.OK.value())
                     .message("Profile picture uploaded successfully")
                     .data(fileUrl)
                     .build();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
