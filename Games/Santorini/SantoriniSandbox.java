package Santorini;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class SantoriniSandbox extends JFrame {

    private final int CELL_SIZE = 100;
    private final int BOARD_CELLS = 5;

    private final JPanel boardPanel;
    private final JLayeredPane layeredPane;
    private final JPanel workersPool;
    
    private final int[][] gridLevels = new int[BOARD_CELLS][BOARD_CELLS];
    
    public final List<GodPowerCard> godCards = new ArrayList<>();
    private final List<WorkerToken> workers = new ArrayList<>();

    public SantoriniSandbox() {
        setTitle("Santorini Sandbox - OOP Edition");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(240, 240, 240));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(new Color(100, 100, 100));
        
        JButton resetBtn = new JButton("RESET");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 18));
        resetBtn.setBackground(new Color(80, 80, 80));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> resetGame());
        topPanel.add(resetBtn);
        
        JLabel hintLabel = new JLabel("  |  Left-Drag: Move Worker  |  Right-Click: Build  |  L-Click Card: Flip  |  R-Click Card: Reroll");
        hintLabel.setForeground(Color.WHITE);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        topPanel.add(hintLabel);
        add(topPanel, BorderLayout.NORTH);

        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for (int r = 0; r < BOARD_CELLS; r++) {
                    for (int c = 0; c < BOARD_CELLS; c++) {
                        int x = c * CELL_SIZE;
                        int y = r * CELL_SIZE;
                        
                        if ((r + c) % 2 == 0) g2d.setColor(new Color(110, 190, 110)); 
                        else g2d.setColor(new Color(70, 150, 70)); 
                        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                        
                        drawBuildings(g2d, r, c, x, y);
                    }
                }
            }
            
            @Override
            public String getToolTipText(MouseEvent e) {
                int col = e.getX() / CELL_SIZE;
                int row = e.getY() / CELL_SIZE;
                if (col >= 0 && col < BOARD_CELLS && row >= 0 && row < BOARD_CELLS) {
                    int level = gridLevels[row][col];
                    if (level == 4) return "Dome (Max Level)";
                    return "Building Level: " + level;
                }
                return null;
            }
        };
        ToolTipManager.sharedInstance().registerComponent(boardPanel); 
        
        int boardPx = BOARD_CELLS * CELL_SIZE; 
        int boardX = (1400 - boardPx) / 2; 
        boardPanel.setBounds(boardX, 60, boardPx, boardPx);
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
        layeredPane.add(boardPanel, JLayeredPane.DEFAULT_LAYER);

        boardPanel.addMouseListener(new MouseAdapter() {
            private Timer doubleClickTimer;
            private int clickCol, clickRow;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int col = e.getX() / CELL_SIZE;
                    int row = e.getY() / CELL_SIZE;
                    if (col < 0 || col >= BOARD_CELLS || row < 0 || row >= BOARD_CELLS) return;
                    
                    if (hasWorkerAt(col, row)) return;

                    if (e.getClickCount() == 1) {
                        clickCol = col; clickRow = row;
                        doubleClickTimer = new Timer(250, evt -> {
                            if (gridLevels[clickRow][clickCol] < 4) {
                                gridLevels[clickRow][clickCol]++;
                                boardPanel.repaint();
                            }
                        });
                        doubleClickTimer.setRepeats(false);
                        doubleClickTimer.start();
                    } else if (e.getClickCount() == 2) {
                        if (doubleClickTimer != null) doubleClickTimer.stop(); 
                        if (gridLevels[row][col] > 0) {
                            gridLevels[row][col]--;
                            boardPanel.repaint();
                        }
                    }
                }
            }
        });

        int cardW = 380; int cardH = 240;
        String[] pNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
        
        // Khởi tạo card và truyền "this" (SantoriniSandbox) vào để card có thể gọi hàm reroll
        godCards.add(new GodPowerCard(pNames[0], 40, 60, cardW, cardH, this));
        godCards.add(new GodPowerCard(pNames[1], 1400 - cardW - 55, 60, cardW, cardH, this));
        godCards.add(new GodPowerCard(pNames[2], 40, 360, cardW, cardH, this));
        godCards.add(new GodPowerCard(pNames[3], 1400 - cardW - 55, 360, cardW, cardH, this));
        
        for (GodPowerCard gc : godCards) layeredPane.add(gc.container, JLayeredPane.DEFAULT_LAYER);

        assignRandomGods();

        workersPool = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        workersPool.setBounds(boardX - 100, boardPx + 80, boardPx + 200, 80);
        workersPool.setOpaque(false);
        layeredPane.add(workersPool, JLayeredPane.DEFAULT_LAYER);

        Color[] wColors = {new Color(50, 100, 200), new Color(50, 100, 200), 
                           new Color(220, 120, 40), new Color(220, 120, 40), 
                           new Color(180, 50, 200), new Color(180, 50, 200)};
                           
        String[] wImages = {
            "images/Santorini/worker_blue.png", "images/Santorini/worker_blue.png",
            "images/Santorini/worker_orange.png", "images/Santorini/worker_orange.png",
            "images/Santorini/worker_purple.png", "images/Santorini/worker_purple.png"
        };

        for (int i = 0; i < 6; i++) {
            // Truyền các biến môi trường vào cho WorkerToken
            WorkerToken w = new WorkerToken(wColors[i], wImages[i], layeredPane, boardPanel, workersPool, CELL_SIZE); 
            workers.add(w);
            workersPool.add(w);
        }
    }

    private void drawBuildings(Graphics2D g2d, int r, int c, int x, int y) {
        int lvl = gridLevels[r][c];
        int cx = x + CELL_SIZE/2;
        int cy = y + CELL_SIZE/2;
        
        if (lvl >= 1) { 
            int s = 88; 
            g2d.setColor(new Color(160, 160, 160));
            g2d.fillRect(cx - s/2, cy - s/2, s, s);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(cx - s/2, cy - s/2, s, s);
        }
        if (lvl >= 2) { 
            int s = 72; 
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillRect(cx - s/2, cy - s/2, s, s);
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(cx - s/2, cy - s/2, s, s);
        }
        if (lvl >= 3) { 
            int s = 54; 
            g2d.setColor(new Color(230, 230, 230));
            g2d.fillOval(cx - s/2, cy - s/2, s, s);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawOval(cx - s/2, cy - s/2, s, s);
        }
        if (lvl == 4) { 
            int s = 34;
            g2d.setColor(new Color(220, 40, 40));
            g2d.fillOval(cx - s/2, cy - s/2, s, s);
            g2d.setColor(new Color(150, 0, 0));
            g2d.drawOval(cx - s/2, cy - s/2, s, s);
            
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillOval(cx - s/4 - 3, cy - s/4 - 3, 10, 10); 
        }
    }

    private boolean hasWorkerAt(int col, int row) {
        for (WorkerToken w : workers) {
            if (w.isOnBoard && w.gridCol == col && w.gridRow == row) return true;
        }
        return false;
    }

    private void assignRandomGods() {
        List<GodPowerDatabase.GodPower> allGods = GodPowerDatabase.getGods();
        Collections.shuffle(allGods); 
        
        int maxCards = Math.min(godCards.size(), allGods.size());
        for (int i = 0; i < maxCards; i++) {
            godCards.get(i).setGodData(allGods.get(i));
        }
    }

    // --- HÀM MỚI: Reroll 1 thẻ bài ngẫu nhiên nhưng KHÔNG TRÙNG với các bài đang có trên sân ---
    public void rerollCard(GodPowerCard targetCard) {
        List<GodPowerDatabase.GodPower> allGods = GodPowerDatabase.getGods();
        List<String> activeNames = new ArrayList<>();
        
        // Quét lấy danh sách tên các nhân vật đang có trên màn hình
        for (GodPowerCard card : godCards) {
            if (card.godData != null) {
                activeNames.add(card.godData.name);
            }
        }

        // Lọc bỏ những vị thần đã xuất hiện
        allGods.removeIf(god -> activeNames.contains(god.name));

        // Nếu kho bài vẫn còn nhân vật mới, tiến hành xáo trộn và bốc 1 lá đè lên
        if (!allGods.isEmpty()) {
            Collections.shuffle(allGods);
            targetCard.setGodData(allGods.get(0));
        }
    }

    private void resetGame() {
        // 1. Xóa toàn bộ công trình trên bàn cờ
        for (int r = 0; r < BOARD_CELLS; r++) {
            for (int c = 0; c < BOARD_CELLS; c++) gridLevels[r][c] = 0;
        }
        
        // 2. Ép TẤT CẢ các thẻ bài úp xuống trước khi chia bài mới
        for (GodPowerCard card : godCards) {
            card.isFaceUp = false;
        }

        // 3. Xáo trộn và chia lại bài
        assignRandomGods();

        // 4. Thu hồi toàn bộ Worker về bể chứa
        for (WorkerToken w : workers) {
            w.isOnBoard = false;
            w.gridCol = -1; w.gridRow = -1;
            layeredPane.remove(w);
            workersPool.add(w);
        }
        
        // 5. Cập nhật lại toàn bộ giao diện
        boardPanel.repaint();
        workersPool.revalidate();
        layeredPane.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SantoriniSandbox().setVisible(true);
        });
    }
}