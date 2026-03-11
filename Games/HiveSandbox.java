package Games;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

public class HiveSandbox extends JFrame {

    private final JPanel playPanel; 
    private final JPanel trayPanel; 
    private final JScrollPane scrollPane;
    
    private final int HEX_RADIUS = 40; 
    private final int BOARD_VIRTUAL_SIZE = 2000; 
    
    private final String[] BUG_TYPES = {
        "QUEEN", "SPIDER", "SPIDER", 
        "BEETLE", "BEETLE", 
        "GRASSHOPPER", "GRASSHOPPER", "GRASSHOPPER", 
        "ANT", "ANT", "ANT"
    };

    private final Map<String, Image> bugImages = new HashMap<>();
    
    // Biến toàn cục để theo dõi menu đang mở
    private JPopupMenu activePopup = null;

    public HiveSandbox() {
        setTitle("Hive Sandbox - Perfect Snap & Stacking");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false); 

        loadImages();

        // --- TOP PANEL ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        topPanel.setBackground(new Color(40, 45, 52));
        
        JButton resetBtn = new JButton("Reset Game");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 14));
        resetBtn.addActionListener(e -> spawnAllBugs());
        topPanel.add(resetBtn);

        JButton viBtn = createLangButton("VI", new Color(200, 50, 50));
        viBtn.addActionListener(e -> showRulesVI());
        topPanel.add(viBtn);

        JButton enBtn = createLangButton("EN", new Color(50, 100, 200));
        enBtn.addActionListener(e -> showRulesEN());
        topPanel.add(enBtn);
        
        JLabel hintLabel = new JLabel("  |  Left-Drag: Pan Board  |  Right-Click Stack: View Bugs");
        hintLabel.setForeground(Color.LIGHT_GRAY);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        topPanel.add(hintLabel);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: SCROLLABLE PLAY AREA CÓ LƯỚI & PANNING ---
        playPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(50, 50, 50, 100)); 
                g2d.setStroke(new BasicStroke(1));

                // Tính toán lưới bằng số thực (Double) để độ chính xác đạt 100%
                double w = Math.sqrt(3) * HEX_RADIUS;
                double h = 2.0 * HEX_RADIUS;
                double vertDist = h * 0.75;

                int cols = (int)(BOARD_VIRTUAL_SIZE / w) + 2;
                int rows = (int)(BOARD_VIRTUAL_SIZE / vertDist) + 2;

                for (int row = 0; row < rows; row++) {
                    double offset = (row % 2 != 0) ? w / 2.0 : 0;
                    for (int col = 0; col < cols; col++) {
                        double cx = col * w + offset + w / 2.0;
                        double cy = row * vertDist + h / 2.0;
                        g2d.draw(createHexPath(cx, cy, HEX_RADIUS));
                    }
                }
            }
            // --- GIỮ NGUYÊN HÀM paintComponent CŨ Ở TRÊN ---

        @Override
        protected void paintChildren(Graphics g) {
            // 1. Lệnh này yêu cầu Java vẽ toàn bộ quân cờ như bình thường
            super.paintChildren(g); 
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 2. Đi dạo một vòng kiểm tra lại các quân cờ đang nằm trên Board
            for (Component c : getComponents()) {
                // Nếu tìm thấy Queen
                if (c instanceof HexToken token && token.bugName.equals("QUEEN")) {
                    
                    // 3. Kiểm tra xem Queen có đang bị Beetle trèo lên đầu không
                    boolean isTop = true;
                    for (Component other : getComponents()) {
                        if (other != token && other.getX() == token.getX() && other.getY() == token.getY()) {
                            // Nếu có quân khác cùng tọa độ nhưng Z-Order nhỏ hơn (nằm trên)
                            if (getComponentZOrder(other) < getComponentZOrder(token)) {
                                isTop = false;
                                break;
                            }
                        }
                    }
                    
                    // 4. Nếu Queen không bị ai đè, tiến hành tô lại viền vàng ĐÈ LÊN MỌI THỨ!
                    if (isTop) {
                        Graphics2D g2 = (Graphics2D) g2d.create();
                        g2.translate(token.getX(), token.getY()); // Dịch cọ vẽ tới đúng tọa độ của Queen
                        g2.setColor(new Color(255, 200, 0));      // Tô viền vàng
                        g2.setStroke(new BasicStroke(3));
                        g2.draw(token.hexPath);                   // Vẽ đè theo đường Path có sẵn
                        g2.dispose();
                    }
                }
            }
        }
        };
        playPanel.setLayout(null);
        playPanel.setBackground(new Color(230, 220, 200));
        playPanel.setPreferredSize(new Dimension(BOARD_VIRTUAL_SIZE, BOARD_VIRTUAL_SIZE));

        scrollPane = new JScrollPane(playPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 170, 150), 2));
        add(scrollPane, BorderLayout.CENTER);

        // --- TÍNH NĂNG KÉO THẢ MÀN HÌNH BÀN CHƠI (PANNING) ---
        MouseAdapter panner = new MouseAdapter() {
            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                // Đóng popup nếu đang click ra ngoài bàn chơi
                if (activePopup != null && activePopup.isVisible()) {
                    activePopup.setVisible(false);
                    activePopup = null;
                }
                
                if (SwingUtilities.isLeftMouseButton(e)) {
                    origin = e.getLocationOnScreen();
                    playPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                playPanel.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null && SwingUtilities.isLeftMouseButton(e)) {
                    Point current = e.getLocationOnScreen();
                    int deltaX = origin.x - current.x;
                    int deltaY = origin.y - current.y;
                    
                    JScrollBar hBar = scrollPane.getHorizontalScrollBar();
                    JScrollBar vBar = scrollPane.getVerticalScrollBar();
                    
                    hBar.setValue(hBar.getValue() + deltaX);
                    vBar.setValue(vBar.getValue() + deltaY);
                    
                    origin = current; 
                }
            }
        };
        playPanel.addMouseListener(panner);
        playPanel.addMouseMotionListener(panner);

        // --- SOUTH: FIXED TRAY AREA ---
        trayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        trayPanel.setLayout(null);
        trayPanel.setPreferredSize(new Dimension(1200, 220));
        trayPanel.setBackground(new Color(210, 200, 180));
        trayPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(150, 140, 120)));
        add(trayPanel, BorderLayout.SOUTH);

        spawnAllBugs();

        SwingUtilities.invokeLater(() -> {
            scrollPane.getHorizontalScrollBar().setValue((BOARD_VIRTUAL_SIZE - 1200) / 2);
            scrollPane.getVerticalScrollBar().setValue((BOARD_VIRTUAL_SIZE - 600) / 2);
        });
    }

    // THUẬT TOÁN TẠO ĐƯỜNG DẪN LỤC GIÁC (ĐỘ CHÍNH XÁC CAO)
    private Path2D.Double createHexPath(double cx, double cy, double radius) {
        Path2D.Double hex = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle_rad = Math.PI / 3.0 * i + Math.PI / 6.0;
            double px = cx + radius * Math.cos(angle_rad);
            double py = cy + radius * Math.sin(angle_rad);
            if (i == 0) hex.moveTo(px, py);
            else hex.lineTo(px, py);
        }
        hex.closePath();
        return hex;
    }

    // THUẬT TOÁN HÚT DÍNH (GRID SNAPPING BẰNG SỐ THỰC DOUBLE)
    private Point snapToGrid(int px, int py) {
        double w = Math.sqrt(3) * HEX_RADIUS;
        double h = 2.0 * HEX_RADIUS;
        double vertDist = h * 0.75;

        int row = (int) Math.round((py - h / 2.0) / vertDist);
        double offset = (row % 2 != 0) ? w / 2.0 : 0;
        int col = (int) Math.round((px - w / 2.0 - offset) / w);

        int[][] candidates = {
            {col, row}, {col+1, row}, {col-1, row},
            {col, row+1}, {col, row-1},
            {col+1, row+1}, {col-1, row+1},
            {col+1, row-1}, {col-1, row-1}
        };

        double bestCx = col * w + offset + w / 2.0;
        double bestCy = row * vertDist + h / 2.0;
        double minDist = Double.MAX_VALUE;

        for(int[] c : candidates) {
            double cOffset = (c[1] % 2 != 0) ? w / 2.0 : 0;
            double cx = c[0] * w + cOffset + w / 2.0;
            double cy = c[1] * vertDist + h / 2.0;
            
            double dist = Math.hypot(px - cx, py - cy);
            if(dist < minDist) {
                minDist = dist;
                bestCx = cx;
                bestCy = cy;
            }
        }
        return new Point((int)Math.round(bestCx), (int)Math.round(bestCy));
    }

    private JButton createLangButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void loadImages() {
        String[] uniqueBugs = {"QUEEN", "SPIDER", "BEETLE", "GRASSHOPPER", "ANT"};
        for (String bug : uniqueBugs) {
            try {
                String path = "/images/Hive/" + bug + ".png";
                java.net.URL imgUrl = getClass().getResource(path);
                if (imgUrl == null) {
                    path = "/images/Hive/" + bug + ".jpg";
                    imgUrl = getClass().getResource(path);
                }
                if (imgUrl != null) bugImages.put(bug, ImageIO.read(imgUrl));
            } catch (IOException e) {
                System.out.println("Error loading: " + bug);
            }
        }
    }

    private void spawnAllBugs() {
        playPanel.removeAll();
        trayPanel.removeAll();
        
        spawnPlayerBugs(Color.WHITE, Color.BLACK, 30, 20, "White", trayPanel);
        spawnPlayerBugs(Color.DARK_GRAY, Color.WHITE, 720, 20, "Black", trayPanel);
        
        trayPanel.repaint();
        playPanel.repaint();
    }

    private void spawnPlayerBugs(Color bg, Color fg, int x, int y, String team, JPanel p) {
        int xOff = 0; int yOff = 0; int count = 0;
        int hWidth = (int)Math.ceil(Math.sqrt(3) * HEX_RADIUS);
        int hHeight = 2 * HEX_RADIUS;

        for (String bugName : BUG_TYPES) {
            Image img = bugImages.get(bugName);
            HexToken token = new HexToken(bugName, bg, fg, img);
            token.setToolTipText(team + " - " + bugName);
            token.setLocation(x + xOff, y + yOff);
            p.add(token);

            count++;
            xOff += hWidth + 5;
            if (count >= 6) { count = 0; xOff = hWidth/2; yOff += hHeight + 5; }
        }
    }

    // ==========================================
    // CUSTOM HEXAGON TOKEN
    // ==========================================
    private class HexToken extends JComponent {
        private final String bugName;
        private final Color bgColor;
        private final Image bugImage; 
        private Path2D.Double hexPath;
        
        private int mouseX, mouseY;
        private boolean isDragging = false;
        
        // Căn lề để đảm bảo nét vẽ 3px không bị cắt (Clip)
        private final int TOKEN_PAD = 4;
        private final int T_WIDTH = (int)Math.ceil(Math.sqrt(3) * HEX_RADIUS) + TOKEN_PAD * 2;
        private final int T_HEIGHT = 2 * HEX_RADIUS + TOKEN_PAD * 2;
        private final int CENTER_X = T_WIDTH / 2;
        private final int CENTER_Y = T_HEIGHT / 2;

        public HexToken(String name, Color bg, Color fg, Image img) {
            this.bugName = name; this.bgColor = bg; this.bugImage = img;
            
            setSize(T_WIDTH, T_HEIGHT);
            hexPath = createHexPath(CENTER_X, CENTER_Y, HEX_RADIUS);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // XỬ LÝ CHUỘT PHẢI: BẬT/TẮT DANH SÁCH STACK
                    if (SwingUtilities.isRightMouseButton(e) && getParent() == playPanel) {
                        // Tính năng Đóng Panel nếu đang mở
                        if (activePopup != null && activePopup.isVisible()) {
                            activePopup.setVisible(false);
                            activePopup = null;
                            return; 
                        }
                        
                        List<HexToken> stack = new ArrayList<>();
                        for (Component c : playPanel.getComponents()) {
                            if (c instanceof HexToken && c.getX() == getX() && c.getY() == getY()) {
                                stack.add((HexToken) c);
                            }
                        }
                        
                        if (stack.size() > 1) {
                            stack.sort((a, b) -> Integer.compare(playPanel.getComponentZOrder(a), playPanel.getComponentZOrder(b)));
                            
                            JPopupMenu popup = new JPopupMenu();
                            for (HexToken t : stack) {
                                JMenuItem item = new JMenuItem(t.getToolTipText());
                                item.setFont(new Font("Arial", Font.BOLD, 14));
                                if (t.bugImage != null) {
                                    item.setIcon(new ImageIcon(t.bugImage.getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
                                }
                                popup.add(item);
                            }
                            activePopup = popup; // Ghi nhớ popup hiện tại
                            popup.show(HexToken.this, e.getX(), e.getY());
                        }
                        return;
                    }

                    // XỬ LÝ CHUỘT TRÁI: BẮT ĐẦU KÉO (LÊN DRAG_LAYER)
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (activePopup != null && activePopup.isVisible()) {
                            activePopup.setVisible(false);
                            activePopup = null;
                        }

                        isDragging = true;
                        mouseX = e.getX();
                        mouseY = e.getY();
                        
                        JLayeredPane layeredPane = getRootPane().getLayeredPane();
                        Point p = SwingUtilities.convertPoint(getParent(), getLocation(), layeredPane);
                        
                        Container oldParent = getParent();
                        oldParent.remove(HexToken.this);
                        layeredPane.add(HexToken.this, JLayeredPane.DRAG_LAYER);
                        setLocation(p);
                        
                        layeredPane.repaint();
                        oldParent.repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!isDragging || !SwingUtilities.isLeftMouseButton(e)) return;
                    isDragging = false;

                    JLayeredPane layeredPane = getRootPane().getLayeredPane();
                    Point centerScreen = new Point(getWidth() / 2, getHeight() / 2);
                    SwingUtilities.convertPointToScreen(centerScreen, HexToken.this);
                    
                    Point trayScreen;
                    try { trayScreen = trayPanel.getLocationOnScreen(); } 
                    catch (Exception ex) { return; }
                    
                    layeredPane.remove(HexToken.this);

                    if (centerScreen.y >= trayScreen.y) {
                        Point p = SwingUtilities.convertPoint(layeredPane, getLocation(), trayPanel);
                        trayPanel.add(HexToken.this);
                        setLocation(p);
                        trayPanel.setComponentZOrder(HexToken.this, 0);
                    } else {
                        // Tính toán Snapping chính xác với Offset của viền
                        Point p = SwingUtilities.convertPoint(layeredPane, getLocation(), playPanel);
                        int centerX = p.x + CENTER_X;
                        int centerY = p.y + CENTER_Y;
                        Point snapped = snapToGrid(centerX, centerY);
                        
                        playPanel.add(HexToken.this);
                        setLocation(snapped.x - CENTER_X, snapped.y - CENTER_Y);
                        playPanel.setComponentZOrder(HexToken.this, 0);
                    }
                    
                    playPanel.repaint();
                    trayPanel.repaint();
                    layeredPane.repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging && SwingUtilities.isLeftMouseButton(e)) {
                        int nx = getX() + e.getX() - mouseX;
                        int ny = getY() + e.getY() - mouseY;
                        setLocation(nx, ny);
                        getParent().repaint();
                    }
                }
            });
        }

        @Override
        public boolean contains(int x, int y) { 
            return hexPath.contains(x, y); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(bgColor);
            g2d.fill(hexPath);

            if (bugImage != null) {
                int iw = (int)(HEX_RADIUS * 1.3);
                int ih = (int)(HEX_RADIUS * 1.3);
                double s = Math.min((double)iw/bugImage.getWidth(null), (double)ih/bugImage.getHeight(null));
                int sw = (int)(bugImage.getWidth(null)*s);
                int sh = (int)(bugImage.getHeight(null)*s);
                g2d.drawImage(bugImage, CENTER_X - sw/2, CENTER_Y - sh/2, sw, sh, null);
            }

            // OUTLINE ĐỒNG NHẤT KHÔNG BỊ CẮT XÉN
            if (bugName.equals("QUEEN")) {
                g2d.setColor(new Color(255, 200, 0)); 
            } else {
                g2d.setColor(Color.BLACK); 
            }
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(hexPath);

            // STACK INDICATOR
            if (getParent() == playPanel) {
                int stackCount = 0;
                boolean isTop = true;
                
                for (Component c : playPanel.getComponents()) {
                    if (c instanceof HexToken && c.getX() == getX() && c.getY() == getY()) {
                        stackCount++;
                        if (playPanel.getComponentZOrder(c) < playPanel.getComponentZOrder(this)) {
                            isTop = false;
                        }
                    }
                }
                
                if (isTop && stackCount > 1) {
                    String text = String.valueOf(stackCount - 1);
                    g2d.setFont(new Font("Arial", Font.BOLD, 22));
                    FontMetrics fm = g2d.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(text)) / 2;
                    int ty = fm.getAscent() + 10; 
                    
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(text, tx - 1, ty - 1);
                    g2d.drawString(text, tx + 1, ty + 1);
                    g2d.drawString(text, tx - 1, ty + 1);
                    g2d.drawString(text, tx + 1, ty - 1);
                    
                    g2d.setColor(Color.RED);
                    g2d.drawString(text, tx, ty);
                }
            }
        }
    }

    private void showRulesVI() {
        String rules = """
                TÓM TẮT LUẬT CHƠI HIVE
                
                1. MỤC TIÊU: Bao vây Queen của đối phương.
                2. QUEEN: Phải đặt xuống trong 4 lượt đầu.
                3. DI CHUYỂN:
                   - Queen: 1 bước.
                   - Beetle: 1 bước (CÓ THỂ TRÈO LÊN ĐẦU QUÂN KHÁC).
                   - Grasshopper: Nhảy thẳng qua các quân khác.
                   - Spider: Đúng 3 bước ven tổ.
                   - Ant: Di chuyển vô hạn ven tổ.
                4. LUẬT MỘT TỔ: Tổ ong không được đứt đoạn.""";
        JOptionPane.showMessageDialog(this, rules, "Luật Chơi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRulesEN() {
        String rules = """
                HIVE RULES SUMMARY
                
                1. GOAL: Surround the opponent's Queen.
                2. QUEEN: Must be placed within the first 4 turns.
                3. MOVEMENT:
                   - Queen: 1 step.
                   - Beetle: 1 step (CAN CLIMB ON TOP OF OTHERS).
                   - Grasshopper: Jumps over others in a line.
                   - Spider: Exactly 3 steps around edge.
                   - Ant: Anywhere around the edge.
                4. ONE HIVE RULE: The hive must never be broken.""";
        JOptionPane.showMessageDialog(this, rules, "Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HiveSandbox().setVisible(true);
        });
    }
}