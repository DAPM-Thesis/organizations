package com.example.orgA.dto;

import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;

import java.util.Collection;

public record EventDto(
        String caseID,
        String activity,
        String timestamp,
        Collection<Attribute<?>> attributes
) {
    public static EventDto from(Event e) {
        return new EventDto(
                e.getCaseID(),
                e.getActivity(),
                e.getTimestamp(),
                e.getAttributes()
        );
    }
}
