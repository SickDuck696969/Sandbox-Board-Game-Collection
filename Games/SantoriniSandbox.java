package Games;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SantoriniSandbox extends JFrame {

    private final int CELL_SIZE = 100;
    private final int BOARD_CELLS = 5;

    private final JPanel boardPanel;
    private final JLayeredPane layeredPane;
    private final JPanel workersPool;
    
    private final int[][] gridLevels = new int[BOARD_CELLS][BOARD_CELLS];
    
    private final List<GodPowerCard> godCards = new ArrayList<>();
    private final List<WorkerToken> workers = new ArrayList<>();

    public SantoriniSandbox() {
        setTitle("Santorini");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(240, 240, 240));

        // --- TOP: BẢNG ĐIỀU KHIỂN ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(new Color(100, 100, 100));
        
        JButton resetBtn = new JButton("RESET");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 18));
        resetBtn.setBackground(new Color(80, 80, 80));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> resetGame());
        topPanel.add(resetBtn);
        
        JLabel hintLabel = new JLabel("  |  Left-Drag: Move Worker  |  Right-Click: Build (+1)  |  Double Right-Click: Downgrade (-1)");
        hintLabel.setForeground(Color.WHITE);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        topPanel.add(hintLabel);
        add(topPanel, BorderLayout.NORTH);

        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // --- CENTER: BÀN CHƠI (BOARD) ---
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

        // --- SỰ KIỆN XÂY / ĐẬP NHÀ BẰNG CHUỘT PHẢI ---
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

        // --- SIDES: TẠO 4 KHUNG THẺ TRỐNG ---
        int cardW = 380; int cardH = 240;
        String[] pNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
        
        godCards.add(new GodPowerCard(pNames[0], 40, 60, cardW, cardH));
        godCards.add(new GodPowerCard(pNames[1], 1400 - cardW - 55, 60, cardW, cardH));
        godCards.add(new GodPowerCard(pNames[2], 40, 360, cardW, cardH));
        godCards.add(new GodPowerCard(pNames[3], 1400 - cardW - 55, 360, cardW, cardH));
        
        for (GodPowerCard gc : godCards) layeredPane.add(gc.container, JLayeredPane.DEFAULT_LAYER);

        // Phát ngẫu nhiên God Powers
        assignRandomGods();

        // --- BOTTOM: BỂ CHỨA WORKER ---
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
            WorkerToken w = new WorkerToken(wColors[i], wImages[i]); 
            workers.add(w);
            workersPool.add(w);
        }
    }

    // --- THUẬT TOÁN VẼ CÔNG TRÌNH ---
    private void drawBuildings(Graphics2D g2d, int r, int c, int x, int y) {
        int lvl = gridLevels[r][c];
        int cx = x + CELL_SIZE/2;
        int cy = y + CELL_SIZE/2;
        
        if (lvl >= 1) { // Tầng 1
            int s = 88; 
            g2d.setColor(new Color(160, 160, 160));
            g2d.fillRect(cx - s/2, cy - s/2, s, s);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(cx - s/2, cy - s/2, s, s);
        }
        if (lvl >= 2) { // Tầng 2
            int s = 72; 
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillRect(cx - s/2, cy - s/2, s, s);
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(cx - s/2, cy - s/2, s, s);
        }
        if (lvl >= 3) { // Tầng 3 (Kích thước 54, to hơn Worker để lộ viền)
            int s = 54; 
            g2d.setColor(new Color(230, 230, 230));
            g2d.fillOval(cx - s/2, cy - s/2, s, s);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawOval(cx - s/2, cy - s/2, s, s);
        }
        if (lvl == 4) { // DOME
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

    // --- GÁN GOD POWER NGẪU NHIÊN KHÔNG TRÙNG LẶP ---
    private void assignRandomGods() {
        List<GodPowerDatabase.GodPower> allGods = GodPowerDatabase.getGods();
        Collections.shuffle(allGods); 
        
        int maxCards = Math.min(godCards.size(), allGods.size());
        for (int i = 0; i < maxCards; i++) {
            godCards.get(i).setGodData(allGods.get(i));
        }
    }

    private void resetGame() {
        for (int r = 0; r < BOARD_CELLS; r++) {
            for (int c = 0; c < BOARD_CELLS; c++) gridLevels[r][c] = 0;
        }
        
        assignRandomGods();

        for (WorkerToken w : workers) {
            w.isOnBoard = false;
            w.gridCol = -1; w.gridRow = -1;
            layeredPane.remove(w);
            workersPool.add(w);
        }
        boardPanel.repaint();
        workersPool.revalidate();
        layeredPane.repaint();
    }

    // ==========================================
    // CLASS THẺ GOD POWER
    // ==========================================
    private class GodPowerCard {
        JPanel container;
        JTextField nameField;
        JPanel cardPanel;
        boolean isFaceUp = false;
        GodPowerDatabase.GodPower godData;
        Image characterImg;

        public GodPowerCard(String defaultPlayerName, int x, int y, int w, int h) {
            container = new JPanel(new BorderLayout(0, 5));
            container.setBounds(x, y, w, h + 35);
            container.setOpaque(false);

            nameField = new JTextField(defaultPlayerName);
            nameField.setFont(new Font("Arial", Font.BOLD, 16));
            nameField.setHorizontalAlignment(JTextField.CENTER);
            nameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            container.add(nameField, BorderLayout.NORTH);

            cardPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    if (!isFaceUp || godData == null) {
                        g2d.setColor(new Color(80, 85, 90));
                        g2d.fillRect(0, 0, w, h);
                        g2d.setColor(Color.WHITE);
                        g2d.setStroke(new BasicStroke(4));
                        g2d.drawRect(10, 10, w - 20, h - 20);
                        
                        g2d.setFont(new Font("Georgia", Font.BOLD, 30));
                        FontMetrics fm = g2d.getFontMetrics();
                        g2d.drawString("SANTORINI", (w - fm.stringWidth("SANTORINI"))/2, h/2 + 10);
                        
                    } else {
                        g2d.setColor(new Color(150, 150, 150)); 
                        g2d.fillRect(0, 0, w, h);
                        
                        int charW = (int)(w * 0.45);
                        int pad = 10;
                        int rightX = pad + charW + pad;
                        int rightW = w - rightX - pad;
                        
                        g2d.setColor(new Color(245, 235, 233)); // Nền màu bạn đã đổi
                        g2d.fillRect(pad, pad, charW, h - pad * 2);
                        
                        if (characterImg != null) {
                            double scale = Math.min((double)(charW-4)/characterImg.getWidth(null), (double)(h-pad*2-4)/characterImg.getHeight(null));
                            int sw = (int)(characterImg.getWidth(null)*scale);
                            int sh = (int)(characterImg.getHeight(null)*scale);
                            g2d.drawImage(characterImg, pad + (charW-sw)/2, pad + (h-pad*2-sh)/2, sw, sh, null);
                        } else {
                            // FIX LỖI TÀNG HÌNH: Đổi chữ dự phòng sang màu ĐEN
                            g2d.setColor(Color.BLACK); 
                            g2d.setFont(new Font("Arial", Font.BOLD, 12));
                            g2d.drawString("CHARACTER", pad + charW/2 - 35, h/2);
                        }
                        
                        // 2. Name Box
g2d.setColor(new Color(245, 235, 233));
g2d.fillRect(rightX, pad, rightW, 30);
g2d.setColor(Color.BLACK);

// --- THUẬT TOÁN TỰ ĐỘNG THU NHỎ FONT CHỮ ---
int maxNameWidth = rightW - 20; // Trừ hao lề trái 10px, phải 10px
int fontSize = 16;              // Cỡ chữ mặc định ban đầu
Font nameFont = new Font("Arial", Font.BOLD, fontSize);
FontMetrics nameFm = g2d.getFontMetrics(nameFont);

// Vòng lặp: Nếu chữ dài hơn khung và size chữ vẫn lớn hơn 9 thì tiếp tục thu nhỏ
while (nameFm.stringWidth(godData.name) > maxNameWidth && fontSize > 9) {
    fontSize--;
    nameFont = new Font("Arial", Font.BOLD, fontSize);
    nameFm = g2d.getFontMetrics(nameFont);
}

g2d.setFont(nameFont);
// Tự động căn giữa chữ theo chiều dọc của hộp (chiều cao hộp là 30)
int nameY = pad + ((30 - nameFm.getHeight()) / 2) + nameFm.getAscent(); 
g2d.drawString(godData.name, rightX + 10, nameY);
                        
                        g2d.setColor(new Color(245, 235, 233));
                        g2d.fillRect(rightX, pad + 30 + pad, rightW, 25);
                        g2d.setColor(Color.BLACK);
                        g2d.setFont(new Font("Arial", Font.ITALIC, 14));
                        g2d.drawString(godData.timing, rightX + 10, pad + 30 + pad + 18);
                        
                        g2d.setColor(new Color(245, 235, 233));
                        int powerY = pad + 30 + pad + 25 + pad;
                        g2d.fillRect(rightX, powerY, rightW, h - powerY - pad);
                        g2d.setColor(Color.BLACK);
                        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                        drawWrappedString(g2d, godData.power, rightX + 10, powerY + 20, rightW - 20);
                    }
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRect(0, 0, w-1, h-1);
                }
            };
            cardPanel.setPreferredSize(new Dimension(w, h));
            cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            cardPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        isFaceUp = !isFaceUp;
                        cardPanel.repaint();
                    }
                }
            });
            container.add(cardPanel, BorderLayout.CENTER);
        }

        public void setGodData(GodPowerDatabase.GodPower god) {
            this.godData = god;
            this.isFaceUp = false; 
            try {
                URL imgUrl = Thread.currentThread().getContextClassLoader().getResource(god.imagePath);
                if (imgUrl != null) this.characterImg = ImageIO.read(imgUrl);
                else this.characterImg = null;
            } catch (IOException ignored) {
                this.characterImg = null;
            }
            cardPanel.repaint();
        }
        
        private void drawWrappedString(Graphics2D g, String text, int x, int y, int width) {
            FontMetrics fm = g.getFontMetrics();
            String[] words = text.split(" ");
            String line = "";
            for (String word : words) {
                if (fm.stringWidth(line + word) < width) {
                    line += word + " ";
                } else {
                    g.drawString(line, x, y);
                    line = word + " ";
                    y += fm.getHeight() + 2;
                }
            }
            g.drawString(line, x, y);
        }
    }

    // ==========================================
    // CLASS WORKER TOKEN (HỖ TRỢ LOAD ẢNH)
    // ==========================================
    private class WorkerToken extends JComponent {
        Color color;
        Image workerImage;
        int dragMouseX, dragMouseY;
        boolean isDragging = false;
        boolean isOnBoard = false;
        int gridCol = -1, gridRow = -1;

        public WorkerToken(Color color, String imagePath) {
            this.color = color;
            
            try {
                URL imgUrl = Thread.currentThread().getContextClassLoader().getResource(imagePath);
                if (imgUrl != null) this.workerImage = ImageIO.read(imgUrl);
            } catch (IOException ignored) {}

            setPreferredSize(new Dimension(50, 50));
            setSize(50, 50);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isDragging = true;
                        dragMouseX = e.getX();
                        dragMouseY = e.getY();
                        
                        Point p = SwingUtilities.convertPoint(getParent(), getLocation(), layeredPane);
                        Container oldParent = getParent();
                        oldParent.remove(WorkerToken.this);
                        layeredPane.add(WorkerToken.this, JLayeredPane.DRAG_LAYER);
                        setLocation(p);
                        
                        layeredPane.repaint();
                        oldParent.repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!isDragging || !SwingUtilities.isLeftMouseButton(e)) return;
                    isDragging = false;

                    Point centerScreen = new Point(getWidth()/2, getHeight()/2);
                    SwingUtilities.convertPointToScreen(centerScreen, WorkerToken.this);
                    
                    Point boardScreen = boardPanel.getLocationOnScreen();
                    boolean dropOnBoard = centerScreen.x >= boardScreen.x && centerScreen.x <= boardScreen.x + boardPanel.getWidth() &&
                                          centerScreen.y >= boardScreen.y && centerScreen.y <= boardScreen.y + boardPanel.getHeight();

                    layeredPane.remove(WorkerToken.this);

                    if (dropOnBoard) {
                        Point p = SwingUtilities.convertPoint(layeredPane, getLocation(), boardPanel);
                        int col = (p.x + getWidth()/2) / CELL_SIZE;
                        int row = (p.y + getHeight()/2) / CELL_SIZE;
                        
                        int snapX = col * CELL_SIZE + (CELL_SIZE - getWidth()) / 2;
                        int snapY = row * CELL_SIZE + (CELL_SIZE - getHeight()) / 2;
                        
                        layeredPane.add(WorkerToken.this, JLayeredPane.PALETTE_LAYER);
                        setLocation(boardPanel.getX() + snapX, boardPanel.getY() + snapY);
                        
                        isOnBoard = true;
                        gridCol = col;
                        gridRow = row;
                    } else {
                        workersPool.add(WorkerToken.this);
                        isOnBoard = false;
                        gridCol = -1; gridRow = -1;
                    }
                    layeredPane.repaint();
                    workersPool.revalidate();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging && SwingUtilities.isLeftMouseButton(e)) {
                        setLocation(getX() + e.getX() - dragMouseX, getY() + e.getY() - dragMouseY);
                        layeredPane.repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (workerImage != null) {
                // Ưu tiên vẽ ảnh kích thước 40x40 ở vị trí canh giữa (cách mép 5px)
                g2d.drawImage(workerImage, 5, 5, 40, 40, null);
            } else {
                // KHÔNG CÓ ẢNH -> TỰ ĐỘNG VẼ HÌNH TAM GIÁC
                
                // 1. Tạo hình tam giác lớn (bao ngoài)
                Polygon triangle = new Polygon();
                triangle.addPoint(25, 5);  // Đỉnh trên cùng (Nằm giữa trục X)
                triangle.addPoint(5, 45);  // Đỉnh dưới cùng bên trái
                triangle.addPoint(45, 45); // Đỉnh dưới cùng bên phải
                
                g2d.setColor(color);
                g2d.fillPolygon(triangle);
                g2d.setColor(color.darker().darker());
                g2d.setStroke(new BasicStroke(3));
                g2d.drawPolygon(triangle);
                
                // 2. Tạo hình tam giác nhỏ màu trắng ở giữa (Tạo chi tiết giống đỉnh đầu)
                // Polygon innerTriangle = new Polygon();
                // innerTriangle.addPoint(25, 20); // Đỉnh trên
                // innerTriangle.addPoint(15, 38); // Dưới trái
                // innerTriangle.addPoint(35, 38); // Dưới phải
                
                // g2d.setColor(Color.WHITE);
                // g2d.fillPolygon(innerTriangle);
                // g2d.setColor(Color.BLACK);
                // g2d.setStroke(new BasicStroke(1));
                // g2d.drawPolygon(innerTriangle);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SantoriniSandbox().setVisible(true);
        });
    }
}