package com.ead.payment.services;

import com.ead.payment.dtos.PaymentRequestRecordDto;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.models.UserModel;

import java.util.Optional;

public interface PaymentService {
    PaymentModel requestPayment(PaymentRequestRecordDto paymentRequestRecordDto, UserModel userModel);

    Optional<PaymentModel> findLastPaymentByUser(UserModel userModel);
}
