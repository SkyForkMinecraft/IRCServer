package cn.langya.client.events;

import com.cubk.event.impl.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventPacketReceive implements Event {
    private final String message;

    public EventPacketReceive(String message) {
        this.message = message;
    }
}