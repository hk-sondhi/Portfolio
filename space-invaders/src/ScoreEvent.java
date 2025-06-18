//represents scoring event in a game
public class ScoreEvent {
    //enum to define the types of scoring events, regular and special alien
    public enum EventType { ALIEN_HIT, SPECIAL_ALIEN_HIT }

    //type of score event
    private EventType type;
    //number of points associated with the score event
    private int points;

    //constructor to create a score event
    public ScoreEvent(EventType type, int points) {
        this.type = type;   //type of event
        this.points = points;   //point value for the event
    }

    //returns type of event
    public EventType getType() {
        return type;
    }

    //returns point value of the event
    public int getPoints() {
        return points;
    }
}
