package com.ead.payment.publishers;

import com.ead.payment.dtos.PaymentEventDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    final RabbitTemplate rabbitTemplate;

    @Value(value = "${ead.broker.exchange.paymentEventExchange}")
    private String exchangePaymentEvent;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentEvent(PaymentEventDto paymentEventDto) {
        rabbitTemplate.convertAndSend(exchangePaymentEvent, "", paymentEventDto);
    }
}
