package dev.wuason.mechanics.actions.events;

import dev.wuason.mechanics.actions.Action;
import org.bukkit.event.Event;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class EventBukkit implements EventAction {
    private final Event event;

    public EventBukkit(Event event) {
        this.event = event;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerPlaceholders(Action action) {
        action.registerPlaceholder("$event$", event);
    }


    public Event getEvent() {
        return event;
    }
}
