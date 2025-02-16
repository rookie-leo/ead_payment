package com.ead.payment.consumer;

import com.ead.payment.dtos.PaymentCommandRecordDto;
import com.ead.payment.services.PaymentService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    final PaymentService paymentService;

    public PaymentConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${ead.broker.queue.paymentCommandQueue.name}", durable = "true"),
            exchange = @Exchange(value = "${ead.broker.exchange.paymentCommandExchange}", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = "${ead.broker.key.paymentCommandKey}"
    ))
    public void listenPaymentCommand(@Payload PaymentCommandRecordDto paymentCommandRecordDto) {
        System.out.println(paymentCommandRecordDto.paymentId());
        System.out.println(paymentCommandRecordDto.userId());
    }
}
