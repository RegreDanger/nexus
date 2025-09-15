package dummy;

import java.io.Serializable;

import com.nexus.core.annotations.Inject;
import com.nexus.core.event.EventHandler;

public class EventHandlerInterfacesDummy implements Serializable, EventHandler<EventDummy> {
    private InjectableFirstLevelDummy firstlevel;

    @Inject
    public EventHandlerInterfacesDummy(InjectableFirstLevelDummy firstLevel) {
        this.firstlevel = firstLevel;
    }

    @Override
    public void on(EventDummy event) {
        //Random operation
    }
    
    public InjectableFirstLevelDummy getFirstlevel() {
        return firstlevel;
    }
}
