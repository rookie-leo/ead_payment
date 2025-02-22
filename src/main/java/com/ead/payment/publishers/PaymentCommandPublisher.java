package com.ead.payment.publishers;

import com.ead.payment.dtos.PaymentCommandRecordDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentCommandPublisher {

    final RabbitTemplate rabbitTemplate;

    public PaymentCommandPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value(value = "${ead.broker.exchange.paymentCommandExchange}")
    private String paymentCommandExchange;

    @Value(value = "${ead.broker.key.paymentCommandKey}")
    private String paymentCommandKey;

    public void publisherPaymentCommand(PaymentCommandRecordDto paymentCommandRecordDto) {
        rabbitTemplate.convertAndSend(paymentCommandExchange, paymentCommandKey, paymentCommandRecordDto);
    }
}
