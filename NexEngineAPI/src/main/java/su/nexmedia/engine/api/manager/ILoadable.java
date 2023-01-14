package su.nexmedia.engine.api.manager;

public interface ILoadable {

    void setup();

    void shutdown();

    default void reload() {
        this.shutdown();
        this.setup();
    }
}
