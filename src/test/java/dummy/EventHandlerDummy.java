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
        //Random operation
    }
    
    public InjectableFirstLevelDummy getFirstlevel() {
        return firstlevel;
    }
    
}
