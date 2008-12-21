/*
 * PoliceApp.java
 */

package Harvester;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class PoliceApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        
        PoliceModel model = new PoliceModel();
        PoliceView view = new PoliceView(this, model);
        model.setView(view);
        
        show(view);
        
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of PoliceApp
     */
    public static PoliceApp getApplication() {
        return Application.getInstance(PoliceApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(PoliceApp.class, args);
    }
}
