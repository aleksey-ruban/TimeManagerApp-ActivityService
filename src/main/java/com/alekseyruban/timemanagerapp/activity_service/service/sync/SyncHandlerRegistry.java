package com.alekseyruban.timemanagerapp.activity_service.service.sync;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SyncHandlerRegistry {

    private final Map<SyncObjectType, SyncHandler> handlers;

    public SyncHandlerRegistry(List<SyncHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        SyncHandler::supports,
                        Function.identity()
                ));
    }

    public SyncHandler get(SyncObjectType type) {
        return handlers.get(type);
    }
}