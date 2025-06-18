public class SpecialAlienHitHandler implements ScoreEventHandler {
    private ScoreEventHandler nextHandler;

    //handles score events for when special alien is hit
    @Override
    public void handleScoreEvent(ScoreEvent event, ScoreManager scoreManager) {
        //checks if type is special alien
        if (event.getType() == ScoreEvent.EventType.SPECIAL_ALIEN_HIT) {
           //adds points to score manager is special alien is hit
            scoreManager.addScore(event.getPoints());
        } else if (nextHandler != null) {
            //if speical alien is not hit, passed on to next handler in chain
            nextHandler.handleScoreEvent(event, scoreManager);
        }
    }

    //sets next handler in chain
    @Override
    public void setNextHandler(ScoreEventHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
