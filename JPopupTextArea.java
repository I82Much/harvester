package Harvester;

import javax.swing.Action;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

/**
 * This class is simply a text area with a built in popup menu that handles
 * basic Cut, Copy, Paste, Select all functionality.
 * Taken from http://www.objectdefinitions.com/odblog/2007/jtextarea-with-popup-menu/
 * with slight modifications.  
 * @date December 20, 2008
 */
public class JPopupTextArea extends JTextArea
{
    private HashMap actions;

    final static String COPY = "Copy";
    final static String CUT = "Cut";
    final static String PASTE = "Paste";
    final static String SELECTALL = "Select All";

    
    public JPopupTextArea()
    {
        addPopupMenu();
    }
    

    private void addPopupMenu() {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem copyItem = new JMenuItem();
        copyItem.setAction(getActionMap().get(DefaultEditorKit.copyAction));
        copyItem.setText(COPY);

        final JMenuItem cutItem = new JMenuItem();
        cutItem.setAction(getActionMap().get(DefaultEditorKit.cutAction));
        cutItem.setText(CUT);

        final JMenuItem pasteItem = new JMenuItem(PASTE);
        pasteItem.setAction(getActionMap().get(DefaultEditorKit.pasteAction));
        pasteItem.setText(PASTE);

        final JMenuItem selectAllItem = new JMenuItem(SELECTALL);
        selectAllItem.setAction(getActionMap().get(DefaultEditorKit.selectAllAction));
        selectAllItem.setText(SELECTALL);

        menu.add(copyItem);
        menu.add(cutItem);
        menu.add(pasteItem);
        menu.add(new JSeparator());
        menu.add(selectAllItem);

        add(menu);
        addMouseListener(new PopupTriggerMouseListener(menu, this));
    }
    

    private Action getActionByName(String name, String description) {
        Action a = (Action)(actions.get(name));
        a.putValue(Action.NAME, description);
        return a;
    }


    private void createActionTable() {
        actions = new HashMap();
        Action[] actionsArray = getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
    }

    public static class PopupTriggerMouseListener extends MouseAdapter
    {
        private JPopupMenu popup;
        private JComponent component;

        public PopupTriggerMouseListener(JPopupMenu popup, JComponent component)
        {
            this.popup = popup;
            this.component = component;
        }

        //some systems trigger popup on mouse press, others on mouse release, we want to cater for both
        private void showMenuIfPopupTrigger(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
               popup.show(component, e.getX() + 3, e.getY() + 3);
            }
        }

        //according to the javadocs on isPopupTrigger, checking for popup trigger on mousePressed and mouseReleased
        //should be all  that is required
        //public void mouseClicked(MouseEvent e) 
        //{
        //    showMenuIfPopupTrigger(e);
        //}

        @Override
        public void mousePressed(MouseEvent e)
        {
            showMenuIfPopupTrigger(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            showMenuIfPopupTrigger(e);
        }

    }

}