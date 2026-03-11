package Games;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

public class HiveSandbox extends JFrame {

    private final JPanel playPanel; // Vùng chơi có thể cuộn
    private final JPanel trayPanel; // Khay chứa cố định
    private final JScrollPane scrollPane;
    
    private final int HEX_RADIUS = 40; 
    private final int BOARD_VIRTUAL_SIZE = 2000; // Kích thước bàn chơi ảo
    
    private final String[] BUG_TYPES = {
        "QUEEN", "SPIDER", "SPIDER", 
        "BEETLE", "BEETLE", 
        "GRASSHOPPER", "GRASSHOPPER", "GRASSHOPPER", 
        "ANT", "ANT", "ANT"
    };

    private final Map<String, Image> bugImages = new HashMap<>();

    public HiveSandbox() {
        setTitle("Hive Sandbox");
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
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: SCROLLABLE PLAY AREA ---
        playPanel = new JPanel();
        playPanel.setLayout(null);
        playPanel.setBackground(new Color(230, 220, 200));
        playPanel.setPreferredSize(new Dimension(BOARD_VIRTUAL_SIZE, BOARD_VIRTUAL_SIZE));

        scrollPane = new JScrollPane(playPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 170, 150), 2));
        add(scrollPane, BorderLayout.CENTER);

        // --- SOUTH: FIXED TRAY AREA ---
        trayPanel = new JPanel();
        trayPanel.setLayout(null);
        trayPanel.setPreferredSize(new Dimension(1200, 220));
        trayPanel.setBackground(new Color(210, 200, 180));
        trayPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(150, 140, 120)));
        add(trayPanel, BorderLayout.SOUTH);

        spawnAllBugs();

        // Cuộn vào giữa bàn chơi khi bắt đầu
        SwingUtilities.invokeLater(() -> {
            scrollPane.getHorizontalScrollBar().setValue((BOARD_VIRTUAL_SIZE - 1200) / 2);
            scrollPane.getVerticalScrollBar().setValue((BOARD_VIRTUAL_SIZE - 600) / 2);
        });
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
        
        // Đặt quân vào khay cố định (tọa độ tương đối với trayPanel)
        spawnPlayerBugs(Color.WHITE, Color.BLACK, 30, 20, "White", trayPanel);
        spawnPlayerBugs(Color.DARK_GRAY, Color.WHITE, 720, 20, "Black", trayPanel);
        
        trayPanel.repaint();
        playPanel.repaint();
    }

    private void spawnPlayerBugs(Color bg, Color fg, int x, int y, String team, JPanel p) {
        int xOff = 0; int yOff = 0; int count = 0;
        int hWidth = (int)(Math.sqrt(3) * HEX_RADIUS);
        int hHeight = 2 * HEX_RADIUS;

        for (String bugName : BUG_TYPES) {
            Image img = bugImages.get(bugName);
            HexToken token = new HexToken(bugName, bg, fg, img);
            
            // Fix: Sử dụng biến team làm Tooltip
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
        private final Color bgColor, fgColor;
        private final Image bugImage; 
        private Polygon hexPolygon;
        private int mouseX, mouseY; // Đã đồng bộ tên biến

        public HexToken(String name, Color bg, Color fg, Image img) {
            this.bugName = name; this.bgColor = bg; this.fgColor = fg; this.bugImage = img;
            setSize((int)(Math.sqrt(3) * HEX_RADIUS), 2 * HEX_RADIUS);
            createHexagon();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                    
                    // NÂNG CẤP: Đưa quân cờ lên Lớp Nổi (LayeredPane) cao nhất của cửa sổ
                    JLayeredPane layeredPane = getRootPane().getLayeredPane();
                    
                    // Tính toán quy đổi tọa độ từ nơi đang đứng sang tọa độ của Lớp Nổi
                    Point p = SwingUtilities.convertPoint(getParent(), getLocation(), layeredPane);
                    
                    Container oldParent = getParent();
                    oldParent.remove(HexToken.this);
                    
                    // Thêm vào lớp DRAG_LAYER để quân cờ luôn bay phía trên mọi thứ
                    layeredPane.add(HexToken.this, JLayeredPane.DRAG_LAYER);
                    setLocation(p);
                    
                    layeredPane.repaint();
                    oldParent.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    JLayeredPane layeredPane = getRootPane().getLayeredPane();
                    
                    // Xác định tọa độ Màn Hình của tâm quân cờ để xem nó đang bay ở khu vực nào
                    Point centerScreen = new Point(getWidth() / 2, getHeight() / 2);
                    SwingUtilities.convertPointToScreen(centerScreen, HexToken.this);
                    
                    Point trayScreen;
                    try {
                        trayScreen = trayPanel.getLocationOnScreen();
                    } catch (Exception ex) { return; }
                    
                    if (centerScreen.y >= trayScreen.y) {
                        // --- NẾU THẢ VÀO KHAY CHỨA (TRAY) ---
                        Point p = SwingUtilities.convertPoint(layeredPane, getLocation(), trayPanel);
                        layeredPane.remove(HexToken.this);
                        trayPanel.add(HexToken.this);
                        setLocation(p);
                        trayPanel.setComponentZOrder(HexToken.this, 0); // Đảm bảo nổi lên trên các quân khác ở khay
                    } else {
                        // --- NẾU THẢ VÀO BÀN CHƠI (BOARD) ---
                        Point p = SwingUtilities.convertPoint(layeredPane, getLocation(), playPanel);
                        
                        // Khóa không cho rơi ra ngoài vũ trụ 2000x2000
                        if (p.x < 0) p.x = 0;
                        if (p.y < 0) p.y = 0;
                        if (p.x > playPanel.getWidth() - getWidth()) p.x = playPanel.getWidth() - getWidth();
                        if (p.y > playPanel.getHeight() - getHeight()) p.y = playPanel.getHeight() - getHeight();

                        layeredPane.remove(HexToken.this);
                        playPanel.add(HexToken.this);
                        setLocation(p);
                        playPanel.setComponentZOrder(HexToken.this, 0); // Đảm bảo đè lên các quân cờ khác (Luật bọ hung)
                    }
                    
                    // Cập nhật lại giao diện
                    trayPanel.repaint();
                    playPanel.repaint();
                    layeredPane.repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // Do đang ở trên lớp Nổi, ta cứ việc di chuyển thoải mái không sợ bị khuất
                    int nx = getX() + e.getX() - mouseX;
                    int ny = getY() + e.getY() - mouseY;
                    setLocation(nx, ny);
                    getParent().repaint();
                }
            });
        }

        private void createHexagon() {
            hexPolygon = new Polygon();
            int cx = getWidth() / 2; int cy = getHeight() / 2;
            for (int i = 0; i < 6; i++) {
                int x = (int)(cx + HEX_RADIUS * Math.cos(i * Math.PI / 3 + Math.PI / 6));
                int y = (int)(cy + HEX_RADIUS * Math.sin(i * Math.PI / 3 + Math.PI / 6));
                hexPolygon.addPoint(x, y);
            }
        }

        @Override
        public boolean contains(int x, int y) { return hexPolygon.contains(x, y); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // HIGHLIGHT CHO QUEEN
            if (bugName.equals("QUEEN")) {
                // Hiệu ứng hào quang (Glow)
                g2d.setColor(new Color(255, 215, 0, 80));
                g2d.fillOval(0, 0, getWidth(), getHeight());
            }

            g2d.setColor(bgColor);
            g2d.fillPolygon(hexPolygon);

            if (bugImage != null) {
                int iw = (int)(getWidth()*0.65), ih = (int)(getHeight()*0.65);
                double s = Math.min((double)iw/bugImage.getWidth(null), (double)ih/bugImage.getHeight(null));
                int sw = (int)(bugImage.getWidth(null)*s), sh = (int)(bugImage.getHeight(null)*s);
                g2d.drawImage(bugImage, (getWidth()-sw)/2, (getHeight()-sh)/2, sw, sh, null);
            }

            // VIỀN CHO QUEEN
            if (bugName.equals("QUEEN")) {
                g2d.setColor(new Color(255, 165, 0)); // Màu Cam Vàng rực rỡ
                g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawPolygon(hexPolygon);
                // Thêm viền phụ bên trong để tạo độ sâu
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(Color.WHITE);
                g2d.drawPolygon(hexPolygon);
            } else {
                g2d.setColor(fgColor);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(hexPolygon);
            }
        }
    }

    // --- RULES LOGIC (VI / EN) ---
    private void showRulesVI() {
        String rules = """
                TÓM TẮT LUẬT CHƠI HIVE
                
                1. MỤC TIÊU: Bao vây Queen của đối phương.
                2. QUEEN: Cực kỳ quan trọng. Phải đặt xuống trong 4 lượt đầu.
                3. DI CHUYỂN:
                   - Queen: 1 bước.
                   - Beetle: 1 bước (có thể đè quân khác).
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
                   - Beetle: 1 step (can climb on top).
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