import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GameSelector extends JFrame {

    public GameSelector() {
        setTitle("Sandbox Board Game Collection");
        setSize(450, 450); // Tăng chiều cao lên 450 để chứa đủ 3 nút cho thoáng
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(40, 45, 52)); // Nền tối hiện đại

        // --- TIÊU ĐỀ ---
        JLabel titleLabel = new JLabel("GAME SELECTOR", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // --- KHU VỰC NÚT CHỌN GAME ---
        JPanel buttonPanel = new JPanel();
        // Chuyển thành GridLayout(3 hàng, 1 cột, khoảng cách dọc 20px)
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 20)); 
        buttonPanel.setOpaque(false);
        // Tăng lề trái phải lên 70 để các nút không bị quá bè ngang
        buttonPanel.setBorder(new EmptyBorder(10, 70, 30, 70));

        // Nút Yahtzee
        JButton yahtzeeBtn = createGameButton("Yahtzee", new Color(34, 139, 34)); // Xanh lá
        yahtzeeBtn.addActionListener(e -> launchGame(new YahtzeeSandbox(), "Yahtzee"));
        buttonPanel.add(yahtzeeBtn);

        // Nút Tellstones
        JButton tellstonesBtn = createGameButton("Tellstones", new Color(50, 100, 200)); // Xanh dương
        tellstonesBtn.addActionListener(e -> launchGame(new TellstonesSandbox(), "Tellstones"));
        buttonPanel.add(tellstonesBtn);

        // Nút Hive
        JButton hiveBtn = createGameButton("Hive", new Color(200, 150, 50)); // Màu mật ong
        hiveBtn.addActionListener(e -> launchGame(new HiveSandbox(), "Hive"));
        buttonPanel.add(hiveBtn);

        add(buttonPanel, BorderLayout.CENTER);
        
        // --- FOOTER ---
        JLabel footerLabel = new JLabel("Select a game to play. Close the game to return here.", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(Color.LIGHT_GRAY);
        footerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(footerLabel, BorderLayout.SOUTH);
    }

    // Hàm tiện ích tạo nút bấm theo phong cách thiết kế phẳng (Flat Design)
    private JButton createGameButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20)); // Font chữ hiện đại, to hơn một chút
        btn.setBackground(bgColor);
        btn.setForeground(Color.BLACK); // Đổi thành màu trắng để nổi bật trên nền đậm
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Tạo viền phẳng hiện đại: Viền ngoài tối màu hơn nền một chút, viền trong là khoảng trống
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Hiệu ứng hover cho nút mượt mà
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

    // THUẬT TOÁN KHỞI CHẠY GAME VÀ LẮNG NGHE SỰ KIỆN ĐÓNG
    private void launchGame(JFrame gameFrame, String gameName) {
        this.setVisible(false);
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);

        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("Closed " + gameName + ", returning to Hub...");
                GameSelector.this.setLocationRelativeTo(null); 
                GameSelector.this.setVisible(true);
            }
        });

        gameFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { 
                // Sử dụng giao diện chuẩn của hệ điều hành
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}
            
            GameSelector hub = new GameSelector();
            hub.setLocationRelativeTo(null);
            hub.setVisible(true);
        });
    }
}