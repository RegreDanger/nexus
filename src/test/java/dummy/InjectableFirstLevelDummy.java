package dummy;

import com.nexus.core.annotations.Inject;
import com.nexus.core.annotations.Injectable;

@Injectable(level = 1)
public class InjectableFirstLevelDummy {
    private InjectableBaseLevelDummy baseLevel;

    @Inject
    public InjectableFirstLevelDummy(InjectableBaseLevelDummy baseLevel) {
        this.baseLevel = baseLevel;
    }

    public InjectableBaseLevelDummy getBaseLevelInstanceFromFirstLevelInjectable() {
        return baseLevel;
    }
}
