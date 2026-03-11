package Games;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class BlokusSandbox extends JFrame {

    private final int BOARD_CELL_SIZE = 25; 
    private final int TRAY_CELL_SIZE = 15;  
    private final int BOARD_CELLS = 20;

    private final JPanel boardPanel;
    private final JPanel leftTraysContainer;
    private final JPanel rightTraysContainer;
    private final JLayeredPane layeredPane;
    
    private final TrayZone[] zones = new TrayZone[4];
    private final Map<Point, Integer> globalGridCounts = new HashMap<>();
    
    // 21 Hình khối của Blokus
    private final int[][][] SHAPE_TEMPLATES = {
        {{0,0}}, {{0,0},{1,0}}, {{0,0},{1,0},{2,0}}, {{0,0},{0,1},{1,0}}, 
        {{0,0},{1,0},{2,0},{3,0}}, {{0,0},{1,0},{2,0},{0,1}}, {{0,0},{1,0},{2,0},{1,1}}, 
        {{0,0},{1,0},{0,1},{1,1}}, {{0,0},{1,0},{1,1},{2,1}}, {{0,0},{1,0},{2,0},{3,0},{4,0}}, 
        {{0,0},{1,0},{2,0},{3,0},{0,1}}, {{0,0},{1,0},{2,0},{3,0},{1,1}}, {{0,0},{1,0},{0,1},{1,1},{0,2}}, 
        {{0,0},{1,0},{2,0},{0,1},{0,2}}, {{0,0},{1,0},{2,0},{1,1},{1,2}}, {{0,0},{1,0},{2,0},{0,1},{2,1}}, 
        {{0,0},{1,0},{2,0},{2,1},{3,1}}, {{0,0},{1,0},{1,1},{1,2},{2,2}}, {{0,2},{1,2},{1,1},{2,1},{2,0}}, 
        {{1,0},{0,1},{1,1},{2,1},{1,2}}, {{1,0},{2,0},{0,1},{1,1},{1,2}}
    };

    public BlokusSandbox() {
        setTitle("Blokus");
        setSize(1500, 700); // Chiều cao tối ưu
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // --- TOP: BẢNG ĐIỀU KHIỂN ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(new Color(40, 45, 52));
        JButton resetBtn = new JButton("Reset Game");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 14));
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> resetGame());
        topPanel.add(resetBtn);
        
        JLabel hintLabel = new JLabel("Left-Drag: Move piece (Auto-Scales)  |  Right-Click: Rotate  |  Red '!' = Error");
        hintLabel.setForeground(Color.LIGHT_GRAY);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        topPanel.add(hintLabel);
        add(topPanel, BorderLayout.NORTH);

        // --- LAYERED PANE ---
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // --- BOARD ---
        boardPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(230, 230, 230));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.LIGHT_GRAY);
                for (int i = 0; i <= BOARD_CELLS; i++) {
                    g.drawLine(i * BOARD_CELL_SIZE, 0, i * BOARD_CELL_SIZE, BOARD_CELLS * BOARD_CELL_SIZE);
                    g.drawLine(0, i * BOARD_CELL_SIZE, BOARD_CELLS * BOARD_CELL_SIZE, i * BOARD_CELL_SIZE);
                }
            }
        };
        int boardPx = BOARD_CELLS * BOARD_CELL_SIZE; // 500
        int boardX = (1500 - boardPx) / 2; // Canh giữa (Tọa độ X = 500)
        int boardY = 40; // Đẩy lên trên để vừa chiều cao 700
        boardPanel.setBounds(boardX, boardY, boardPx, boardPx);
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
        layeredPane.add(boardPanel, JLayeredPane.DEFAULT_LAYER);

        // --- TRAYS (Hai bên hông) ---
        // Thêm khoảng cách dọc (10px) giữa 2 khay của 2 người chơi để không bị dính vào nhau
        leftTraysContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        leftTraysContainer.setBounds(20, 15, 450, 580); 
        leftTraysContainer.setOpaque(false);
        
        rightTraysContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        rightTraysContainer.setBounds(boardX + boardPx + 30, 15, 450, 580); 
        rightTraysContainer.setOpaque(false);
        
        Color[] initialColors = {new Color(220, 50, 50), new Color(50, 120, 220), new Color(50, 180, 50), new Color(220, 180, 20)};
        String[] defaultNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
        
        for (int i = 0; i < 4; i++) {
            zones[i] = new TrayZone(defaultNames[i], initialColors[i]);
            if (i < 2) leftTraysContainer.add(zones[i].panel);
            else rightTraysContainer.add(zones[i].panel);
        }
        
        layeredPane.add(leftTraysContainer, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(rightTraysContainer, JLayeredPane.DEFAULT_LAYER);

        resetGame();
    }

    private void updateOverlaps() {
        globalGridCounts.clear();
        for (TrayZone zone : zones) {
            for (PolyPiece p : zone.piecesList) {
                if (p.isOnBoard || p.isDragging) {
                    int gridX, gridY;
                    if (p.isDragging && p.isOverBoard()) {
                        Point pLoc = SwingUtilities.convertPoint(p.getParent(), p.getLocation(), boardPanel);
                        gridX = Math.round((float) pLoc.x / BOARD_CELL_SIZE);
                        gridY = Math.round((float) pLoc.y / BOARD_CELL_SIZE);
                    } else if (p.isOnBoard && !p.isDragging) {
                        gridX = (p.getX() - boardPanel.getX()) / BOARD_CELL_SIZE;
                        gridY = (p.getY() - boardPanel.getY()) / BOARD_CELL_SIZE;
                    } else {
                        continue;
                    }

                    for (Point pt : p.blocks) {
                        Point cell = new Point(gridX + pt.x, gridY + pt.y);
                        globalGridCounts.put(cell, globalGridCounts.getOrDefault(cell, 0) + 1);
                    }
                }
            }
        }
        layeredPane.repaint(); 
    }

    private void resetGame() {
        boardPanel.removeAll();
        for (Component c : layeredPane.getComponents()) {
            if (c instanceof PolyPiece) {
                layeredPane.remove(c);
            }
        }
        
        for (TrayZone zone : zones) zone.resetZone();
        updateOverlaps();
        
        boardPanel.repaint();
        layeredPane.repaint();
    }

    // ==========================================
    // CLASS QUẢN LÝ KHAY (TRAY ZONE)
    // ==========================================
    private class TrayZone {
        JPanel panel;
        JPanel piecesArea;
        JTextField nameField;
        JButton randomColorBtn;
        JLabel scoreLabel;
        
        Color currentColor;
        Color defaultColor;
        List<PolyPiece> piecesList = new ArrayList<>();

        public TrayZone(String defaultName, Color defaultColor) {
            this.defaultColor = defaultColor;
            this.currentColor = defaultColor;

            panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            panel.setBackground(new Color(245, 245, 245));

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            header.setOpaque(false);
            
            nameField = new JTextField(defaultName, 10);
            nameField.setFont(new Font("Arial", Font.BOLD, 15));
            nameField.setHorizontalAlignment(JTextField.CENTER);
            header.add(nameField);
            
            randomColorBtn = new JButton("Random Color");
            randomColorBtn.setFocusPainted(false);
            randomColorBtn.addActionListener(e -> {
                currentColor = new Color((int)(Math.random() * 0x1000000));
                for (PolyPiece p : piecesList) p.repaint();
            });
            header.add(randomColorBtn);

            scoreLabel = new JLabel("Score: 89"); 
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 15));
            header.add(scoreLabel);
            
            panel.add(header, BorderLayout.NORTH);

            piecesArea = new JPanel(null); 
            piecesArea.setOpaque(false);
            panel.add(piecesArea, BorderLayout.CENTER);
        }

        public void resetZone() {
            piecesArea.removeAll();
            piecesList.clear();
            currentColor = defaultColor;
            
            randomColorBtn.setEnabled(true);

            // Căn chỉnh lại để fit gọn trong khung nhỏ
            int startX = 10, startY = 10, maxHeightInRow = 0;
            for (int[][] shape : SHAPE_TEMPLATES) {
                PolyPiece piece = new PolyPiece(shape, this);
                
                if (startX + piece.getWidth() > 430) { 
                    startX = 10; 
                    startY += maxHeightInRow + 8; // Giảm khoảng cách dòng
                    maxHeightInRow = 0; 
                }
                
                piece.homeLocation = new Point(startX, startY); 
                piece.setLocation(startX, startY);
                piecesArea.add(piece);
                piecesList.add(piece);
                
                startX += piece.getWidth() + 8; // Giảm khoảng cách ngang giữa các mảnh
                if (piece.getHeight() > maxHeightInRow) maxHeightInRow = piece.getHeight();
            }
            updateScoreAndState();
        }

        public void updateScoreAndState() {
            int score = 0;
            boolean hasPieceOnBoard = false;
            for (PolyPiece p : piecesList) {
                if (p.getParent() == piecesArea) score += p.blocks.length; 
                else if (p.getParent() == layeredPane && !p.isDragging) hasPieceOnBoard = true;
            }
            scoreLabel.setText("Score: " + score);
            randomColorBtn.setEnabled(!hasPieceOnBoard);
        }
    }

    // ==========================================
    // CLASS MẢNH GHÉP (POLY PIECE)
    // ==========================================
    private class PolyPiece extends JComponent {
        Point[] blocks;
        TrayZone ownerZone;
        
        Point homeLocation; 
        int dragMouseX, dragMouseY;
        boolean isDragging = false;
        boolean isOnBoard = false; 

        public PolyPiece(int[][] shapeTemplate, TrayZone owner) {
            this.ownerZone = owner;
            this.blocks = new Point[shapeTemplate.length];
            for (int i = 0; i < shapeTemplate.length; i++) {
                this.blocks[i] = new Point(shapeTemplate[i][0], shapeTemplate[i][1]);
            }
            
            calculateBounds();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        rotatePiece();
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        isDragging = true;
                        dragMouseX = e.getX();
                        dragMouseY = e.getY();
                        
                        Point p = SwingUtilities.convertPoint(getParent(), getLocation(), layeredPane);
                        Container oldParent = getParent();
                        oldParent.remove(PolyPiece.this);
                        layeredPane.add(PolyPiece.this, JLayeredPane.DRAG_LAYER);
                        setLocation(p);
                        
                        updateOverlaps(); 
                        ownerZone.updateScoreAndState();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!isDragging || !SwingUtilities.isLeftMouseButton(e)) return;
                    isDragging = false;

                    boolean dropOnBoard = isOverBoard();
                    layeredPane.remove(PolyPiece.this);

                    if (dropOnBoard) {
                        isOnBoard = true;
                        calculateBounds();
                        Point p = SwingUtilities.convertPoint(layeredPane, getLocation(), boardPanel);
                        int gridX = Math.round((float) p.x / BOARD_CELL_SIZE);
                        int gridY = Math.round((float) p.y / BOARD_CELL_SIZE);
                        
                        layeredPane.add(PolyPiece.this, JLayeredPane.PALETTE_LAYER);
                        setLocation(boardPanel.getX() + gridX * BOARD_CELL_SIZE, boardPanel.getY() + gridY * BOARD_CELL_SIZE);
                    } else {
                        isOnBoard = false;
                        calculateBounds();
                        ownerZone.piecesArea.add(PolyPiece.this);
                        setLocation(homeLocation); 
                    }
                    
                    updateOverlaps();
                    ownerZone.updateScoreAndState();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging && SwingUtilities.isLeftMouseButton(e)) {
                        boolean currentlyOverBoard = isOverBoard();
                        
                        if (currentlyOverBoard != isOnBoard) {
                            isOnBoard = currentlyOverBoard;
                            double scale = isOnBoard ? (double)BOARD_CELL_SIZE/TRAY_CELL_SIZE : (double)TRAY_CELL_SIZE/BOARD_CELL_SIZE;
                            
                            int newDragX = (int)(dragMouseX * scale);
                            int newDragY = (int)(dragMouseY * scale);
                            
                            setLocation(getX() + dragMouseX - newDragX, getY() + dragMouseY - newDragY);
                            
                            dragMouseX = newDragX;
                            dragMouseY = newDragY;
                            calculateBounds();
                        }
                        
                        int nx = getX() + e.getX() - dragMouseX;
                        int ny = getY() + e.getY() - dragMouseY;
                        setLocation(nx, ny);
                        updateOverlaps(); 
                    }
                }
            });
        }
        
        private boolean isOverBoard() {
            Point centerScreen = new Point(getWidth() / 2, getHeight() / 2);
            SwingUtilities.convertPointToScreen(centerScreen, this);
            Point boardScreen = boardPanel.getLocationOnScreen();
            return centerScreen.x >= boardScreen.x && centerScreen.x <= boardScreen.x + boardPanel.getWidth() &&
                   centerScreen.y >= boardScreen.y && centerScreen.y <= boardScreen.y + boardPanel.getHeight();
        }

        private void rotatePiece() {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            for (Point p : blocks) {
                int temp = p.x; p.x = -p.y; p.y = temp;
                if (p.x < minX) minX = p.x;
                if (p.y < minY) minY = p.y;
            }
            for (Point p : blocks) {
                p.x -= minX; p.y -= minY;
            }
            
            calculateBounds();
            
            if (isOnBoard && !isDragging) {
                int gridX = Math.round((float) (getX() - boardPanel.getX()) / BOARD_CELL_SIZE);
                int gridY = Math.round((float) (getY() - boardPanel.getY()) / BOARD_CELL_SIZE);
                setLocation(boardPanel.getX() + gridX * BOARD_CELL_SIZE, boardPanel.getY() + gridY * BOARD_CELL_SIZE);
            }
            updateOverlaps();
        }

        private void calculateBounds() {
            int cellSize = isOnBoard ? BOARD_CELL_SIZE : TRAY_CELL_SIZE;
            int maxX = 0, maxY = 0;
            for (Point p : blocks) {
                if (p.x > maxX) maxX = p.x;
                if (p.y > maxY) maxY = p.y;
            }
            setSize((maxX + 1) * cellSize + 2, (maxY + 1) * cellSize + 2);
        }

        @Override
        public boolean contains(int x, int y) {
            int cellSize = isOnBoard ? BOARD_CELL_SIZE : TRAY_CELL_SIZE;
            int gridX = x / cellSize;
            int gridY = y / cellSize;
            for (Point p : blocks) {
                if (p.x == gridX && p.y == gridY) return true;
            }
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int cellSize = isOnBoard ? BOARD_CELL_SIZE : TRAY_CELL_SIZE;
            
            g2d.setColor(ownerZone.currentColor);
            for (Point p : blocks) {
                int px = p.x * cellSize; int py = p.y * cellSize;
                g2d.fillRect(px, py, cellSize, cellSize);
            }
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2)); 
            for (Point p : blocks) {
                int px = p.x * cellSize; int py = p.y * cellSize;
                g2d.drawRect(px, py, cellSize, cellSize);
                
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.drawLine(px + 2, py + 2, px + cellSize - 3, py + 2);
                g2d.drawLine(px + 2, py + 2, px + 2, py + cellSize - 3);
                g2d.setColor(Color.BLACK); 
            }

            if (isOnBoard || (isDragging && isOverBoard())) {
                g2d.setFont(new Font("Arial", Font.BOLD, 22));
                FontMetrics fm = g2d.getFontMetrics();
                
                int gridX, gridY;
                if (isDragging) {
                    Point pLoc = SwingUtilities.convertPoint(getParent(), getLocation(), boardPanel);
                    gridX = Math.round((float) pLoc.x / BOARD_CELL_SIZE);
                    gridY = Math.round((float) pLoc.y / BOARD_CELL_SIZE);
                } else {
                    gridX = (getX() - boardPanel.getX()) / BOARD_CELL_SIZE;
                    gridY = (getY() - boardPanel.getY()) / BOARD_CELL_SIZE;
                }

                for (Point pt : blocks) {
                    int bx = gridX + pt.x;
                    int by = gridY + pt.y;
                    
                    boolean outOfBounds = (bx < 0 || bx >= BOARD_CELLS || by < 0 || by >= BOARD_CELLS);
                    boolean overlap = globalGridCounts.getOrDefault(new Point(bx, by), 0) > 1;

                    if (outOfBounds || overlap) {
                        int px = pt.x * BOARD_CELL_SIZE;
                        int py = pt.y * BOARD_CELL_SIZE;
                        g2d.setColor(Color.BLACK);
                        g2d.fillRect(px, py, BOARD_CELL_SIZE, BOARD_CELL_SIZE);
                        g2d.setColor(Color.RED);
                        int textWidth = fm.stringWidth("!");
                        g2d.drawString("!", px + (BOARD_CELL_SIZE - textWidth)/2, py + (BOARD_CELL_SIZE + fm.getAscent())/2 - 3);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BlokusSandbox().setVisible(true);
        });
    }
}