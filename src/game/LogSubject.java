package game;

public interface LogSubject {
    void attachLogObserver(LogObserver observer);
    void notifyLogObservers(String message);
}
