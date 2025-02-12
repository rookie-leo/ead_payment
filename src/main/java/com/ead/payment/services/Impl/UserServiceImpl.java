package com.ead.payment.services.Impl;

import com.ead.payment.exceptions.NotFoundException;
import com.ead.payment.models.UserModel;
import com.ead.payment.repositories.UserRepository;
import com.ead.payment.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserModel save(UserModel userModel) {
        return userRepository.save(userModel);
    }

    @Transactional
    @Override
    public void delete(UUID userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public Optional<UserModel> findById(UUID userId) {
        Optional<UserModel> userModelOptional = userRepository.findById(userId);

        if (userModelOptional.isEmpty()) throw new NotFoundException("User not found!");

        return userModelOptional;
    }
}
