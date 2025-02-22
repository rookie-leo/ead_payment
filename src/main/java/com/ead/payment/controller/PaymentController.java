package com.ead.payment.controller;

import com.ead.payment.configs.security.AuthenticationCurrentUserService;
import com.ead.payment.configs.security.UserDetailsImpl;
import com.ead.payment.dtos.PaymentRequestRecordDto;
import com.ead.payment.enums.PaymentControl;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.services.PaymentService;
import com.ead.payment.services.UserService;
import com.ead.payment.specifications.SpecificationTemplate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
public class PaymentController {

    final UserService userService;
    final PaymentService paymentService;
    final AuthenticationCurrentUserService authenticationCurrentUserService;

    public PaymentController(UserService userService, PaymentService paymentService, AuthenticationCurrentUserService authenticationCurrentUserService) {
        this.userService = userService;
        this.paymentService = paymentService;
        this.authenticationCurrentUserService = authenticationCurrentUserService;
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

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/users/{userId}/payments")
    public ResponseEntity<Page<PaymentModel>> getAllPayments(
            @PathVariable(value = "userId") UUID userId,
            SpecificationTemplate.PaymentSpec spec,
            Pageable pageable
    ) {
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrentUser();

        if (userDetails.getUserId().equals(userId) || userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(paymentService.findAllByUser(SpecificationTemplate.paymentUserId(userId).and(spec), pageable));
        } else {
            throw new AccessDeniedException("Forbidden");
        }

    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/users/{userId}/payments/{paymentId}")
    public ResponseEntity<Object> getOnePayment(
            @PathVariable(value = "userId") UUID userId,
            @PathVariable(value = "paymentId") UUID paymentId,
            SpecificationTemplate.PaymentSpec spec,
            Pageable pageable
    ) {
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrentUser();

        if (userDetails.getUserId().equals(userId) || userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(paymentService.findPaymentByUser(userId, paymentId).get());
        } else {
            throw new AccessDeniedException("Forbidden");
        }

    }
}
