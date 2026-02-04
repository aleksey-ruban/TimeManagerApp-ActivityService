package com.alekseyruban.timemanagerapp.activity_service.service.rabbit;

import com.alekseyruban.timemanagerapp.activity_service.DTO.rabbit.ChronometryCreatedEvent;
import com.alekseyruban.timemanagerapp.activity_service.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChronometryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishChronometryCreated(ChronometryCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHRONOMETRY_EVENTS_EXCHANGE,
                RabbitConfig.CHRONOMETRY_CREATED_KEY,
                event
        );
    }

}
