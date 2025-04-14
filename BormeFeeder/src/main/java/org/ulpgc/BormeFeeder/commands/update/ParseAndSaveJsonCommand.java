    package org.ulpgc.BormeFeeder.commands.update;

    import com.google.gson.Gson;

    import com.google.gson.JsonObject;
    import org.ulpgc.BormeFeeder.commands.update.SaveDataCommand;
    import org.ulpgc.BormeFeeder.services.Command;
    import org.ulpgc.BormeFeeder.services.Input;
    import org.ulpgc.BormeFeeder.services.Output;

    public class ParseAndSaveJsonCommand implements Command {
        private final Input input;
        private final Output output;

        public ParseAndSaveJsonCommand(Input input, Output output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public String execute() {
            String jsonResponse = input.getValue("jsonResponse");
            Gson gson = new Gson();
            JsonObject response = gson.fromJson(jsonResponse, JsonObject.class);

            SaveDataCommand saveDataCommand = new SaveDataCommand(input, output, response);
            saveDataCommand.execute();
            return jsonResponse;

        }
    }