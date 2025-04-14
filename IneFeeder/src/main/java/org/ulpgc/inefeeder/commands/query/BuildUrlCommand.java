package main.java.org.ulpgc.inefeeder.commands.query;


import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;

public class BuildUrlCommand implements Command {
    private static final String BaseUrl = "https://servicios.ine.es/wstempus/js/";
    private final Input input;
    private final Output output;
    private String url;

    public BuildUrlCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        try{
            input();
            process();
            output();
            return output.result();
            }
        catch (Exception e){
            output.setResponse(422,"Unprocessable Entity");
            return output.errorCode();
            }
    }

    private void input() {
        String language = input.getValue("language");
        String function = input.getValue("function");

        if (language == null || function == null) {
            throw new IllegalArgumentException("Los parámetros language y function no pueden ser nulos.");
        }
    }

    private void process() {
        StringBuilder urlBuilder = new StringBuilder(BaseUrl);

        urlBuilder.append((String) input.getValue("language")).append("/");
        urlBuilder.append((String) input.getValue("function"));

        String inputValue = input.getValue("inputValue");
        if (inputValue != null && !inputValue.isEmpty()) {
            urlBuilder.append("/").append(inputValue);
        }

        java.util.Map<String, String> params = input.getValue("params");
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("/").append(buildQueryString(params));
        }

        this.url = urlBuilder.toString();
    }

    private void output() {
        output.setValue("url", this.url);
    }

    private String buildQueryString(java.util.Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;

        for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                queryString.append("&");
            }
            queryString.append(entry.getValue());
            first = false;
        }

        return queryString.toString();
    }
}