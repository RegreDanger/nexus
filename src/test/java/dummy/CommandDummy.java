package dummy;

import com.nexus.core.annotations.Inject;
import com.nexus.core.cqrs.Command;

public class CommandDummy implements Command<String, String>{
    private InjectableFirstLevelDummy firstLevel;

    @Inject
    public CommandDummy(InjectableFirstLevelDummy firstLevel) {
        this.firstLevel = firstLevel;
    }

    @Override
    public String handle(String input) {
        return "Hello from CommandDummy! input:" + input;
    }

    public InjectableFirstLevelDummy getFirstLevel() {
        return firstLevel;
    }
    
}
