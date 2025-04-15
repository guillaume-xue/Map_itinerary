package fr.u_paris.gla.project.controllers;

import javax.swing.JTextArea;

public class KeyboardController {

  private JTextArea textArea;

  /**
   * Constructor for KeyboardController.
   *
   * @param textArea the JTextArea to control
   */
  public KeyboardController(JTextArea textArea) {
    this.textArea = textArea;
    initKey();
  }

  /**
   * Initializes the key listener to prevent newlines in the JTextArea.
   */
  private void initKey() {
    // Prevent newlines by intercepting key events
    textArea.addKeyListener(new EnterKeyAdapter());
  }

  /// Custom KeyAdapter to handle Enter key events
  private class EnterKeyAdapter extends java.awt.event.KeyAdapter {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
      if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
        e.consume(); // Prevent the Enter key from inserting a newline
      }
    }
  }

}
