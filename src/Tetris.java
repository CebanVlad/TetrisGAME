import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Tetris extends JPanel implements ActionListener, KeyListener {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int BLOCK_SIZE = 30;

    private Timer timer;
    private int speed = 500;

    private Color[][] board;
    private Shape currentShape;

    private int score = 0;
    private boolean gameOver = false;

    public Tetris() {
        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        spawnShape();

        timer = new Timer(speed, this);
        timer.start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris GAME");
        Tetris game = new Tetris();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ===================== SHAPES ===================== //

    private class Shape {
        int[][] coords;
        Color color;
        int x = 3, y = 0;

        Shape(int[][] c, Color col) {
            coords = c;
            color = col;
        }

        void rotate() {
            int[][] oldCoords = coords; // salvăm forma veche

            // inversăm dimensiunile pentru rotație
            int[][] rotated = new int[coords[0].length][coords.length];
            for (int i = 0; i < coords.length; i++)
                for (int j = 0; j < coords[0].length; j++)
                    rotated[j][coords.length - 1 - i] = coords[i][j];

            coords = rotated;

            if (!isValidPosition()) {
                coords = oldCoords; // revenim la forma veche dacă nu e valid
            }
        }

        boolean isValidPosition() {
            for (int i = 0; i < coords.length; i++) {
                for (int j = 0; j < coords[0].length; j++) {
                    if (coords[i][j] == 1) {
                        int newX = x + j;
                        int newY = y + i;

                        if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT)
                            return false;

                        if (newY >= 0 && board[newY][newX] != null)
                            return false;
                    }
                }
            }
            return true;
        }
    }

    private void spawnShape() {
        int type = (int) (Math.random() * 7);

        switch (type) {
            case 0 -> currentShape = new Shape(new int[][]{{1, 1, 1, 1}}, Color.CYAN);
            case 1 -> currentShape = new Shape(new int[][]{{1, 1}, {1, 1}}, Color.YELLOW);
            case 2 -> currentShape = new Shape(new int[][]{{0, 1, 0}, {1, 1, 1}}, Color.MAGENTA);
            case 3 -> currentShape = new Shape(new int[][]{{1, 0, 0}, {1, 1, 1}}, Color.ORANGE);
            case 4 -> currentShape = new Shape(new int[][]{{0, 0, 1}, {1, 1, 1}}, Color.BLUE);
            case 5 -> currentShape = new Shape(new int[][]{{1, 1, 0}, {0, 1, 1}}, Color.GREEN);
            case 6 -> currentShape = new Shape(new int[][]{{0, 1, 1}, {1, 1, 0}}, Color.RED);
        }
    }

    // ===================== GAME LOGIC ===================== //

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            if (canMove(0, 1)) {
                currentShape.y++;
            } else {
                lockShape();
                clearLines();
                spawnShape();
                if (!currentShape.isValidPosition()) {
                    gameOver = true;
                    timer.stop();
                }
            }
            repaint();
        }
    }

    private boolean canMove(int dx, int dy) {
        currentShape.x += dx;
        currentShape.y += dy;

        boolean valid = currentShape.isValidPosition();

        if (!valid) {
            currentShape.x -= dx;
            currentShape.y -= dy;
        }

        return valid;
    }

    private void lockShape() {
        for (int i = 0; i < currentShape.coords.length; i++) {
            for (int j = 0; j < currentShape.coords[0].length; j++) {
                if (currentShape.coords[i][j] == 1) {
                    int px = currentShape.x + j;
                    int py = currentShape.y + i;
                    if (py >= 0)
                        board[py][px] = currentShape.color;
                }
            }
        }
    }

    private void clearLines() {
        int linesCleared = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == null) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesCleared++;
                for (int k = i; k > 0; k--) {
                    System.arraycopy(board[k - 1], 0, board[k], 0, BOARD_WIDTH);
                }
                for (int j = 0; j < BOARD_WIDTH; j++) board[0][j] = null;
                i++;
            }
        }

        if (linesCleared > 0)
            score += linesCleared * 100;
    }

    // ===================== DRAW ===================== //

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // blocurile căzute
        for (int y = 0; y < BOARD_HEIGHT; y++)
            for (int x = 0; x < BOARD_WIDTH; x++)
                if (board[y][x] != null) {
                    g.setColor(board[y][x]);
                    g.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }

        // forma curentă
        g.setColor(currentShape.color);
        for (int i = 0; i < currentShape.coords.length; i++)
            for (int j = 0; j < currentShape.coords[0].length; j++)
                if (currentShape.coords[i][j] == 1) {
                    int px = (currentShape.x + j) * BLOCK_SIZE;
                    int py = (currentShape.y + i) * BLOCK_SIZE;
                    g.fillRect(px, py, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(px, py, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(currentShape.color);
                }

        // scor
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Press UP to rotate", 10, 40);

        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER!", getWidth() / 2 - 40, getHeight() / 2);
        }
    }

    // ===================== CONTROLS ===================== //

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> canMove(-1, 0);
            case KeyEvent.VK_RIGHT -> canMove(1, 0);
            case KeyEvent.VK_DOWN -> canMove(0, 1);
            case KeyEvent.VK_UP -> currentShape.rotate();
            case KeyEvent.VK_SPACE -> {
                while (canMove(0, 1)) {}
            }
        }

        repaint();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
