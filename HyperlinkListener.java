/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Harvester;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nick
 */
public class HyperlinkListener extends MouseAdapter {
    
    Desktop desktop;
    String url;
    public HyperlinkListener(Desktop desktop, String url) {
        this.desktop = desktop;
        this.url = url;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            // launch browser
            URI uri = null;
            try {
                uri = new URI(url);

                
                desktop.browse(uri);
            } catch (IOException ex) {
                Logger.getLogger(HyperlinkListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(HyperlinkListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        
    }

    
    @Override
    public void mouseEntered(MouseEvent e) {
        e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    public void mouseExited(MouseEvent e) {
        e.getComponent().setCursor(Cursor.getDefaultCursor());
    }
    
}
