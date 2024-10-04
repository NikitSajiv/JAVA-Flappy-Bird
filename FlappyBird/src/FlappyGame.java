import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyGame extends JPanel implements ActionListener, KeyListener {
    int windowWidth = 560;
    int windowHeight = 940;

    // Images
    Image background;
    Image birdImage;
    Image topPipeImage;
    Image bottomPipeImage;

    // Bird properties
    int birdXPosition = windowWidth / 8;
    int birdYPosition = windowWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdXPosition;
        int y = birdYPosition;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe properties
    int pipeXPosition = windowWidth;
    int pipeYPosition = 0;
    int pipeWidth = 64;  // 1/6 of the width
    int pipeHeight = 512;

    class Pipe {
        int x = pipeXPosition;
        int y = pipeYPosition;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int pipeVelocityX = -4;
    int birdVelocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipeList;
    Random rand = new Random();

    Timer gameTimer;
    Timer pipeTimer;
    boolean isGameOver = false;
    double score = 0;
    double highScore = 0;
    boolean newHighScore = false;  // Flag to track if a new high score is reached

    FlappyGame() {
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        background = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImage = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImage = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImage = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Bird initialization
        bird = new Bird(birdImage);
        pipeList = new ArrayList<>();

        // Timer to place pipes
        pipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPipes();
            }
        });
        pipeTimer.start();

        // Main game loop timer
        gameTimer = new Timer(1000 / 60, this);  // 60 frames per second
        gameTimer.start();
    }

    void addPipes() {
        int randomYPosition = (int) (pipeYPosition - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int gapBetweenPipes = windowHeight / 8;

        Pipe upperPipe = new Pipe(topPipeImage);
        upperPipe.y = randomYPosition;
        pipeList.add(upperPipe);

        Pipe lowerPipe = new Pipe(bottomPipeImage);
        lowerPipe.y = upperPipe.y + pipeHeight + gapBetweenPipes;
        pipeList.add(lowerPipe);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g);
    }

    void render(Graphics g) {
        // Background
        g.drawImage(background, 0, 0, windowWidth, windowHeight, null);

        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (Pipe pipe : pipeList) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score and High Score Display
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        if (isGameOver) {
            if (newHighScore) {
                // Display message in the middle of the screen for new high score
                String message = "Well done! New high score of " + (int) highScore;
                FontMetrics metrics = g.getFontMetrics(g.getFont());
                int x = (windowWidth - metrics.stringWidth(message)) / 2;
                int y = windowHeight / 2;
                g.drawString(message, x, y);
            } else {
                g.drawString("Game Over", 10, 35);
                g.drawString("Score: " + (int) score, 10, 75);
            }
            g.drawString("High Score: " + (int) highScore, 10, 115);
        } else {
            g.drawString("Score: " + (int) score, 10, 35);
            g.drawString("High Score: " + (int) highScore, 10, 75);
        }
    }

    void update() {
        // Update bird position with gravity
        birdVelocityY += gravity;
        bird.y += birdVelocityY;
        bird.y = Math.max(bird.y, 0);  // Prevent bird from going above the window

        // Move pipes
        for (Pipe pipe : pipeList) {
            pipe.x += pipeVelocityX;

            // Scoring logic
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;  // Increment score for each pipe set passed
                pipe.passed = true;
            }

            // Collision check
            if (checkCollision(bird, pipe)) {
                isGameOver = true;
            }
        }

        // Check if bird falls below the screen
        if (bird.y > windowHeight) {
            isGameOver = true;
        }
    }

    boolean checkCollision(Bird bird, Pipe pipe) {
        return bird.x < pipe.x + pipe.width &&
               bird.x + bird.width > pipe.x &&
               bird.y < pipe.y + pipe.height &&
               bird.y + bird.height > pipe.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();

        if (isGameOver) {
            if (score > highScore) {
                highScore = score;  // Update high score if the current score is higher
                newHighScore = true;  // Set flag to true
            }
            pipeTimer.stop();
            gameTimer.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            birdVelocityY = -9;

            if (isGameOver) {
                resetGame();
            }
        }
    }

    void resetGame() {
        bird.y = birdYPosition;
        birdVelocityY = 0;
        pipeList.clear();
        isGameOver = false;
        score = 0;
        newHighScore = false;  // Reset the flag for new high score
        gameTimer.start();
        pipeTimer.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}

