package Santorini;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;


public class GodPowerCard {
    public JPanel container;
    public JTextField nameField;
    public JPanel cardPanel;
    public boolean isFaceUp = false;
    public GodPowerDatabase.GodPower godData;
    private Image characterImg;
    private SantoriniSandbox mainGame;

    public GodPowerCard(String defaultPlayerName, int x, int y, int w, int h, SantoriniSandbox mainGame) {
        this.mainGame = mainGame;
        
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
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
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
                    
                    g2d.setColor(new Color(245, 235, 233)); 
                    g2d.fillRect(pad, pad, charW, h - pad * 2);
                    
                    if (characterImg != null) {
                        double scale = Math.min((double)(charW-4)/characterImg.getWidth(null), (double)(h-pad*2-4)/characterImg.getHeight(null));
                        int sw = (int)(characterImg.getWidth(null)*scale);
                        int sh = (int)(characterImg.getHeight(null)*scale);
                        g2d.drawImage(characterImg, pad + (charW-sw)/2, pad + (h-pad*2-sh)/2, sw, sh, null);
                    } else {
                        g2d.setColor(Color.BLACK); 
                        g2d.setFont(new Font("Arial", Font.BOLD, 12));
                        g2d.drawString("CHARACTER", pad + charW/2 - 35, h/2);
                    }
                    
                    // Name Box
                    g2d.setColor(new Color(245, 235, 233));
                    g2d.fillRect(rightX, pad, rightW, 30);
                    g2d.setColor(Color.BLACK);

                    int maxNameWidth = rightW - 20; 
                    int fontSize = 16;              
                    Font nameFont = new Font("Arial", Font.BOLD, fontSize);
                    FontMetrics nameFm = g2d.getFontMetrics(nameFont);

                    while (nameFm.stringWidth(godData.name) > maxNameWidth && fontSize > 9) {
                        fontSize--;
                        nameFont = new Font("Arial", Font.BOLD, fontSize);
                        nameFm = g2d.getFontMetrics(nameFont);
                    }

                    g2d.setFont(nameFont);
                    int nameY = pad + ((30 - nameFm.getHeight()) / 2) + nameFm.getAscent(); 
                    g2d.drawString(godData.name, rightX + 10, nameY);
                    
                    // Timing Box
                    g2d.setColor(new Color(245, 235, 233));
                    g2d.fillRect(rightX, pad + 30 + pad, rightW, 25);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    g2d.drawString(godData.timing, rightX + 10, pad + 30 + pad + 18);
                    
                    // Power Box
                    g2d.setColor(new Color(245, 235, 233));
                    int powerY = pad + 30 + pad + 25 + pad;
                    int powerBoxHeight = h - powerY - pad;
                    g2d.fillRect(rightX, powerY, rightW, powerBoxHeight);
                    g2d.setColor(Color.BLACK);

                    int maxPowerWidth = rightW - 20; 
                    int powerFontSize = 13;          
                    Font powerFont = new Font("Segoe UI", Font.PLAIN, powerFontSize);
                    FontMetrics powerFm = g2d.getFontMetrics(powerFont);

                    while (powerFontSize > 9) {
                        String[] words = godData.power.split(" ");
                        String line = "";
                        int linesCount = 1;
                        
                        for (String word : words) {
                            if (powerFm.stringWidth(line + word) < maxPowerWidth) {
                                line += word + " ";
                            } else {
                                line = word + " ";
                                linesCount++;
                            }
                        }
                        
                        int totalTextHeight = linesCount * (powerFm.getHeight() + 2);
                        
                        if (totalTextHeight > powerBoxHeight - 20) {
                            powerFontSize--;
                            powerFont = new Font("Segoe UI", Font.PLAIN, powerFontSize);
                            powerFm = g2d.getFontMetrics(powerFont);
                        } else {
                            break; 
                        }
                    }

                    g2d.setFont(powerFont);
                    drawWrappedString(g2d, godData.power, rightX + 10, powerY + 20, maxPowerWidth);
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
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // CHUỘT TRÁI: Lật bài
                    isFaceUp = !isFaceUp;
                    cardPanel.repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // CHUỘT PHẢI: Yêu cầu Game đổi card mới (Không trùng lặp)
                    mainGame.rerollCard(GodPowerCard.this);
                }
            }
        });
        container.add(cardPanel, BorderLayout.CENTER);
    }

    public void setGodData(GodPowerDatabase.GodPower god) {
        this.godData = god;
        // Giữ nguyên trạng thái lật/úp hiện tại khi đổi bài
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