package dummy;

public class SingletonDummy {
    private static SingletonDummy instance;
    private SingletonDummy() {}

    public static SingletonDummy getInstance() {
        if(instance == null) {
            instance = new SingletonDummy();
        }
        return instance;
    }

}
