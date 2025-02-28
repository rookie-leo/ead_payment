package com.ead.payment.consumer;

import com.ead.payment.dtos.UserEventRecordDto;
import com.ead.payment.enums.ActionType;
import com.ead.payment.enums.PaymentStatus;
import com.ead.payment.models.UserModel;
import com.ead.payment.services.UserService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class UserConsumer {

    final UserService userService;

    public UserConsumer(UserService userService) {
        this.userService = userService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${ead.broker.queue.userEventQueue.name}", durable = "true"),
            exchange = @Exchange(value = "${ead.broker.exchange.userEventExchange}",
                    type = ExchangeTypes.FANOUT, ignoreDeclarationExceptions = "true")
    ))
    public void listenUserEvent(@Payload UserEventRecordDto userEventRecordDto) {
        switch (ActionType.valueOf(userEventRecordDto.actionType())) {
            case CREATE -> {
                var userModel = userEventRecordDto.convertToUserModel(new UserModel());
                userModel.setPaymentStatus(PaymentStatus.NOTSTARTED);
                userService.save(userModel);
            }
            case UPDATE -> userService.save(
                    userEventRecordDto.convertToUserModel(userService.findById(userEventRecordDto.userId()).get())
            );
            case DELETE -> userService.delete(userEventRecordDto.userId());
        }
    }

}
