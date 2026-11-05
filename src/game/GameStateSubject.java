package game;

public interface GameStateSubject {
    void attachGameStateObserver(GameStateObserver observer);
    void notifyGameStateObservers();
}
