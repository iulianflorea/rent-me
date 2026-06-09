package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.AdminUserFilterRequest;
import com.singularity.rentit.dto.response.UserProfileResponse;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getUsers(AdminUserFilterRequest filter, Pageable pageable) {
        return userRepository.findAllWithFilters(
                filter.email(), filter.role(), filter.kycStatus(), filter.active(), pageable
        ).map(userService::toResponse);
    }

    @Transactional
    public UserProfileResponse suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Cannot suspend admin accounts", HttpStatus.BAD_REQUEST);
        }

        user.setActive(false);
        userRepository.save(user);
        log.info("User {} suspended", userId);
        return userService.toResponse(user);
    }

    @Transactional
    public UserProfileResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(true);
        userRepository.save(user);
        log.info("User {} activated", userId);
        return userService.toResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Cannot delete admin accounts", HttpStatus.BAD_REQUEST);
        }

        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deleted (deactivated)", userId);
    }
}
