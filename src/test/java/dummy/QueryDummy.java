package dummy;

import com.nexus.core.annotations.Inject;
import com.nexus.core.cqrs.Query;

public class QueryDummy implements Query<String, String>{
    private InjectableBaseLevelDummy baseLevel;

    @Inject
    public QueryDummy(InjectableBaseLevelDummy baseLevel) {
        this.baseLevel = baseLevel;
    }

    @Override
    public String handle(String input) {
        return "Hello from QueryDummy! input:" + input;
    }

    public InjectableBaseLevelDummy getBaseLevel() {
        return baseLevel;
    }
    
}

