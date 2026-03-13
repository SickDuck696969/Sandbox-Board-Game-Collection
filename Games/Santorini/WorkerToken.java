package Santorini;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

public class WorkerToken extends JComponent {
    Color color;
    Image workerImage;
    int dragMouseX, dragMouseY;
    boolean isDragging = false;
    public boolean isOnBoard = false;
    public int gridCol = -1, gridRow = -1;

    private JLayeredPane layeredPane;
    private JPanel boardPanel;
    private JPanel workersPool;
    private int cellSize;

    // Constructor nhận thêm các tham số môi trường từ Game chính
    public WorkerToken(Color color, String imagePath, JLayeredPane layeredPane, JPanel boardPanel, JPanel workersPool, int cellSize) {
        this.color = color;
        this.layeredPane = layeredPane;
        this.boardPanel = boardPanel;
        this.workersPool = workersPool;
        this.cellSize = cellSize;
        
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
                    int col = (p.x + getWidth()/2) / cellSize;
                    int row = (p.y + getHeight()/2) / cellSize;
                    
                    int snapX = col * cellSize + (cellSize - getWidth()) / 2;
                    int snapY = row * cellSize + (cellSize - getHeight()) / 2;
                    
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
            g2d.drawImage(workerImage, 5, 5, 40, 40, null);
        } else {
            Polygon triangle = new Polygon();
            triangle.addPoint(25, 5);  
            triangle.addPoint(5, 45);  
            triangle.addPoint(45, 45); 
            
            g2d.setColor(color);
            g2d.fillPolygon(triangle);
            g2d.setColor(color.darker().darker());
            g2d.setStroke(new BasicStroke(3));
            g2d.drawPolygon(triangle);
        }
    }
}