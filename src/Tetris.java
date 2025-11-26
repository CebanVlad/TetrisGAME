import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Tetris extends JPanel implements ActionListener {

    private final int BOARD_WIDTH = 12;   // mai lat
    private final int BOARD_HEIGHT = 24;  // mai înalt
    private final int TILE_SIZE = 35;     // pătrate mai mari
    private Timer timer;
    private boolean[][] board = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
    private Tetromino currentPiece;
    private int pieceX = 4, pieceY = 0;
    private int score = 0;
    private boolean gameOver = false;

    public Tetris() {
        setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);

        timer = new Timer(500, this);
        timer.start();

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (gameOver) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: if (canMove(-1,0)) pieceX--; break;
                    case KeyEvent.VK_RIGHT: if (canMove(1,0)) pieceX++; break;
                    case KeyEvent.VK_DOWN: if (canMove(0,1)) pieceY++; break;
                    case KeyEvent.VK_UP: rotatePiece(); break;
                }
                repaint();
            }
        });

        setFocusable(true);
        spawnPiece();
    }

    private void spawnPiece() {
        currentPiece = Tetromino.randomTetromino();
        pieceX = BOARD_WIDTH / 2 - 1;
        pieceY = 0;
        if (!canMove(0,0)) gameOver = true;
    }

    private boolean canMove(int dx, int dy) {
        for (Point p : currentPiece.getShape()) {
            int newX = pieceX + p.x + dx;
            int newY = pieceY + p.y + dy;
            if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) return false;
            if (board[newY][newX]) return false;
        }
        return true;
    }

    private void rotatePiece() {
        currentPiece.rotate();
        if (!canMove(0,0)) currentPiece.rotateBack();
    }

    private void fixPiece() {
        for (Point p : currentPiece.getShape()) {
            board[pieceY + p.y][pieceX + p.x] = true;
        }
        clearLines();
        spawnPiece();
    }

    private void clearLines() {
        for (int y = BOARD_HEIGHT-1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (!board[y][x]) full = false;
            }
            if (full) {
                score += 100;
                for (int i = y; i > 0; i--) {
                    System.arraycopy(board[i-1],0,board[i],0,BOARD_WIDTH);
                }
                for (int x = 0; x < BOARD_WIDTH; x++) board[0][x] = false;
                y++; // verifică linia nouă
            }
        }
        // crește viteza Timerului pe măsură ce scorul crește
        timer.setDelay(Math.max(100, 500 - score/5));
    }

    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            timer.stop();
        } else if (canMove(0,1)) {
            pieceY++;
        } else {
            fixPiece();
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // fundal gradient
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0,0,Color.DARK_GRAY,0,getHeight(),Color.BLACK);
        g2.setPaint(gp);
        g2.fillRect(0,0,getWidth(),getHeight());

        // board
        for (int y=0; y<BOARD_HEIGHT; y++) {
            for (int x=0; x<BOARD_WIDTH; x++) {
                if (board[y][x]) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(x*TILE_SIZE, y*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x*TILE_SIZE, y*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // piesa curenta
        if (!gameOver) {
            g.setColor(currentPiece.getColor());
            for (Point p : currentPiece.getShape()) {
                int px = (pieceX + p.x) * TILE_SIZE;
                int py = (pieceY + p.y) * TILE_SIZE;
                g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                g.setColor(currentPiece.getColor());
            }
        }

        // scor
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);

        // game over
        if (gameOver) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", getWidth()/2 - 90, getHeight()/2);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris Colorat și Dinamic");
        Tetris game = new Tetris();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

// Tetromino cu culori și rotații
class Tetromino {
    private Point[] shape;
    private int rotationIndex = 0;
    private Point[][] rotations;
    private Color color;

    private Tetromino(Point[][] rotations, Color color) {
        this.rotations = rotations;
        this.color = color;
        this.shape = rotations[0];
    }

    public Point[] getShape() { return shape; }
    public Color getColor() { return color; }

    public void rotate() {
        rotationIndex = (rotationIndex + 1) % rotations.length;
        shape = rotations[rotationIndex];
    }

    public void rotateBack() {
        rotationIndex = (rotationIndex - 1 + rotations.length) % rotations.length;
        shape = rotations[rotationIndex];
    }

    public static Tetromino randomTetromino() {
        Point[][][] allShapes = {
                // I
                { {new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3)},
                        {new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0)} },
                // O
                { {new Point(0,0), new Point(1,0), new Point(0,1), new Point(1,1)} },
                // T
                { {new Point(0,0), new Point(-1,1), new Point(0,1), new Point(1,1)},
                        {new Point(0,0), new Point(0,1), new Point(1,0), new Point(0,-1)},
                        {new Point(0,0), new Point(-1,0), new Point(0,-1), new Point(1,0)},
                        {new Point(0,0), new Point(-1,0), new Point(0,1), new Point(0,-1)} },
                // L
                { {new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,2)},
                        {new Point(0,0), new Point(1,0), new Point(2,0), new Point(0,1)},
                        {new Point(0,0), new Point(0,1), new Point(0,2), new Point(-1,2)},
                        {new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1)} },
                // J
                { {new Point(0,0), new Point(0,1), new Point(0,2), new Point(-1,2)},
                        {new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1)},
                        {new Point(0,0), new Point(1,0), new Point(0,1), new Point(0,2)},
                        {new Point(0,0), new Point(-2,0), new Point(-1,0), new Point(0,1)} },
                // S
                { {new Point(0,0), new Point(1,0), new Point(0,1), new Point(-1,1)},
                        {new Point(0,0), new Point(0,1), new Point(1,1), new Point(1,2)} },
                // Z
                { {new Point(0,0), new Point(-1,0), new Point(0,1), new Point(1,1)},
                        {new Point(0,0), new Point(0,1), new Point(-1,1), new Point(-1,2)} }
        };

        Color[] colors = {Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.ORANGE, Color.BLUE, Color.GREEN, Color.RED};

        Random r = new Random();
        int idx = r.nextInt(allShapes.length);
        Point[][] rotations = allShapes[idx];
        Color color = colors[r.nextInt(colors.length)];

        return new Tetromino(rotations, color);
    }
}
