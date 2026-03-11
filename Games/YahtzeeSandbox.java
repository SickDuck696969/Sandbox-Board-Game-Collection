package Games;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class YahtzeeSandbox extends JFrame {

    @SuppressWarnings("FieldMayBeFinal")
    private List<Dice> diceList = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private Random random = new Random();
    
    private int rollCount = 0;
    @SuppressWarnings("FieldMayBeFinal")
    private JLabel rollCountLabel;
    private boolean isRolling = false; 
    
    private final JPanel diceTrayPanel;
    @SuppressWarnings("FieldMayBeFinal")
    private JButton addPlayerBtn;
    @SuppressWarnings("FieldMayBeFinal")
    private JButton resetBtn; // Thêm nút Reset
    @SuppressWarnings("FieldMayBeFinal")
    private JButton randomColorBtn;
    @SuppressWarnings("FieldMayBeFinal")
    private JPanel playersContainer;
    
    private final String[] CATEGORIES = {
            "Aces (1s)", "Twos (2s)", "Threes (3s)", "Fours (4s)", "Fives (5s)", "Sixes (6s)",
            "--- UPPER TOTAL ---", "Bonus (If >= 63)",
            "3 of a kind", "4 of a kind", "Full House", "Sm. Straight", "Lg. Straight", "YAHTZEE", "Chance",
            "=== GRAND TOTAL ==="
    };

    public YahtzeeSandbox() {
        setTitle("Yahtzee");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==========================================
        // TOP SECTION: DICE TRAY
        // ==========================================
        diceTrayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        diceTrayPanel.setBackground(new Color(34, 139, 34)); 
        diceTrayPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                "Dice Tray (Left-Click: Hold | Right-Click: Roll)",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.WHITE));

        diceTrayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && !isRolling) {
                    rollUnheldDice();
                }
            }
        });

        // Khởi tạo xúc xắc
        for (int i = 0; i < 5; i++) {
            Dice die = new Dice();
            die.value = random.nextInt(6) + 1;
            diceList.add(die);
            diceTrayPanel.add(die);
        }

        rollCountLabel = new JLabel("Roll Count: 0");
        rollCountLabel.setFont(new Font("Arial", Font.BOLD, 22));
        rollCountLabel.setForeground(Color.YELLOW);
        rollCountLabel.setBorder(new EmptyBorder(0, 30, 0, 30));
        diceTrayPanel.add(rollCountLabel);

        randomColorBtn = new JButton("Random Colors");
        randomColorBtn.setFont(new Font("Arial", Font.BOLD, 14));
        randomColorBtn.setFocusPainted(false);
        randomColorBtn.addActionListener(e -> randomizeColors());
        diceTrayPanel.add(randomColorBtn);

        add(diceTrayPanel, BorderLayout.NORTH);

        // ==========================================
        // BOTTOM SECTION: SCORE SHEET
        // ==========================================
        JPanel scoreSheetPanel = new JPanel(new BorderLayout());
        scoreSheetPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel addPlayerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPlayerPanel.add(new JLabel("Player Name: "));
        JTextField nameInput = new JTextField(15);
        
        // Bắt sự kiện nhấn Enter để thêm người chơi nhanh hơn
        nameInput.addActionListener(e -> addPlayerBtn.doClick());
        addPlayerPanel.add(nameInput);
        
        addPlayerBtn = new JButton("Add Player");
        addPlayerBtn.setFocusPainted(false);
        addPlayerPanel.add(addPlayerBtn);
        
        // Khởi tạo và thêm nút Reset
        resetBtn = new JButton("Reset Board");
        resetBtn.setFocusPainted(false); // Cho màu hơi đỏ để nổi bật
        resetBtn.setForeground(Color.BLACK);
        addPlayerPanel.add(resetBtn);
        
        scoreSheetPanel.add(addPlayerPanel, BorderLayout.NORTH);

        JPanel mainScoreArea = new JPanel(new BorderLayout());
        
        JPanel labelsColumn = new JPanel(new GridLayout(CATEGORIES.length + 1, 1, 2, 2));
        labelsColumn.add(createHeaderLabel("CATEGORIES"));
        for (String cat : CATEGORIES) {
            JLabel label = new JLabel(cat, SwingConstants.RIGHT);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(0, 5, 0, 10));
            labelsColumn.add(label);
        }
        mainScoreArea.add(labelsColumn, BorderLayout.WEST);

        playersContainer = new JPanel();
        playersContainer.setLayout(new BoxLayout(playersContainer, BoxLayout.X_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(mainScoreArea);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        mainScoreArea.add(playersContainer, BorderLayout.CENTER);
        
        scoreSheetPanel.add(scrollPane, BorderLayout.CENTER);
        add(scoreSheetPanel, BorderLayout.CENTER);

        // Chức năng thêm người chơi
        addPlayerBtn.addActionListener(e -> {
            String name = nameInput.getText().trim();
            if (!name.isEmpty()) {
                addPlayerColumn(name);
                nameInput.setText("");
            }
        });
        
        // Chức năng Reset bảng điểm
        // Chức năng Reset bảng điểm
        resetBtn.addActionListener(e -> {
            if (playersContainer.getComponentCount() > 0) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to clear all scores?", 
                        "Reset Scores", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    
                    // Duyệt qua từng cột người chơi trong bảng
                    for (Component colComp : playersContainer.getComponents()) {
                        if (colComp instanceof JPanel playerCol) {
                            
                            // Duyệt qua từng ô trong cột (bỏ qua Label tên ở vị trí 0)
                            for (int i = 1; i < playerCol.getComponentCount(); i++) {
                                Component cellComp = playerCol.getComponent(i);
                                if (cellComp instanceof JTextField scoreField) {
                                    scoreField.setText(""); // Xóa số điểm
                                    scoreField.setForeground(Color.BLACK); // Đưa màu chữ về mặc định
                                    
                                    // Dựa vào màu nền trắng để phân biệt ô nhập điểm thường và ô Total
                                    if (scoreField.getBackground().equals(Color.WHITE)) {
                                        scoreField.putClientProperty("isConfirmed", false); // Hủy trạng thái đã chốt điểm
                                    }
                                }
                            }
                        }
                    }
                    resetTurn(); // Đưa xúc xắc và số lượt đổ về 0
                }
            }
        });
    }

    // ==========================================
    // LOGIC & ANIMATION
    // ==========================================
    private void randomizeColors() {
        Color newTrayColor = Color.getHSBColor(random.nextFloat(), 0.6f, 0.6f);
        Color newDiceBgColor = Color.getHSBColor(random.nextFloat(), 0.2f, 0.9f);
        Color newPipColor = Color.getHSBColor(random.nextFloat(), 0.8f, 0.3f); 

        diceTrayPanel.setBackground(newTrayColor);
        addPlayerBtn.setBackground(Color.WHITE);
        randomColorBtn.setBackground(Color.WHITE);

        for (Dice die : diceList) {
            die.diceBgColor = newDiceBgColor;
            die.pipColor = newPipColor;
            die.repaint();
        }
    }

    private void rollUnheldDice() {
        if (isRolling) return; 
        isRolling = true;
        rollCount++;
        rollCountLabel.setText("Roll Count: " + rollCount);

        Timer timer = new Timer(50, new ActionListener() {
            int ticks = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                ticks++;
                for (Dice die : diceList) {
                    if (!die.isHeld) {
                        die.value = random.nextInt(6) + 1;
                        die.repaint();
                    }
                }
                
                if (ticks >= 15) {
                    ((Timer) e.getSource()).stop();
                    isRolling = false;
                }
            }
        });
        timer.start();
    }

    private void resetTurn() {
        rollCount = 0; 
        rollCountLabel.setText("Roll Count: 0");
        for (Dice die : diceList) {
            die.isHeld = false;
            die.repaint();
        }
    }

    private void addPlayerColumn(String playerName) {
        JPanel playerCol = new JPanel(new GridLayout(CATEGORIES.length + 1, 1, 2, 2));
        playerCol.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.LIGHT_GRAY));
        
        // Tạo label tên người chơi và thêm sự kiện click chuột phải để xóa
        JLabel nameLabel = createHeaderLabel(playerName);
        nameLabel.setToolTipText("Right-click to remove this player");
        nameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int confirm = JOptionPane.showConfirmDialog(YahtzeeSandbox.this, 
                            "Remove player: " + playerName + "?", 
                            "Remove Player", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        playersContainer.remove(playerCol);
                        playersContainer.revalidate();
                        playersContainer.repaint();
                    }
                }
            }
        });
        playerCol.add(nameLabel);
        
        List<JTextField> textFields = new ArrayList<>();

        for (String cat : CATEGORIES) {
            JTextField scoreField = new JTextField(5);
            scoreField.setHorizontalAlignment(JTextField.CENTER);
            scoreField.setFont(new Font("Arial", Font.BOLD, 14));
            
            boolean isTotalRow = cat.contains("TOTAL") || cat.contains("Bonus");
            
            if (isTotalRow) {
                scoreField.setEditable(false);
                scoreField.setBackground(new Color(220, 220, 220));
                scoreField.putClientProperty("isConfirmed", true);
            } else {
                scoreField.setEditable(false);
                scoreField.setBackground(Color.WHITE); 
                scoreField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                scoreField.putClientProperty("isConfirmed", false);

                scoreField.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isRolling) return;
                        boolean confirmed = (boolean) scoreField.getClientProperty("isConfirmed");
                        if (!confirmed) {
                            int[] currentDice = new int[5];
                            for (int d = 0; d < 5; d++) currentDice[d] = diceList.get(d).value;
                            int score = YahtzeeScorer.calculateScore(cat, currentDice);
                            scoreField.setForeground(Color.RED);
                            scoreField.setText(String.valueOf(score));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (isRolling) return;
                        boolean confirmed = (boolean) scoreField.getClientProperty("isConfirmed");
                        if (!confirmed) {
                            scoreField.setText("");
                            scoreField.setForeground(Color.BLACK); 
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (isRolling) return; 
                        boolean confirmed = (boolean) scoreField.getClientProperty("isConfirmed");
                        
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            if (!confirmed) {
                                int[] currentDice = new int[5];
                                for (int d = 0; d < 5; d++) currentDice[d] = diceList.get(d).value;
                                int score = YahtzeeScorer.calculateScore(cat, currentDice);
                                
                                scoreField.setForeground(Color.BLACK);
                                scoreField.setText(String.valueOf(score));
                                scoreField.putClientProperty("isConfirmed", true);
                                
                                updateTotals(textFields);
                                resetTurn(); 
                            }
                        } 
                        else if (SwingUtilities.isRightMouseButton(e)) {
                            if (confirmed) {
                                scoreField.putClientProperty("isConfirmed", false);
                                updateTotals(textFields);
                                
                                int[] currentDice = new int[5];
                                for (int d = 0; d < 5; d++) currentDice[d] = diceList.get(d).value;
                                int score = YahtzeeScorer.calculateScore(cat, currentDice);
                                scoreField.setForeground(Color.RED);
                                scoreField.setText(String.valueOf(score));
                            }
                        }
                    }
                });
            }
            textFields.add(scoreField);
            playerCol.add(scoreField);
        }
        
        playersContainer.add(playerCol);
        playersContainer.revalidate();
        playersContainer.repaint();
    }

    private void updateTotals(List<JTextField> fields) {
        int upperTotal = 0;
        for (int i = 0; i < 6; i++) { 
            boolean confirmed = (boolean) fields.get(i).getClientProperty("isConfirmed");
            if (confirmed) {
                String txt = fields.get(i).getText();
                if (!txt.isEmpty()) upperTotal += Integer.parseInt(txt);
            }
        }
        
        fields.get(6).setText(String.valueOf(upperTotal)); 
        
        int bonus = (upperTotal >= 63) ? 35 : 0;
        fields.get(7).setText(String.valueOf(bonus)); 
        
        int lowerTotal = 0;
        for (int i = 8; i <= 14; i++) { 
            boolean confirmed = (boolean) fields.get(i).getClientProperty("isConfirmed");
            if (confirmed) {
                String txt = fields.get(i).getText();
                if (!txt.isEmpty()) lowerTotal += Integer.parseInt(txt);
            }
        }
        
        fields.get(15).setText(String.valueOf(upperTotal + bonus + lowerTotal)); 
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setOpaque(true);
        label.setBackground(Color.DARK_GRAY);
        label.setForeground(Color.WHITE);
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        return label;
    }

    // ==========================================
    // YAHTZEE SCORER CLASS (LOGIC)
    // ==========================================
    private static class YahtzeeScorer {
        public static int calculateScore(String category, int[] dice) {
            int sum = 0;
            int[] counts = new int[7]; 
            
            for (int d : dice) {
                sum += d;
                counts[d]++;
            }

            switch (category) {
                case "Aces (1s)" -> { return counts[1] * 1; }
                case "Twos (2s)" -> { return counts[2] * 2; }
                case "Threes (3s)" -> { return counts[3] * 3; }
                case "Fours (4s)" -> { return counts[4] * 4; }
                case "Fives (5s)" -> { return counts[5] * 5; }
                case "Sixes (6s)" -> { return counts[6] * 6; }
                case "3 of a kind" -> {
                    for (int count : counts) if (count >= 3) return sum;
                    return 0;
                }
                case "4 of a kind" -> {
                    for (int count : counts) if (count >= 4) return sum;
                    return 0;
                }
                case "Full House" -> {
                    boolean hasThree = false, hasTwo = false;
                    for (int count : counts) {
                        if (count == 3) hasThree = true;
                        if (count == 2) hasTwo = true;
                        if (count == 5) return 25; 
                    }
                    return (hasThree && hasTwo) ? 25 : 0;
                }
                case "Sm. Straight" -> {
                    if (hasStraight(counts, 4)) return 30;
                    return 0;
                }
                case "Lg. Straight" -> {
                    if (hasStraight(counts, 5)) return 40;
                    return 0;
                }
                case "YAHTZEE" -> {
                    for (int count : counts) if (count == 5) return 50;
                    return 0;
                }
                case "Chance" -> { return sum; }
                default -> { return 0; }
            }
        }

        private static boolean hasStraight(int[] counts, int lengthRequired) {
            int consecutive = 0;
            for (int i = 1; i <= 6; i++) {
                if (counts[i] > 0) {
                    consecutive++;
                    if (consecutive >= lengthRequired) return true;
                } else {
                    consecutive = 0;
                }
            }
            return false;
        }
    }

    // ==========================================
    // CUSTOM DICE COMPONENT
    // ==========================================
    private class Dice extends JComponent {
        int value = 1;
        boolean isHeld = false;
        Color diceBgColor = Color.WHITE; 
        Color pipColor = Color.BLACK; 
        private final int SIZE = 80;

        public Dice() {
            setPreferredSize(new Dimension(SIZE, SIZE));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isRolling) return; 

                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isHeld = !isHeld;
                        repaint();
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        rollUnheldDice();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isHeld) {
                g2d.setColor(diceBgColor.darker()); 
            } else {
                g2d.setColor(diceBgColor);
            }
            g2d.fillRoundRect(2, 2, SIZE - 4, SIZE - 4, 15, 15);

            if (isHeld) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(4));
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
            }
            g2d.drawRoundRect(2, 2, SIZE - 4, SIZE - 4, 15, 15);

            g2d.setColor(pipColor);
            int pipSize = 14;
            int center = (SIZE - pipSize) / 2;
            int left = 15;
            int right = SIZE - 15 - pipSize;
            int top = 15;
            int bottom = SIZE - 15 - pipSize;

            switch (value) {
                case 1 -> g2d.fillOval(center, center, pipSize, pipSize);
                case 2 -> {
                    g2d.fillOval(left, top, pipSize, pipSize); g2d.fillOval(right, bottom, pipSize, pipSize);
                }
                case 3 -> {
                    g2d.fillOval(left, top, pipSize, pipSize); g2d.fillOval(center, center, pipSize, pipSize); g2d.fillOval(right, bottom, pipSize, pipSize);
                }
                case 4 -> {
                    g2d.fillOval(left, top, pipSize, pipSize); g2d.fillOval(right, top, pipSize, pipSize); g2d.fillOval(left, bottom, pipSize, pipSize); g2d.fillOval(right, bottom, pipSize, pipSize);
                }
                case 5 -> {
                    g2d.fillOval(left, top, pipSize, pipSize); g2d.fillOval(right, top, pipSize, pipSize); g2d.fillOval(center, center, pipSize, pipSize); g2d.fillOval(left, bottom, pipSize, pipSize); g2d.fillOval(right, bottom, pipSize, pipSize);
                }
                case 6 -> {
                    g2d.fillOval(left, top, pipSize, pipSize); g2d.fillOval(right, top, pipSize, pipSize); g2d.fillOval(left, center, pipSize, pipSize); g2d.fillOval(right, center, pipSize, pipSize); g2d.fillOval(left, bottom, pipSize, pipSize); g2d.fillOval(right, bottom, pipSize, pipSize);
                }
            }
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            YahtzeeSandbox game = new YahtzeeSandbox();
            game.setLocationRelativeTo(null);
            game.setVisible(true);
        });
    }
}