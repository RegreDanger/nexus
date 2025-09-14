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
        System.out.println("Event dummy!");
    }
    
    public InjectableFirstLevelDummy getFirstlevel() {
        return firstlevel;
    }
}
