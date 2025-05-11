package org.ulpgc.inefeeder.commands.publisher;

import org.ulpgc.inefeeder.servicios.Command;
import org.ulpgc.inefeeder.servicios.Input;
import org.ulpgc.inefeeder.servicios.Output;

import java.util.prefs.Preferences;

/**
 * Command to update and save ActiveMQ settings
 */
public class UpdateMQSettingsCommand implements Command {
    private final Input input;
    private final Output output;
    private static final String PREF_NODE_NAME = "org.ulpgc.inefeeder";
    private static final String PREF_BROKER_URL = "activemq.broker.url";

    public UpdateMQSettingsCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        String brokerUrl = input.getValue("brokerUrl");
        
        if (brokerUrl != null && !brokerUrl.isEmpty()) {
            // Save the broker URL in preferences
            Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);
            prefs.put(PREF_BROKER_URL, brokerUrl);
            
            // Build response
            StringBuilder html = new StringBuilder();
            html.append("<html><body>");
            html.append("<h2>Configuración de ActiveMQ actualizada</h2>");
            html.append("<p>Broker URL: ").append(brokerUrl).append("</p>");
            html.append("<a href='/'>Volver a inicio</a>");
            html.append("</body></html>");
            
            output.setValue("html", html.toString());
        } else {
            output.setResponse(400, "Broker URL no puede estar vacía");
        }
        
        return null;
    }
    
    /**
     * Gets the saved broker URL from preferences or returns the default if not found
     * @return The ActiveMQ broker URL
     */
    public static String getBrokerUrl() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        return prefs.get(PREF_BROKER_URL, "tcp://localhost:61616");
    }
}