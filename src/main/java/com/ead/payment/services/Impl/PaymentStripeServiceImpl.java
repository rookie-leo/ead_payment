package com.ead.payment.services.Impl;

import com.ead.payment.models.CreditCardModel;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.services.PaymentStripeService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {

    @Value(value = "${ead.stripe.secretKey}")
    private String secretKey;

    @Override
    public PaymentModel processStripePayment(PaymentModel paymentModel, CreditCardModel creditCardModel) {
        Stripe.apiKey = secretKey;
        String paymentIntentId = null;

        try {
            var params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentModel.getValuePaid().multiply(new BigDecimal("100")).longValue())
                    .setCurrency("brl")
                    .setPaymentMethod(getPaymentMethod(creditCardModel.getCreditCardNumber().replaceAll(" ", "")))
                    .addPaymentMethodType("card")
                    .build();

            var paymentIntent = PaymentIntent.create(params);
            paymentIntentId = paymentIntent.getId();

            var paramsPaymentConfirm = PaymentIntentConfirmParams.builder().build();
            var confirmPaymentIntent = paymentIntent.confirm(paramsPaymentConfirm);


        } catch (Exception ex) {

        }

        return paymentModel;
    }

    private String getPaymentMethod(String creditCardNumber) {
        return switch (creditCardNumber) {
            case "4242424242424242" ->  "pm_card_visa";
            case "5555555555554444" -> "pm_card_mastercard";
            case "4000000000009995" -> "pm_card_visa_chargeDeclinedInsufficientFunds";
            case "4000000000000127" -> "pm_card_chargeDeclinedIncorrectCvc";
            default -> "pm_card_visa_chargeDeclined";
        };
    }
}
