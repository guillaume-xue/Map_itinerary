package fr.u_paris.gla.project.controllers;

import javax.swing.JButton;
import javax.swing.JTextArea;

public class KeyboardController {

  private JTextArea textArea1;
  private JTextArea textArea2;
  private JButton button;

  /**
   * Constructor for KeyboardController.
   *
   * @param textArea the JTextArea to control
   */
  public KeyboardController(JTextArea textArea) {
    this.textArea1 = textArea;
    initKeySpace();
  }

  public KeyboardController(JTextArea textArea1, JTextArea textArea2, JButton button) {
    this.textArea1 = textArea1;
    this.textArea2 = textArea2;
    this.button = button;
    initKey();
  }

  /**
   * Initializes the key listener to prevent newlines in the JTextArea.
   */
  private void initKey() {
    // Prevent newlines by intercepting key events
    textArea1.addKeyListener(new EnterKeyAdapter());
    textArea2.addKeyListener(new EnterKeyAdapter());
  }

  private void initKeySpace() {
    // Prevent newlines by intercepting key events
    textArea1.addKeyListener(new EnterSpaceKeyAdapter());
  }

  private class EnterSpaceKeyAdapter extends java.awt.event.KeyAdapter {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
      if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER || e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
        e.consume(); // Prevent the Enter key from inserting a newline
      }
    }
  }

  /// Custom KeyAdapter to handle Enter key events
  private class EnterKeyAdapter extends java.awt.event.KeyAdapter {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
      if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
        e.consume(); // Prevent the Enter key from inserting a newline
        if (textArea1.isFocusOwner()) {
          textArea2.requestFocus();
        } else if (textArea2.isFocusOwner()) {
          button.doClick();
        }

      }
    }
  }

}
