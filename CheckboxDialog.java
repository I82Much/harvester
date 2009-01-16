package Harvester;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * This allows user to 
 * Based on code available at http://forums.sun.com/thread.jspa?threadID=5357662
 * @author Nick
 */
class CheckboxDialog implements ItemListener
{
  
  /**
   * The names that will be displayed next to each of the check boxes
   */
  private final String[] choices;
  /**
   * The states of the checkboxes.  If element i is 'true' then the ith 
   * checkbox is checked
   */
  private boolean[] selections;
  
  private JCheckBox[] checkBoxes;
 
  private final String title;
  
  private Component parent;
  
  private static final int NUM_COLS = 2;
  
  public CheckboxDialog(Component parent, String title, String[] choices, boolean[] selections) {
      
      if (choices.length != selections.length) {
          throw new IllegalArgumentException("Error, " +
                  "choices and selections arrays must be of same length");
      }
      this.parent = parent;
      this.title = title;
      this.choices = choices;
      this.selections = selections;
      
      checkBoxes = new JCheckBox[choices.length];
      for (int i=0; i <choices.length; i++) {
          checkBoxes[i] = new JCheckBox(choices[i], selections[i]);
          checkBoxes[i].addItemListener(this);
    }
  }
  
  public CheckboxDialog(String title, String[] choices, boolean[] selections) {
      this(null, title, choices, selections);
  }
  
  public CheckboxDialog(String title, String[] choices) {
      this(null, title, choices, new boolean[choices.length]);
  }
 
  public void select() {
      
      int numRows = (int) Math.ceil(checkBoxes.length / (double) NUM_COLS);
      
      // Create a grid layout with the correct number of rows and columns
      JPanel panel = new JPanel(new GridLayout(numRows, NUM_COLS));
      for (int i=0; i<checkBoxes.length; i++) {
        panel.add(checkBoxes[i]);
      }
      JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE);
      JDialog dialog = optionPane.createDialog(parent, title);
      dialog.setVisible(true);
      dialog.dispose();
      
                 
  }
 
  public boolean[] getSelections() {
      return selections;
  }
  
  
 
  // implements ItemListener
  public void itemStateChanged(ItemEvent evt) {
    Object src = evt.getItemSelectable();
    for (int i=0; i<selections.length; i++) {
      if (checkBoxes[i] == src) {
        selections[i] = (evt.getStateChange() == ItemEvent.SELECTED);
      }
    }
  }

    
}
