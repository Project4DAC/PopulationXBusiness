package org.ulpgc.inefeeder.commands.publisher;

import org.ulpgc.inefeeder.servicios.general.Interfaces.Command;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;
import org.ulpgc.inefeeder.servicios.general.helpers.PublisherFactory;


public class PublishINEDataCommand implements Command {
    private final Input input;
    private final Output output;
    private final Publisher publisher;


    public PublishINEDataCommand(Input input, Output output, Publisher publisher) {
        this.input = input;
        this.output = output;
        this.publisher = publisher;
    }


    public PublishINEDataCommand(Input input, Output output) {
        this(input, output, PublisherFactory.createActiveMQPublisher());
    }

    @Override
    public String execute() {
        try {
            String function = input.getValue("function");
            String jsonResponse = input.getValue("jsonResponse");
            
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                output.setResponse(400, "No data to publish");
                return null;
            }
            
            // Use the function name as the queue name
            String destination = "INE." + function;
            
            // Publish the message
            publisher.publish(destination, jsonResponse);
            
            // Set success response
            output.setValue("publishStatus", "Data successfully published to ActiveMQ");
            output.setValue("destination", destination);
            
            return function;
        } catch (Exception e) {
            e.printStackTrace();
            output.setResponse(500, "Error publishing data: " + e.getMessage());
            return null;
        }
    }
}