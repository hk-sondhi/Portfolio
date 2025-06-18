import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Shield {
    private int positionX;  //x position of shield
    private int positionY;  //y position of shield
    private int scaledWidth;  // Scaled width of the shield
    private int scaledHeight; // Scaled height of the shield
    private BufferedImage image; // The shield image
    private boolean[][] pixels; // Tracks which pixels are active

    //constructor to initialise shield postion, image and scaling
    public Shield(int x, int y, String imagePath, int scaledWidth, int scaledHeight) {
        this.positionX = x;
        this.positionY = y;
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;

        try {
            // Load the shield image
            image = ImageIO.read(getClass().getResource(imagePath));
        } catch (IOException e) {
            //handles errors loading image
            e.printStackTrace();
        }

        // Initialise the pixels array to match the image size
        if (image != null) {
            pixels = new boolean[image.getWidth()][image.getHeight()];
            //mark all pixels as active (shield intact initially)
            for (int i = 0; i < pixels.length; i++) {
                for (int j = 0; j < pixels[0].length; j++) {
                    pixels[i][j] = true;
                }
            }
        }
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getWidth() {
        return scaledWidth; // Return scaled width
    }

    public int getHeight() {
        return scaledHeight; // Return scaled height
    }

    //check if shield is completely destroyed (all pixels inactive)
    public boolean isDestroyed() {
        // Check if all pixels are destroyed
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                if (pixels[i][j]) {
                    return false; // If any pixel is active, shield is not fully destroyed
                }
            }
        }
        return true; // All pixels are destroyed
    }

    //method to apply damage to shield when hit by a bullet
    public void takeDamage(int bulletX, int bulletY) {
        //convert bullet coordinates to shield coordinates
        int localX = (bulletX - positionX) * image.getWidth() / scaledWidth;
        int localY = (bulletY - positionY) * image.getHeight() / scaledHeight;

        //define damage radius and iterations for damage
        int damageRadius = 50;
        int iterations = 5;

        //apply damage over iterations
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int dx = -damageRadius; dx <= damageRadius; dx++) {
                for (int dy = -damageRadius; dy <= damageRadius; dy++) {
                    //calculates the pixel coordinates affected by the damage
                    int px = localX + dx;
                    int py = localY + dy;
                    if (px >= 0 && px < pixels.length && py >= 0 && py < pixels[0].length) {
                        //checks if damage is within the circular area and randomly destroy pixels
                        if (dx * dx + dy * dy <= damageRadius * damageRadius && Math.random() > 0.2) {
                            pixels[px][py] = false; //mark pixel as destroyed (inactive)
                        }
                    }
                }
            }
            damageRadius += 2; //increase damage radius with each radius
        }
    }



    //method to draw the shield on screen
    public void draw(Graphics g) {
        if (image != null) {
            // Create a copy of the image to apply pixel modifications
            BufferedImage renderedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            //iterate through all pixels and update based on shield damage
            for (int i = 0; i < pixels.length; i++) {
                for (int j = 0; j < pixels[0].length; j++) {
                    if (pixels[i][j]) {
                        //if pixel active, Copy active pixels from the original image
                        renderedImage.setRGB(i, j, image.getRGB(i, j));
                    } else {
                        // Set destroyed pixels to transparent
                        renderedImage.setRGB(i, j, 0x00000000);
                    }
                }
            }

            // Draw the updated shield image on the screen, scaled to the desired dimensions
            g.drawImage(renderedImage, positionX, positionY, scaledWidth, scaledHeight, null);
        }
    }
}