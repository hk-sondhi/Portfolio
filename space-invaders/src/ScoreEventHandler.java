//handles score
public interface ScoreEventHandler {
    //updates score manager based on event and updates score
    void handleScoreEvent(ScoreEvent event, ScoreManager scoreManager);
    void setNextHandler(ScoreEventHandler nextHandler);
}
