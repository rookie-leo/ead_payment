package com.ead.payment.dtos;

import com.ead.payment.models.UserModel;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

public record UserEventRecordDto(
        UUID userId,
        String userName,
        String email,
        String fullName,
        String userStatus,
        String userType,
        String phoneNumber,
        String imageUrl,
        String actionType
) {
    public UserModel convertToUserModel() {
        var userModel = new UserModel();
        BeanUtils.copyProperties(this, userModel);
        return userModel;
    }

}
