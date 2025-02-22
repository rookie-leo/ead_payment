package com.ead.payment.services;

import com.ead.payment.dtos.PaymentCommandRecordDto;
import com.ead.payment.dtos.PaymentRequestRecordDto;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public interface PaymentService {
    PaymentModel requestPayment(PaymentRequestRecordDto paymentRequestRecordDto, UserModel userModel);

    Optional<PaymentModel> findLastPaymentByUser(UserModel userModel);

    Page<PaymentModel> findAllByUser(Specification<PaymentModel> spec, Pageable pageable);

    Optional<PaymentModel> findPaymentByUser(UUID userId, UUID paymentId);

    void makePayment(PaymentCommandRecordDto paymentCommandRecordDto);
}
