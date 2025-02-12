package com.ead.payment.services.Impl;

import com.ead.payment.dtos.PaymentRequestRecordDto;
import com.ead.payment.enums.PaymentControl;
import com.ead.payment.models.CreditCardModel;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.models.UserModel;
import com.ead.payment.repositories.CreditCardRepository;
import com.ead.payment.repositories.PaymentRepository;
import com.ead.payment.repositories.UserRepository;
import com.ead.payment.services.PaymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    final PaymentRepository paymentRepository;
    final UserRepository userRepository;
    final CreditCardRepository creditCardRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, UserRepository userRepository, CreditCardRepository creditCardRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.creditCardRepository = creditCardRepository;
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



        return paymentModel;
    }

    @Override
    public Optional<PaymentModel> findLastPaymentByUser(UserModel userModel) {
        return paymentRepository.findTopByUserOrderByPaymentRequestDateDesc(userModel);
    }
}
