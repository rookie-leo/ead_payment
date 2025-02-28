package com.ead.payment.dtos;

import com.ead.payment.models.UserModel;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

public record UserEventRecordDto(
        UUID userId,
        String username, //TODO
        String email,
        String fullName,
        String userStatus,
        String userType,
        String phoneNumber,
        String imageUrl,
        String actionType
) {
    public UserModel convertToUserModel(UserModel userModel) {
        BeanUtils.copyProperties(this, userModel);
        return userModel;
    }

}
