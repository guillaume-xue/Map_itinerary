package fr.u_paris.gla.project.views.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A custom JPanel that represents a square text field.
 */
public class TextSquarePanel extends JPanel {
    private String text;
    private Color squareColor;

    public TextSquarePanel(String text, Color squareColor) {
        this.text = text;
        this.squareColor = squareColor;
        this.setPreferredSize(new Dimension(15, 15));
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    public void setSquareColor(Color color) {
        this.squareColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        // Draw background square
        g.setColor(squareColor);
        g.fillRect(0, 0, width, height);

        // Find max font size that fits within the square
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int fontSize = height; // start with maximum font size
        Font font = new Font("SansSerif", Font.BOLD, fontSize);
        FontMetrics fm;

        // Reduce font size until it fits both width and height
        while (fontSize > 5) {
            font = font.deriveFont((float) fontSize);
            fm = g2d.getFontMetrics(font);
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent() + fm.getDescent();

            if (textWidth <= width * 0.9 && textHeight <= height * 0.9) {
                break;
            }

            fontSize--;
        }

        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height + fm.getAscent() - fm.getDescent()) / 2;

        g2d.drawString(text, x, y);
        g2d.dispose();
    }
  }