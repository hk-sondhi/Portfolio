public class Bullet {
    private int positionX; // x coordinate of the bullets position
    private int positionY; // y coordinate of the bullets position
    private int speed; // The speed at which the bullet moves
    private String owner; // This tracks whether the bullet belongs to "player" or "alien"

    // Constructor to initialize the bullet's position, speed, and owner.
    public Bullet(int x, int y, int speed, String owner) {
        this.positionX = x;
        this.positionY = y;
        this.speed = speed;
        this.owner = owner;
    }

    // Updates the bullet's position by moving it along the y-axis based on its speed.
    public void update() {
        positionY += speed;
    }

    //returns the x coordinate of the bullet
    public int getPositionX() {
        return positionX;
    }

    //returns the y coordinate of the bullet
    public int getPositionY() {
        return positionY;
    }

    // Returns the owner of the bullet ("player" or "alien") as a string.
    public String getOwner() {
        return owner;
    }
}