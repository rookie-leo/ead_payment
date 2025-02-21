package com.ead.payment.services.Impl;

import com.ead.payment.enums.PaymentControl;
import com.ead.payment.models.CreditCardModel;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.services.PaymentStripeService;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {

    Logger logger = LogManager.getLogger(PaymentStripeServiceImpl.class);

    private static final String SUCCEEDED = "succeeded";
    private static final String SUCCEESS_MESSAGE = "payment effected - paymentIntent: ";
    private static final String ERROR_MESSAGE = "Another problem occurred, maybe unrelated to Stripe. With cause: {}";
    private static final String ERROR_MESSAGE_V1 = "payment error v1 - paymentIntent: ";
    private static final String ERROR_MESSAGE_V2 = "payment error v2 - paymentIntent: ";

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

            if (confirmPaymentIntent.getStatus().equals(SUCCEEDED)) {
                paymentModel.setPaymentControl(PaymentControl.EFFECTED);
                paymentModel.setPaymentMessage(SUCCEESS_MESSAGE + paymentIntentId);
                paymentModel.setPaymentCompletionDate(LocalDateTime.now(ZoneId.of("UTC")));
            } else {
                paymentModel.setPaymentControl(PaymentControl.ERROR);
                paymentModel.setPaymentMessage(ERROR_MESSAGE_V1 + paymentIntentId);
            }
        } catch (CardException cardException) {
            logger.error("A payment error occurred: {}", cardException.getMessage());
            try {
                paymentModel.setPaymentControl(PaymentControl.REFUSED);
                var paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                paymentModel.setPaymentMessage("payment refused v1 - paymentIntent: " + paymentIntentId
                        + ", cause: " + paymentIntent.getLastPaymentError().getCode()
                        + ", message: " + paymentIntent.getLastPaymentError().getMessage()
                );
            } catch (Exception ex) {
                logger.error(ERROR_MESSAGE, ex.getMessage());
                paymentModel.setPaymentMessage("payment refused v2 - paymentIntent: " + paymentIntentId);
            }
        } catch (Exception ex) {
            logger.error(ERROR_MESSAGE, ex.getMessage());
            paymentModel.setPaymentControl(PaymentControl.ERROR);
            paymentModel.setPaymentMessage(ERROR_MESSAGE_V2 + paymentIntentId);
        }

        return paymentModel;
    }

    private String getPaymentMethod(String creditCardNumber) {
        return switch (creditCardNumber) {
            case "4242424242424242" -> "pm_card_visa";
            case "5555555555554444" -> "pm_card_mastercard";
            case "4000000000009995" -> "pm_card_visa_chargeDeclinedInsufficientFunds";
            case "4000000000000127" -> "pm_card_chargeDeclinedIncorrectCvc";
            default -> "pm_card_visa_chargeDeclined";
        };
    }
}
