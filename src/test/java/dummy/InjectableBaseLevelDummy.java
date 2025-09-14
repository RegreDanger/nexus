package dummy;

import com.nexus.core.annotations.Inject;
import com.nexus.core.annotations.Injectable;

@Injectable
public class InjectableBaseLevelDummy {
    private SingletonDummy singleton;

    @Inject
    public InjectableBaseLevelDummy(SingletonDummy singleton) {
        this.singleton = singleton;
    }

    public SingletonDummy getSingletonFromBaseLevelInjectable() {
        return singleton;
    }
}
