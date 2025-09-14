package dummy;

import com.nexus.core.annotations.Managed;
import com.nexus.core.annotations.WiringConfig;

@WiringConfig
public class WiringConfigSingletonDummy {

    @Managed
    public static SingletonDummy singletonBean() {
        return SingletonDummy.getInstance();
    }
}
