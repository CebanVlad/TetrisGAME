import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class Tetris extends JPanel implements ActionListener, KeyListener {
    // Board
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int BLOCK_SIZE = 30; // mărimea vizuală a blocului
    private final int SIDEBAR_WIDTH = 160; // spațiu pentru next / score

    // Timing
    private Timer timer;
    private int speed = 500; // ms per step (poți micșora pentru mai mult challenge)

    // Game state
    private Color[][] board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
    private Shape currentShape;
    private Shape nextShape;
    private int score = 0;
    private boolean gameOver = false;

    // UI
    private JButton playAgainButton;

    private Random rand = new Random();

    public Tetris() {
        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE + SIDEBAR_WIDTH, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(new Color(40, 30, 20)); // fallback
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        // Play again button (centrat)
        playAgainButton = new JButton("Play Again");
        playAgainButton.setFocusable(false);
        playAgainButton.setVisible(false);
        playAgainButton.addActionListener(e -> resetGame());
        add(playAgainButton);

        // initial shapes
        nextShape = randomShape();
        spawnShape();

        timer = new Timer(speed, this);
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris");
            Tetris game = new Tetris();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }

    // ================= SHAPE CLASS & HELPERS =================
    private class Shape {
        int[][] coords; // matrice binară
        Color color;
        int x = 3, y = -1; // poziție inițială: y poate fi negativ

        Shape(int[][] c, Color col) {
            // copie profundă
            coords = new int[c.length][c[0].length];
            for (int i = 0; i < c.length; i++)
                System.arraycopy(c[i], 0, coords[i], 0, c[0].length);
            color = col;
        }

        void rotate() {
            // rotire 90° clockwise
            int[][] old = coords;
            int rows = coords.length, cols = coords[0].length;
            int[][] rotated = new int[cols][rows];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    rotated[j][rows - 1 - i] = coords[i][j];

            coords = rotated;
            if (!isValidPosition()) coords = old;
        }

        boolean isValidPosition() {
            for (int i = 0; i < coords.length; i++) {
                for (int j = 0; j < coords[0].length; j++) {
                    if (coords[i][j] == 1) {
                        int newX = x + j;
                        int newY = y + i;
                        if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT) return false;
                        if (newY >= 0 && board[newY][newX] != null) return false;
                    }
                }
            }
            return true;
        }
    }

    private Shape randomShape() {
        int t = rand.nextInt(7);
        return switch (t) {
            case 0 -> new Shape(new int[][]{{1,1,1,1}}, new Color(0x00cccc)); // I - cyan
            case 1 -> new Shape(new int[][]{{1,1},{1,1}}, new Color(0xFFD700)); // O - yellow
            case 2 -> new Shape(new int[][]{{0,1,0},{1,1,1}}, new Color(0xB03060)); // T - magenta-ish
            case 3 -> new Shape(new int[][]{{1,0,0},{1,1,1}}, new Color(0xFF8C00)); // J - orange
            case 4 -> new Shape(new int[][]{{0,0,1},{1,1,1}}, new Color(0x1E90FF)); // L - blue
            case 5 -> new Shape(new int[][]{{1,1,0},{0,1,1}}, new Color(0x32CD32)); // S - green
            default -> new Shape(new int[][]{{0,1,1},{1,1,0}}, new Color(0xFF4C4C)); // Z - red
        };
    }

    private void spawnShape() {
        currentShape = nextShape;
        currentShape.x = (BOARD_WIDTH - currentShape.coords[0].length) / 2; // centrare orizontală
        currentShape.y = -getTopPadding(currentShape); // plasează puțin deasupra
        nextShape = randomShape();

        if (!currentShape.isValidPosition()) {
            gameOver = true;
            timer.stop();
            playAgainButton.setVisible(true);
            positionPlayAgainButton();
        }
    }

    // top padding: linii goale de sus în matricea piesei
    private int getTopPadding(Shape s) {
        for (int i = 0; i < s.coords.length; i++) {
            for (int j = 0; j < s.coords[0].length; j++) {
                if (s.coords[i][j] == 1) return i;
            }
        }
        return 0;
    }

    // ================= GAME CONTROLS =================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            if (canMove(0, 1)) {
                currentShape.y++;
            } else {
                lockShape();
                clearLines();
                spawnShape();
            }
            repaint();
        }
    }

    private boolean canMove(int dx, int dy) {
        int testX = currentShape.x + dx;
        int testY = currentShape.y + dy;
        for (int i = 0; i < currentShape.coords.length; i++) {
            for (int j = 0; j < currentShape.coords[0].length; j++) {
                if (currentShape.coords[i][j] == 1) {
                    int nx = testX + j;
                    int ny = testY + i;
                    if (nx < 0 || nx >= BOARD_WIDTH || ny >= BOARD_HEIGHT) return false;
                    if (ny >= 0 && board[ny][nx] != null) return false;
                }
            }
        }
        return true;
    }

    private void lockShape() {
        for (int i = 0; i < currentShape.coords.length; i++) {
            for (int j = 0; j < currentShape.coords[0].length; j++) {
                if (currentShape.coords[i][j] == 1) {
                    int px = currentShape.x + j;
                    int py = currentShape.y + i;
                    if (py >= 0 && py < BOARD_HEIGHT && px >= 0 && px < BOARD_WIDTH)
                        board[py][px] = currentShape.color;
                }
            }
        }
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == null) { full = false; break; }
            }
            if (full) {
                linesCleared++;
                for (int r = y; r > 0; r--) {
                    System.arraycopy(board[r-1], 0, board[r], 0, BOARD_WIDTH);
                }
                for (int x = 0; x < BOARD_WIDTH; x++) board[0][x] = null;
                y++; // recheck same index after shift
            }
        }
        if (linesCleared > 0) score += linesCleared * 100;
    }

    // ================= RESET =================
    private void resetGame() {
        for (int y = 0; y < BOARD_HEIGHT; y++)
            for (int x = 0; x < BOARD_WIDTH; x++)
                board[y][x] = null;
        score = 0;
        gameOver = false;
        nextShape = randomShape();
        spawnShape();
        playAgainButton.setVisible(false);
        timer.setDelay(speed);
        timer.start();
        requestFocusInWindow();
        repaint();
    }

    // ================= DRAWING =================
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background tile pattern (brown squares)
        drawBackgroundTiles(g);

        // Board area background (slightly darker)
        g.setColor(new Color(60, 45, 30, 200));
        g.fillRoundRect(8, 8, BOARD_WIDTH * BLOCK_SIZE - 16, BOARD_HEIGHT * BLOCK_SIZE - 16, 8, 8);

        // Draw fixed blocks
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != null) {
                    drawBlock(g, x * BLOCK_SIZE, y * BLOCK_SIZE, board[y][x]);
                }
            }
        }

        // Draw current piece
        if (currentShape != null) {
            for (int i = 0; i < currentShape.coords.length; i++) {
                for (int j = 0; j < currentShape.coords[0].length; j++) {
                    if (currentShape.coords[i][j] == 1) {
                        int px = (currentShape.x + j) * BLOCK_SIZE;
                        int py = (currentShape.y + i) * BLOCK_SIZE;
                        drawBlock(g, px, py, currentShape.color);
                    }
                }
            }
        }

        // Sidebar area
        int sidebarX = BOARD_WIDTH * BLOCK_SIZE + 10;
        g.setColor(new Color(35, 30, 25, 220));
        g.fillRoundRect(sidebarX, 10, SIDEBAR_WIDTH - 20, PANEL_HEIGHT() - 20, 10, 10);

        // Draw next piece label and next piece
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Next:", sidebarX + 12, 30);

        if (nextShape != null) {
            drawNextShape(g, sidebarX + 20, 40);
        }

        // Score
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Score: " + score, sidebarX + 12, 140);

        // Controls
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("← → : move", sidebarX + 12, 180);
        g.drawString("↑ : rotate", sidebarX + 12, 200);
        g.drawString("↓ : soft drop", sidebarX + 12, 220);
        g.drawString("Space : hard drop", sidebarX + 12, 240);

        // Game over text
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            String msg = "GAME OVER";
            FontMetrics fm = g.getFontMetrics();
            int mx = (getWidth() - fm.stringWidth(msg)) / 2;
            int my = getHeight() / 2 - 20;
            g.drawString(msg, mx, my);

            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String msg2 = "Score: " + score;
            int mx2 = (getWidth() - g.getFontMetrics().stringWidth(msg2)) / 2;
            g.drawString(msg2, mx2, my + 30);
        }

        g.dispose();
    }

    private int PANEL_HEIGHT() {
        return BOARD_HEIGHT * BLOCK_SIZE;
    }

    private void drawBackgroundTiles(Graphics2D g) {
        // desenăm mici pătrate maro pamp
        int tile = BLOCK_SIZE / 2;
        Color base = new Color(90, 70, 50);
        Color alt = new Color(75, 55, 40);
        for (int y = 0; y < PANEL_HEIGHT(); y += tile) {
            for (int x = 0; x < BOARD_WIDTH * BLOCK_SIZE + SIDEBAR_WIDTH; x += tile) {
                boolean odd = ((x / tile) + (y / tile)) % 2 == 0;
                g.setColor(odd ? base : alt);
                g.fillRect(x, y, tile, tile);
            }
        }
    }

    private void drawBlock(Graphics2D g, int x, int y, Color c) {
        int pad = 4;
        int w = BLOCK_SIZE - pad;
        int h = BLOCK_SIZE - pad;
        int rx = x + pad / 2;
        int ry = y + pad / 2;

        // gradient pentru efect 3D
        GradientPaint gp = new GradientPaint(rx, ry, c.brighter(), rx + w, ry + h, c.darker());
        g.setPaint(gp);
        RoundRectangle2D rounded = new RoundRectangle2D.Float(rx, ry, w, h, 8, 8);
        g.fill(rounded);

        // highlight (suprapus)
        g.setColor(new Color(255,255,255,80));
        g.fill(new RoundRectangle2D.Float(rx+2, ry+2, w/2f, h/4f, 6, 6));

        // contur întunecat
        g.setColor(c.darker().darker());
        g.setStroke(new BasicStroke(2));
        g.draw(rounded);
    }

    private void drawNextShape(Graphics2D g, int sx, int sy) {
        // desenăm într-un pătrat 4x4 pentru vizualizare
        int box = BLOCK_SIZE;
        int startX = sx;
        int startY = sy;
        // centru approx
        int offsetX = 20;
        int offsetY = 10;

        for (int i = 0; i < nextShape.coords.length; i++) {
            for (int j = 0; j < nextShape.coords[0].length; j++) {
                if (nextShape.coords[i][j] == 1) {
                    int px = startX + offsetX + j * box;
                    int py = startY + offsetY + i * box;
                    drawBlock(g, px, py, nextShape.color);
                }
            }
        }
    }

    private void positionPlayAgainButton() {
        int w = 140, h = 36;
        playAgainButton.setBounds((getWidth() - w) / 2, (getHeight() - h) / 2 + 40, w, h);
    }

    // ================= KEY HANDLERS =================
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                if (canMove(-1, 0)) currentShape.x--;
            }
            case KeyEvent.VK_RIGHT -> {
                if (canMove(1, 0)) currentShape.x++;
            }
            case KeyEvent.VK_DOWN -> {
                // soft drop (mai rapid doar cât ții tasta)
                if (canMove(0, 1)) {
                    currentShape.y++;
                    score += 1; // mic bonus pentru soft drop
                }
            }
            case KeyEvent.VK_UP -> {
                currentShape.rotate();
            }
            case KeyEvent.VK_SPACE -> {
                // hard drop
                int drops = 0;
                while (canMove(0, 1)) {
                    currentShape.y++;
                    drops++;
                }
                score += drops * 2;
                // imediat lockăm și trecem la pasul următor
                lockShape();
                clearLines();
                spawnShape();
            }
        }
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
