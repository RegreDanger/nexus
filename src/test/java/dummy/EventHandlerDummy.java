package dummy;

import com.nexus.core.annotations.Inject;
import com.nexus.core.event.EventHandler;

public class EventHandlerDummy implements EventHandler<EventDummy> {

    private InjectableFirstLevelDummy firstlevel;

    @Inject
    public EventHandlerDummy(InjectableFirstLevelDummy firstLevel) {
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
