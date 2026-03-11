import Games.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder; 

public class GameSelector extends JFrame {

    public GameSelector() {
        setTitle("Sandbox Board Game Collection");
        setSize(450, 550); // Tăng chiều cao lên 550 để chứa đủ 4 game
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(40, 45, 52)); 

        // --- TIÊU ĐỀ ---
        JLabel titleLabel = new JLabel("GAME SELECTOR", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // --- KHU VỰC NÚT CHỌN GAME ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 1, 0, 20)); // Đổi thành 4 hàng cho 4 game
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 70, 30, 70));

        // Nút Yahtzee
        JButton yahtzeeBtn = createGameButton("Yahtzee", new Color(34, 139, 34)); 
        yahtzeeBtn.addActionListener(e -> launchGame(new YahtzeeSandbox()));
        buttonPanel.add(yahtzeeBtn);

        // Nút Tellstones
        JButton tellstonesBtn = createGameButton("Tellstones", new Color(50, 100, 200)); 
        tellstonesBtn.addActionListener(e -> launchGame(new TellstonesSandbox()));
        buttonPanel.add(tellstonesBtn);

        // Nút Hive
        JButton hiveBtn = createGameButton("Hive", new Color(200, 150, 50)); 
        hiveBtn.addActionListener(e -> launchGame(new HiveSandbox()));
        buttonPanel.add(hiveBtn);

        // Nút Blokus
        JButton blokusBtn = createGameButton("Blokus", new Color(138, 43, 226)); // Màu tím mộng mơ
        blokusBtn.addActionListener(e -> launchGame(new BlokusSandbox()));
        buttonPanel.add(blokusBtn);

        add(buttonPanel, BorderLayout.CENTER);
        
        // --- FOOTER ---
        JLabel footerLabel = new JLabel("Select a game to play. Close the game to return here.", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(Color.LIGHT_GRAY);
        footerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(footerLabel, BorderLayout.SOUTH);
    }

    private JButton createGameButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE); 
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }

    // ĐÃ XÓA BIẾN: String gameName (Giải quyết triệt để cảnh báo của VS Code)
    private void launchGame(JFrame gameFrame) {
        this.setVisible(false);
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);

        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                GameSelector.this.setLocationRelativeTo(null); 
                GameSelector.this.setVisible(true);
            }
        });

        gameFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { 
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}
            
            GameSelector hub = new GameSelector();
            hub.setLocationRelativeTo(null);
            hub.setVisible(true);
        });
    }
}