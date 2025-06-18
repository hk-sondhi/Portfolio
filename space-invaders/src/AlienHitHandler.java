//handles score when alien is hit
public class AlienHitHandler implements ScoreEventHandler {
    //if current handler cant process score event, delegates to next handler
    private ScoreEventHandler nextHandler;

    //when alien is hit activates score event to update score
    @Override
    public void handleScoreEvent(ScoreEvent event, ScoreManager scoreManager) {
        // if the ScoreEvent type is ALIEN_HIT, then the handler updates the score using the points from the event
        if (event.getType() == ScoreEvent.EventType.ALIEN_HIT) {
            scoreManager.addScore(event.getPoints());
        } else if (nextHandler != null) {
            nextHandler.handleScoreEvent(event, scoreManager);
        }
    }

    @Override
    public void setNextHandler(ScoreEventHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
