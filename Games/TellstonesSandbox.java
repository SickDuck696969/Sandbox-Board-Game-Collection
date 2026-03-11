package Games;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class TellstonesSandbox extends JFrame {

    private final String[] STONE_TYPES = {"crown", "shield", "sword", "flag", "knight", "hammer", "scales"};
    private List<Stone> poolStones = new ArrayList<>();
    private List<Stone> lineStones = new ArrayList<>();
    
    private JPanel linePanel;
    private JPanel poolPanel;
    
    private JPanel glassPane;
    private ScalableStoneButton dragLabel; 
    private Stone draggedStone = null;
    
    private Stone selectedSwapStone = null; 

    private Map<String, Image> stoneImages = new HashMap<>();
    private Image hiddenImage;

    private JLabel p1ScoreLabel = new JLabel("0");
    private JLabel p2ScoreLabel = new JLabel("0");

    public TellstonesSandbox() {
        setTitle("Tellstones");
        setSize(1300, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        loadOriginalImages();

        for (String type : STONE_TYPES) {
            poolStones.add(new Stone(type, true));
        }

        // --- TOP: SCOREBOARD & RESET ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(25, 30, 36)); // Màu nền thanh công cụ tối hiện đại
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        topPanel.add(createPlayerScorePanel("Player 1", p1ScoreLabel), BorderLayout.WEST);
        
        JButton resetBtn = new JButton("Reset Game");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resetBtn.setBackground(new Color(200, 60, 60));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> resetGameState());
        
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.setOpaque(false);
        centerWrapper.add(resetBtn);
        topPanel.add(centerWrapper, BorderLayout.CENTER);
        
        topPanel.add(createPlayerScorePanel("Player 2", p2ScoreLabel), BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: THE LINE (THẢM CHƠI MÀU XANH) ---
        linePanel = new JPanel();
        linePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 50));
        linePanel.setBackground(new Color(38, 89, 53)); // Thảm nhung xanh lá chuẩn Casino/Boardgame
        linePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 100), 2),
                "The Line (Left-Click two stones to SWAP | Drag to gaps to INSERT)",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), new Color(200, 230, 200)));

        JScrollPane scrollPane = new JScrollPane(linePanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: THE POOL (KHAY GỖ ĐEN) ---
        poolPanel = new JPanel();
        poolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));
        poolPanel.setBackground(new Color(40, 42, 45)); // Khay đen nhám
        poolPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, new Color(70, 75, 80)),
                "The Pool (Drag to move | Right-Click to Flip)",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), Color.LIGHT_GRAY));
        poolPanel.setPreferredSize(new Dimension(1000, 220));
        add(poolPanel, BorderLayout.SOUTH);

        // --- GLASS PANE ---
        glassPane = new JPanel(null);
        glassPane.setOpaque(false);
        setGlassPane(glassPane);
        
        dragLabel = new ScalableStoneButton();
        dragLabel.setStone(new Stone("DragIcon", false)); 
        dragLabel.setVisible(false);
        dragLabel.setFocusable(false);
        glassPane.add(dragLabel);

        updateUIState();
    }

    private void resetGameState() {
        lineStones.clear();
        poolStones.clear();
        for (String type : STONE_TYPES) {
            poolStones.add(new Stone(type, true));
        }
        selectedSwapStone = null;
        draggedStone = null;
        p1ScoreLabel.setText("0");
        p2ScoreLabel.setText("0");
        updateUIState();
    }

    private JPanel createPlayerScorePanel(String playerName, JLabel scoreLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setOpaque(false);
        JLabel nameLabel = new JLabel(playerName + ": ");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        scoreLabel.setForeground(new Color(100, 200, 255)); // Số điểm màu xanh Cyan sáng
        
        JButton minusBtn = new JButton("-");
        minusBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        minusBtn.setFocusPainted(false);
        minusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        minusBtn.addActionListener(e -> {
            int score = Integer.parseInt(scoreLabel.getText());
            if (score > 0) scoreLabel.setText(String.valueOf(score - 1));
        });
        
        JButton plusBtn = new JButton("+");
        plusBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        plusBtn.setFocusPainted(false);
        plusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        plusBtn.addActionListener(e -> {
            scoreLabel.setText(String.valueOf(Integer.parseInt(scoreLabel.getText()) + 1));
        });
        
        panel.add(nameLabel);
        panel.add(minusBtn);
        panel.add(scoreLabel);
        panel.add(plusBtn);
        return panel;
    }

    private void loadOriginalImages() {
        for (String type : STONE_TYPES) {
            stoneImages.put(type, loadRawImage("images/Tellstones/" + type + ".png"));
        }
        hiddenImage = loadRawImage("images/Tellstones/Hidden.png");
    }

    private BufferedImage loadRawImage(String path) {
        try {
            URL imgUrl = Thread.currentThread().getContextClassLoader().getResource(path);
            if (imgUrl != null) {
                return ImageIO.read(imgUrl);
            } else {
                System.err.println("Resource not found: " + path);
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void updateUIState() {
        linePanel.removeAll();
        poolPanel.removeAll();

        for (Stone stone : lineStones) linePanel.add(createStoneButton(stone, "LINE"));
        for (Stone stone : poolStones) poolPanel.add(createStoneButton(stone, "POOL"));

        linePanel.revalidate();
        linePanel.repaint();
        poolPanel.revalidate();
        poolPanel.repaint();
    }

    private ScalableStoneButton createStoneButton(Stone stone, String location) {
        ScalableStoneButton btn = new ScalableStoneButton(stone, stoneImages.get(stone.type), hiddenImage);
        btn.setPreferredSize(new Dimension(120, 120)); 
        btn.putClientProperty("location", location);

        MouseAdapter ma = new MouseAdapter() {
            Point pressPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    stone.faceUp = !stone.faceUp; 
                    btn.updateButtonState(); 
                    btn.repaint();
                    return;
                }
                pressPoint = e.getPoint();
                draggedStone = stone;
                
                dragLabel.setStone(stone);
                dragLabel.symbolImage = stoneImages.get(stone.type); 
                dragLabel.hiddenImage = hiddenImage;
                dragLabel.updateButtonState(); 
                
                dragLabel.setBounds(0, 0, 120, 120); 
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedStone != null && pressPoint != null) {
                    if (pressPoint.distance(e.getPoint()) > 5) {
                        // SỬA LỖI TÀNG HÌNH KHI KÉO: Ẩn nút gốc khi kéo đi
                        if (!dragLabel.isVisible()) {
                            btn.setVisible(false);
                            glassPane.setVisible(true);
                            dragLabel.setVisible(true);
                        }
                        
                        Point p = e.getLocationOnScreen();
                        SwingUtilities.convertPointFromScreen(p, glassPane);
                        dragLabel.setLocation(p.x - 60, p.y - 60);
                        glassPane.repaint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) || draggedStone == null) return;
                
                boolean wasDragging = dragLabel.isVisible();
                
                glassPane.setVisible(false);
                dragLabel.setVisible(false);
                
                if (!wasDragging && pressPoint != null && pressPoint.distance(e.getPoint()) <= 5) {
                    handleSwapClick(stone, location);
                } else if (wasDragging) {
                    Point dropPoint = e.getLocationOnScreen();
                    Component droppedOn = SwingUtilities.getDeepestComponentAt(getContentPane(), dropPoint.x, dropPoint.y);
                    handleDrop(droppedOn, dropPoint);
                }
                
                draggedStone = null;
                updateUIState(); // Hàm này sẽ tự làm mới và hiện lại mọi viên đá đang có trong List
            }
        };

        btn.addMouseListener(ma);
        btn.addMouseMotionListener(ma);
        return btn;
    }

    private void handleSwapClick(Stone clickedStone, String location) {
        if (!"LINE".equals(location)) return;

        if (selectedSwapStone == null) {
            selectedSwapStone = clickedStone;
            updateUIState(); 
        } else if (selectedSwapStone == clickedStone) {
            selectedSwapStone = null;
            updateUIState(); 
        } else {
            int index1 = lineStones.indexOf(selectedSwapStone);
            int index2 = lineStones.indexOf(clickedStone);
            
            lineStones.set(index1, clickedStone);
            lineStones.set(index2, selectedSwapStone);
            
            selectedSwapStone = null; 
        }
    }

    private void handleDrop(Component droppedOn, Point dropPointOnScreen) {
        if (droppedOn == null) return;
        selectedSwapStone = null; 

        if (droppedOn instanceof ScalableStoneButton targetBtn) {
            Stone targetStone = targetBtn.getStone();
            String targetLoc = (String) targetBtn.getClientProperty("location");
            
            if (targetStone != null && targetStone != draggedStone && "LINE".equals(targetLoc)) {
                Point p = new Point(dropPointOnScreen);
                SwingUtilities.convertPointFromScreen(p, targetBtn);
                
                int oldIndex = lineStones.indexOf(draggedStone);
                int targetIndex = lineStones.indexOf(targetStone);
                
                if (poolStones.contains(draggedStone)) poolStones.remove(draggedStone);
                else lineStones.remove(draggedStone);

                if (oldIndex != -1 && targetIndex > oldIndex) targetIndex--;

                if (p.x < targetBtn.getWidth() / 2) lineStones.add(targetIndex, draggedStone); 
                else lineStones.add(targetIndex + 1, draggedStone); 
                return;
            }
        }

        if (SwingUtilities.isDescendingFrom(droppedOn, linePanel) || droppedOn == linePanel) {
            Point p = new Point(dropPointOnScreen);
            SwingUtilities.convertPointFromScreen(p, linePanel);

            int insertIndex = 0;
            for (int i = 0; i < linePanel.getComponentCount(); i++) {
                Component c = linePanel.getComponent(i);
                if (p.x < c.getX() + (c.getWidth() / 2)) break;
                insertIndex = i + 1;
            }

            if (poolStones.contains(draggedStone)) {
                poolStones.remove(draggedStone);
                lineStones.add(insertIndex, draggedStone);
            } else if (lineStones.contains(draggedStone)) {
                int oldIndex = lineStones.indexOf(draggedStone);
                lineStones.remove(draggedStone);
                if (insertIndex > oldIndex) insertIndex--; 
                lineStones.add(insertIndex, draggedStone);
            }
            return;
        }

        if (SwingUtilities.isDescendingFrom(droppedOn, poolPanel) || droppedOn == poolPanel) {
            if (lineStones.contains(draggedStone)) {
                lineStones.remove(draggedStone);
                poolStones.add(draggedStone);
            }
        }
    }

    private static class Stone {
        String type;
        boolean faceUp;

        Stone(String type, boolean faceUp) {
            this.type = type;
            this.faceUp = faceUp;
        }
    }

    // ==========================================
    // NÂNG CẤP: UI VIÊN ĐÁ BO TRÒN VÀ CÓ BÓNG ĐỔ
    // ==========================================
    private final class ScalableStoneButton extends JButton {
        private Stone currentStone;
        Image symbolImage; 
        Image hiddenImage; 
        
        public ScalableStoneButton() { 
            setupButtonUI();
        }

        public ScalableStoneButton(Stone stone, Image symbol, Image hidden) {
            this.currentStone = stone;
            this.symbolImage = symbol;
            this.hiddenImage = hidden;
            setupButtonUI();
            updateButtonState(); 
        }
        
        private void setupButtonUI() {
            setFocusPainted(false);
            setContentAreaFilled(false); // Xóa nền mặc định
            setBorderPainted(false);     // Xóa viền Windows mặc định
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        public void setStone(Stone stone) { 
            this.currentStone = stone;
            updateButtonState();
            repaint();
        }

        public Stone getStone() { return this.currentStone; }

        public void updateButtonState() {
            if (currentStone == null) {
            }
            // Không cần set Border/Background ở đây nữa vì paintComponent sẽ lo việc đó
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (currentStone == null) return;
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            int arc = 40; // Độ bo tròn góc
            
            // 1. VẼ BÓNG ĐỔ (Drop Shadow)
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillRoundRect(8, 8, w - 16, h - 16, arc, arc);

            // 2. VẼ NỀN VIÊN ĐÁ (Sáng nếu lật ngửa, Tối nếu úp)
            g2d.setColor(currentStone.faceUp ? new Color(245, 245, 240) : new Color(60, 65, 70));
            g2d.fillRoundRect(4, 4, w - 16, h - 16, arc, arc);

            // 3. VẼ VIÊN (Stroke) VÀ VIỀN CHỌN SWAP (Nếu có)
            if (currentStone == selectedSwapStone) {
                g2d.setColor(new Color(50, 205, 50)); // Viền Xanh lá chói khi được chọn Swap
                g2d.setStroke(new BasicStroke(4));
            } else {
                g2d.setColor(new Color(150, 150, 150)); // Viền xám bạc sang trọng
                g2d.setStroke(new BasicStroke(2));
            }
            g2d.drawRoundRect(4, 4, w - 16, h - 16, arc, arc);

            // 4. VẼ ICON BÊN TRONG (Nếu có ảnh)
            Image rawImage = currentStone.faceUp ? symbolImage : hiddenImage;
            
            if (rawImage != null) {
                int padding = (int)(Math.min(w, h) * 0.15);
                int maxWidth = w - (padding * 2) - 16;
                int maxHeight = h - (padding * 2) - 16;

                int imgWidth = rawImage.getWidth(null);
                int imgHeight = rawImage.getHeight(null);
                
                double scale = Math.min((double)maxWidth / imgWidth, (double)maxHeight / imgHeight); 
                int scaledWidth = (int)(imgWidth * scale);
                int scaledHeight = (int)(imgHeight * scale);

                // Căn giữa hình ảnh vào tâm viên đá
                g2d.drawImage(rawImage, (w - scaledWidth - 8) / 2, (h - scaledHeight - 8) / 2, scaledWidth, scaledHeight, this);
            } else {
                // Nếu lỗi không load được ảnh thì hiện chữ chữa cháy
                String textFallback = currentStone.faceUp ? currentStone.type : "?";
                g2d.setColor(currentStone.faceUp ? Color.BLACK : Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(textFallback, (w - fm.stringWidth(textFallback) - 8) / 2, (h + fm.getAscent() - 8) / 2 - 4);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}
            TellstonesSandbox game = new TellstonesSandbox();
            game.setLocationRelativeTo(null);
            game.setVisible(true);
        });
    }
}