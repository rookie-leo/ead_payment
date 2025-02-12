package com.ead.payment.controller;

import com.ead.payment.dtos.PaymentRequestRecordDto;
import com.ead.payment.enums.PaymentControl;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.services.PaymentService;
import com.ead.payment.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
public class PaymentController {

    final UserService userService;
    final PaymentService paymentService;

    public PaymentController(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("/users/{userId}/payments")
    public ResponseEntity<Object> requestPayment(@PathVariable(value = "userId") UUID userId,
                                                 @RequestBody @Valid PaymentRequestRecordDto paymentRequestRecordDto
    ) {
        var userModel = userService.findById(userId).get();

        Optional<PaymentModel> paymentModelOptional = paymentService.findLastPaymentByUser(userModel);
        if (paymentModelOptional.isPresent()) {
            if (paymentModelOptional.get().getPaymentControl().equals(PaymentControl.REQUESTED))
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already requested.");
            if (paymentModelOptional.get().getPaymentControl().equals(PaymentControl.EFFECTED)
                    && paymentModelOptional.get().getPaymentExpirationDate().isAfter(LocalDateTime.now(ZoneId.of("UTC"))))
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already made.");
        }

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(paymentService.requestPayment(paymentRequestRecordDto, userModel));
    }
}
