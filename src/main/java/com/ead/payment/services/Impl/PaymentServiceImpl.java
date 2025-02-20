package com.ead.payment.services.Impl;

import com.ead.payment.dtos.PaymentCommandRecordDto;
import com.ead.payment.dtos.PaymentRequestRecordDto;
import com.ead.payment.enums.PaymentControl;
import com.ead.payment.exceptions.NotFoundException;
import com.ead.payment.models.CreditCardModel;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.models.UserModel;
import com.ead.payment.publishers.PaymentCommandPublisher;
import com.ead.payment.repositories.CreditCardRepository;
import com.ead.payment.repositories.PaymentRepository;
import com.ead.payment.repositories.UserRepository;
import com.ead.payment.services.PaymentService;
import com.ead.payment.services.PaymentStripeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    Logger logger = LogManager.getLogger(PaymentServiceImpl.class);

    final PaymentRepository paymentRepository;
    final UserRepository userRepository;
    final CreditCardRepository creditCardRepository;
    final PaymentCommandPublisher paymentCommandPublisher;
    final PaymentStripeService paymentStripeService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, UserRepository userRepository, CreditCardRepository creditCardRepository, PaymentCommandPublisher paymentCommandPublisher, PaymentStripeService paymentStripeService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.creditCardRepository = creditCardRepository;
        this.paymentCommandPublisher = paymentCommandPublisher;
        this.paymentStripeService = paymentStripeService;
    }

    @Transactional
    @Override
    public PaymentModel requestPayment(PaymentRequestRecordDto paymentRequestRecordDto, UserModel userModel) {
        var creditCardModel = creditCardRepository
                .findByUser(userModel)
                .orElseGet(CreditCardModel::new);

        BeanUtils.copyProperties(paymentRequestRecordDto, creditCardModel);
        creditCardModel.setUser(userModel);
        creditCardRepository.save(creditCardModel);

        var paymentModel = new PaymentModel();
        paymentModel.setPaymentControl(PaymentControl.REQUESTED);
        paymentModel.setPaymentRequestDate(LocalDateTime.now(ZoneId.of("UTC")));
        paymentModel.setPaymentExpirationDate(LocalDateTime.now(ZoneId.of("UTC")).plusMonths(12));
        paymentModel.setLastDigitsCreditCard(paymentRequestRecordDto.creditCardNumber().substring(paymentRequestRecordDto.creditCardNumber().length() - 4));
        paymentModel.setValuePaid(paymentRequestRecordDto.valuePaid());
        paymentModel.setUser(userModel);
        paymentRepository.save(paymentModel);

        try {
            var paymentCommandRecordDto = new PaymentCommandRecordDto(userModel.getUserId(), paymentModel.getPaymentId(), creditCardModel.getCardId());
            paymentCommandPublisher.publisherPaymentCommand(paymentCommandRecordDto);
        } catch (Exception ex) {
            logger.error("Error sending payment command message with casue: {}", ex.getMessage());
        }

        return paymentModel;
    }

    @Override
    public Optional<PaymentModel> findLastPaymentByUser(UserModel userModel) {
        return paymentRepository.findTopByUserOrderByPaymentRequestDateDesc(userModel);
    }

    @Override
    public Page<PaymentModel> findAllByUser(Specification<PaymentModel> spec, Pageable pageable) {
        return paymentRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<PaymentModel> findPaymentByUser(UUID userId, UUID paymentId) {
        Optional<PaymentModel> paymentModelOptional = paymentRepository.findPaymentByUser(userId, paymentId);

        if (paymentModelOptional.isEmpty()) throw new NotFoundException("Payment not found for this user");

        return paymentModelOptional;
    }

    @Transactional
    @Override
    public void makePayment(PaymentCommandRecordDto paymentCommandRecordDto) {
        var payment = paymentRepository.findById(paymentCommandRecordDto.paymentId()).get();
        var user = userRepository.findById(paymentCommandRecordDto.userId()).get();
        var creditCard = creditCardRepository.findById(paymentCommandRecordDto.cardId()).get();

        payment = paymentStripeService.processStripePayment(payment, creditCard);
    }
}
