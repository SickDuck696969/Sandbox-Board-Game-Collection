import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class TellstonesSandbox extends JFrame {

    private final String[] STONE_TYPES = {"Crown", "Shield", "Sword", "Flag", "Knight", "Hammer", "Scales"};
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

    // Make score labels accessible to the reset method
    private JLabel p1ScoreLabel = new JLabel("0");
    private JLabel p2ScoreLabel = new JLabel("0");

    public TellstonesSandbox() {
        setTitle("Tellstones");
        setSize(1300, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        loadOriginalImages();

        for (String type : STONE_TYPES) {
            poolStones.add(new Stone(type, true));
        }

        // --- TOP: SCOREBOARD & RESET ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        topPanel.add(createPlayerScorePanel("Player 1", p1ScoreLabel), BorderLayout.WEST);
        
        // Add Reset Button to the center
        JButton resetBtn = new JButton("Reset Game");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 16));
        resetBtn.setBackground(new Color(200, 60, 60));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> resetGameState());
        
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.add(resetBtn);
        topPanel.add(centerWrapper, BorderLayout.CENTER);
        
        topPanel.add(createPlayerScorePanel("Player 2", p2ScoreLabel), BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: THE LINE ---
        linePanel = new JPanel();
        linePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 50));
        linePanel.setBackground(new Color(34, 40, 49));
        linePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "The Line (Left-Click two stones to SWAP | Drag to gaps to INSERT)",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.LIGHT_GRAY));

        JScrollPane scrollPane = new JScrollPane(linePanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: THE POOL ---
        poolPanel = new JPanel();
        poolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));
        poolPanel.setBackground(new Color(57, 62, 70));
        poolPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "The Pool (Drag to move | Right-Click to Flip)",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.LIGHT_GRAY));
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

    // --- NEW: RESET LOGIC ---
    private void resetGameState() {
        // Clear both lists
        lineStones.clear();
        poolStones.clear();
        
        // Regenerate the 7 default face-up stones in the pool
        for (String type : STONE_TYPES) {
            poolStones.add(new Stone(type, true));
        }
        
        // Reset state variables
        selectedSwapStone = null;
        draggedStone = null;
        
        // Reset scores
        p1ScoreLabel.setText("0");
        p2ScoreLabel.setText("0");
        
        // Repaint the screen
        updateUIState();
    }

    private JPanel createPlayerScorePanel(String playerName, JLabel scoreLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JLabel nameLabel = new JLabel(playerName + ": ");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(new Color(40, 120, 200));
        
        JButton minusBtn = new JButton("-");
        minusBtn.setFocusPainted(false);
        minusBtn.addActionListener(e -> {
            int score = Integer.parseInt(scoreLabel.getText());
            if (score > 0) scoreLabel.setText(String.valueOf(score - 1));
        });
        
        JButton plusBtn = new JButton("+");
        plusBtn.setFocusPainted(false);
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

    private Image loadRawImage(String path) {
        try {
            File file = new File(path);
            if (file.exists()) return ImageIO.read(file); 
        } catch (IOException e) {}
        return null;
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
                dragLabel.setBounds(0, 0, 120, 120); 
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedStone != null && pressPoint != null) {
                    if (pressPoint.distance(e.getPoint()) > 5) {
                        glassPane.setVisible(true);
                        dragLabel.setVisible(true);
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
                updateUIState();
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
            updateUIState(); // Force repaint to show the green selection border
        } else if (selectedSwapStone == clickedStone) {
            selectedSwapStone = null;
            updateUIState(); // Force repaint to clear the border
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

    private final class ScalableStoneButton extends JButton {
        private Stone currentStone;
        private Image symbolImage; 
        private Image hiddenImage; 
        
        public ScalableStoneButton() { 
            setFocusPainted(false);
            setBorder(BorderFactory.createLineBorder(Color.YELLOW, 4));
        }

        public ScalableStoneButton(Stone stone, Image symbol, Image hidden) {
            this.currentStone = stone;
            this.symbolImage = symbol;
            this.hiddenImage = hidden;
            
            setFocusPainted(false);
            updateButtonState(); 
        }
        
        public void setStone(Stone stone) { 
            this.currentStone = stone;
            updateButtonState();
            repaint();
        }

        public Stone getStone() { return this.currentStone; }

        public void updateButtonState() {
            if (currentStone == null) return;

            if (currentStone == selectedSwapStone) {
                setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));
            } else {
                setBorder(BorderFactory.createRaisedBevelBorder());
            }

            if (!currentStone.faceUp) {
                setBackground(Color.DARK_GRAY);
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(238, 238, 238));
                setForeground(Color.BLACK);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            if (currentStone == null) return;

            Image rawImage = currentStone.faceUp ? symbolImage : hiddenImage;
            String textFallback = currentStone.faceUp ? currentStone.type : "? Hidden ?";

            if (rawImage == null) {
                setIcon(null);
                setText(textFallback);
                return;
            }

            setText("");
            setIcon(null); 

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int btnWidth = getWidth();
            int btnHeight = getHeight();
            int padding = (int)(Math.min(btnWidth, btnHeight) * 0.10);
            
            int maxWidth = btnWidth - (padding * 2);
            int maxHeight = btnHeight - (padding * 2);

            int imgWidth = rawImage.getWidth(null);
            int imgHeight = rawImage.getHeight(null);
            
            double scale = Math.min((double)maxWidth / imgWidth, (double)maxHeight / imgHeight); 
            int scaledWidth = (int)(imgWidth * scale);
            int scaledHeight = (int)(imgHeight * scale);

            g2d.drawImage(rawImage, (btnWidth - scaledWidth) / 2, (btnHeight - scaledHeight) / 2, scaledWidth, scaledHeight, this);
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