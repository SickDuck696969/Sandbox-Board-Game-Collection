import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

public class HiveSandbox extends JFrame {

    private final JPanel boardPanel;
    
    // Kích thước của 1 quân cờ lục giác
    private final int HEX_RADIUS = 40; 
    
    private final String[] BUG_TYPES = {
        "QUEEN", "SPIDER", "SPIDER", 
        "BEETLE", "BEETLE", 
        "GRASSHOPPER", "GRASSHOPPER", "GRASSHOPPER", 
        "ANT", "ANT", "ANT"
    };

    // Bộ nhớ đệm lưu trữ hình ảnh
    private final Map<String, Image> bugImages = new HashMap<>();

    public HiveSandbox() {
        setTitle("Hive");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false); 

        // Load toàn bộ ảnh vào RAM trước khi vẽ game
        loadImages();

        // --- TOP: BẢNG ĐIỀU KHIỂN ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        topPanel.setBackground(new Color(40, 45, 52));
        
        JButton resetBtn = new JButton("Reset Game");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 14));
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> spawnAllBugs());
        topPanel.add(resetBtn);

        // Nút Tiếng Việt
        JButton viBtn = new JButton("VI");
        viBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viBtn.setBackground(new Color(200, 50, 50));
        viBtn.setForeground(Color.RED);
        viBtn.setFocusPainted(false);
        viBtn.setToolTipText("Luật chơi (Tiếng Việt)");
        viBtn.addActionListener(e -> showRulesVI());
        topPanel.add(viBtn);

        // Nút Tiếng Anh
        JButton enBtn = new JButton("EN");
        enBtn.setFont(new Font("Arial", Font.BOLD, 12));
        enBtn.setBackground(new Color(50, 100, 200));
        enBtn.setForeground(Color.RED);
        enBtn.setFocusPainted(false);
        enBtn.setToolTipText("Game Rules (English)");
        enBtn.addActionListener(e -> showRulesEN());
        topPanel.add(enBtn);
        
        JLabel hintLabel = new JLabel("  |  Drag bugs from the bottom tray to the board!");
        hintLabel.setForeground(Color.LIGHT_GRAY);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        topPanel.add(hintLabel);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: BÀN CHƠI & KHAY CHỨA ---
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(180, 170, 150));
                g.drawLine(0, 530, getWidth(), 530);
                g.drawLine(0, 531, getWidth(), 531); 
                
                g.setColor(new Color(210, 200, 180));
                g.fillRect(0, 532, getWidth(), getHeight() - 532);
            }
        };
        boardPanel.setLayout(null); 
        boardPanel.setBackground(new Color(230, 220, 200)); 
        add(boardPanel, BorderLayout.CENTER);

        spawnAllBugs();
    }

    // Tải hình ảnh từ thư mục "images"
    private void loadImages() {
        String[] uniqueBugs = {"QUEEN", "SPIDER", "BEETLE", "GRASSHOPPER", "ANT"};
        for (String bug : uniqueBugs) {
            try {
                File file = new File("images/Hive/" + bug + ".png"); 
                if (!file.exists()) {
                    file = new File("images/Hive/" + bug + ".jpg"); 
                }
                if (file.exists()) {
                    bugImages.put(bug, ImageIO.read(file));
                }
            } catch (IOException e) { 
                System.out.println("Could not load image for: " + bug); 
            }
        }
    }

    private void spawnAllBugs() {
        boardPanel.removeAll();
        int startY = 540; 
        spawnPlayerBugs(Color.WHITE, Color.BLACK, 30, startY, "White Player");
        spawnPlayerBugs(Color.DARK_GRAY, Color.WHITE, 720, startY, "Black Player");
        boardPanel.repaint();
    }

    private void spawnPlayerBugs(Color bgColor, Color fgColor, int startX, int startY, String tooltip) {
        int xOffset = 0;
        int yOffset = 0;
        int colCount = 0;

        int hexWidth = (int) (Math.sqrt(3) * HEX_RADIUS);
        int hexHeight = 2 * HEX_RADIUS;

        for (String bugName : BUG_TYPES) {
            Image img = bugImages.get(bugName);
            HexToken bug = new HexToken(bugName, bgColor, fgColor, img);
            bug.setToolTipText(tooltip + " - " + bugName);
            
            bug.setLocation(startX + xOffset, startY + yOffset);
            boardPanel.add(bug);

            colCount++;
            xOffset += hexWidth + 5; 
            
            if (colCount >= 6) { 
                colCount = 0;
                xOffset = hexWidth / 2; 
                yOffset += hexHeight + 5; 
            }
        }
    }

    // ==========================================
    // LOGIC HIỂN THỊ LUẬT CHƠI (VI / EN)
    // ==========================================
    private void showRulesVI() {
        String rules = """
                TÓM TẮT LUẬT CHƠI HIVE
                
                1. MỤC TIÊU:
                   - Bao vây hoàn toàn Ong Chúa (Queen) của đối phương bằng bất kỳ quân nào (kể cả quân của mình hay địch).
                
                2. CÁCH ĐẶT QUÂN:
                   - Lượt đầu tiên: Hai bên đặt 1 quân bất kỳ chạm nhau.
                   - Từ lượt 2 trở đi: Quân mới đặt xuống chỉ được chạm quân phe mình, KHÔNG được chạm quân đối phương.
                
                3. CÁCH DI CHUYỂN:
                   - Bạn CHỈ ĐƯỢC DI CHUYỂN sau khi đã đặt Ong Chúa (bắt buộc đặt Ong Chúa trong 4 lượt đầu).
                   - Luật Một Tổ (One Hive): Trong quá trình di chuyển, tổ ong không bao giờ được đứt đoạn thành 2 phần.
                
                4. KHẢ NĂNG CỦA CÁC LOÀI:
                   - ONG CHÚA (Queen): Chỉ bò 1 bước.
                   - BỌ HUNG (Beetle): Bò 1 bước, HOẶC trèo lên đầu các quân khác (quân bị đè không thể di chuyển).
                   - CHÂU CHẤU (Grasshopper): Nhảy qua đầu các quân khác theo một đường thẳng đến ô trống đầu tiên.
                   - NHỆN (Spider): Bò ven theo tổ CHÍNH XÁC 3 bước (không được đi ngược lại).
                   - KIẾN (Ant): Bò ven theo tổ đến BẤT KỲ ô trống nào.""";
        
        JOptionPane.showMessageDialog(this, rules, "Luật Chơi - Hive", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRulesEN() {
        String rules = """
                       HIVE - QUICK RULES SUMMARY
                       
                       1. GOAL:
                          - Completely surround the opponent's Queen Bee with any bugs (yours or theirs).
                       
                       2. PLACEMENT:
                          - Turn 1: Players place one bug touching each other.
                          - After Turn 1: New bugs must ONLY touch your own color. They cannot touch opponent's bugs.
                       
                       3. MOVEMENT:
                          - You CANNOT move any bugs until your Queen is placed (must be placed within your first 4 turns).
                          - ONE HIVE RULE: The hive must never be broken into two separate parts during or after a move.
                       
                       4. BUG ABILITIES:
                          - QUEEN: Moves exactly 1 space.
                          - BEETLE: Moves 1 space OR climbs on top of the hive (bugs underneath cannot move).
                          - GRASSHOPPER: Jumps over other bugs in a straight line to the next empty space.
                          - SPIDER: Moves EXACTLY 3 spaces around the outside edge (no backtracking).
                          - ANT: Moves anywhere around the outside edge of the hive.""";
        
        JOptionPane.showMessageDialog(this, rules, "Game Rules - Hive", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    // CUSTOM HEXAGON COMPONENT
    // ==========================================
    private class HexToken extends JComponent {
        private final String bugName;
        private final Color bgColor;
        private final Color fgColor;
        private final Image bugImage; 
        private Polygon hexPolygon;
        
        private int mouseClickX, mouseClickY;

        public HexToken(String bugName, Color bgColor, Color fgColor, Image bugImage) {
            this.bugName = bugName;
            this.bgColor = bgColor;
            this.fgColor = fgColor;
            this.bugImage = bugImage;

            int width = (int) (Math.sqrt(3) * HEX_RADIUS);
            int height = 2 * HEX_RADIUS;
            setSize(width, height);
            
            createHexagon();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    mouseClickX = e.getX();
                    mouseClickY = e.getY();
                    getParent().setComponentZOrder(HexToken.this, 0);
                    getParent().repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int newX = getX() + e.getX() - mouseClickX;
                    int newY = getY() + e.getY() - mouseClickY;
                    
                    if (newX < 0) newX = 0;
                    if (newY < 0) newY = 0;
                    if (newX > getParent().getWidth() - getWidth()) newX = getParent().getWidth() - getWidth();
                    if (newY > getParent().getHeight() - getHeight()) newY = getParent().getHeight() - getHeight();

                    setLocation(newX, newY);
                }
            });
        }

        private void createHexagon() {
            hexPolygon = new Polygon();
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            for (int i = 0; i < 6; i++) {
                int xVal = (int) (centerX + HEX_RADIUS * Math.cos(i * Math.PI / 3 + Math.PI / 6));
                int yVal = (int) (centerY + HEX_RADIUS * Math.sin(i * Math.PI / 3 + Math.PI / 6));
                hexPolygon.addPoint(xVal, yVal);
            }
        }

        @Override
        public boolean contains(int x, int y) {
            return hexPolygon.contains(x, y);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2d.setColor(bgColor);
            g2d.fillPolygon(hexPolygon);

            if (bugImage != null) {
                int maxWidth = (int)(getWidth() * 0.65);
                int maxHeight = (int)(getHeight() * 0.65);

                int imgWidth = bugImage.getWidth(null);
                int imgHeight = bugImage.getHeight(null);
                
                double scale = Math.min((double)maxWidth / imgWidth, (double)maxHeight / imgHeight); 
                int scaledWidth = (int)(imgWidth * scale);
                int scaledHeight = (int)(imgHeight * scale);

                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;

                g2d.drawImage(bugImage, x, y, scaledWidth, scaledHeight, this);
            } else {
                g2d.setColor(fgColor);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(bugName);
                int textHeight = fm.getAscent();
                g2d.drawString(bugName, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 2);
            }

            if (bugName.equals("QUEEN")) {
                g2d.setColor(new Color(255, 200, 0)); 
                g2d.setStroke(new BasicStroke(4));
            } else {
                g2d.setColor(fgColor); 
                g2d.setStroke(new BasicStroke(2));
            }
            g2d.drawPolygon(hexPolygon);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}
            HiveSandbox game = new HiveSandbox();
            game.setLocationRelativeTo(null);
            game.setVisible(true);
        });
    }
}