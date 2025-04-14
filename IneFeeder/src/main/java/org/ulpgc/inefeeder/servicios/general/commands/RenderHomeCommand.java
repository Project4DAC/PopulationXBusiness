package org.ulpgc.inefeeder.servicios.general.commands;

import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;

import static main.java.org.ulpgc.inefeeder.servicios.general.helpers.HtmlUtil.getHtmlFooter;
import static main.java.org.ulpgc.inefeeder.servicios.general.helpers.HtmlUtil.getHtmlHeader;

public class RenderHomeCommand implements Command {
    private final Input input;
    private final Output output;

    public RenderHomeCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    enum FuncionesINE {
        OPERACIONES_DISPONIBLES,
        DATOS_TABLA,
        DATOS_SERIE,
        DATOS_METADATAOPERACION,
        OPERACION,
        VARIABLES,
        VARIABLES_OPERACION,
        VALORES_VARIABLE,
        VALORES_VARIABLEOPERACION,
        TABLAS_OPERACION,
        GRUPOS_TABLA,
        VALORES_GRUPOSTABLA,
        SERIE,
        SERIES_OPERACION,
        VALORES_SERIE,
        SERIES_TABLA,
        SERIE_METADATAOPERACION,
        PERIODICIDADES,
        PUBLICACIONES,
        PUBLICACIONES_OPERACION,
        PUBLICACIONFECHA_PUBLICACION
    }

    @Override
    public String execute() {
        StringBuilder html = new StringBuilder(getHtmlHeader());
        html.append("<h1>Consulta de datos del INE</h1>\n");
        html.append("<form id='ineForm' method='post' action='/fetchINEData'>\n");
        html.append("<div><label for='function'>Función:</label>\n");
        html.append("<select name='function' id='function' onchange='updateParamFields()'>\n");

        for (FuncionesINE function : FuncionesINE.values()) {
            html.append("<option value='").append(function.name()).append("'>")
                    .append(function.name()).append("</option>\n");
        }

        html.append("</select></div>\n");

        html.append("<div style='margin-top: 10px;'><label for='language'>Idioma:</label>\n");
        html.append("<input type='text' id='language' name='language' value='es' placeholder='es, en, etc.'></div>\n");

        html.append("<div id='additionalParams' style='margin-top: 15px;'></div>\n");

        html.append("<div style='margin-top: 15px;'><button type='submit'>Consultar datos</button></div>\n");
        html.append("</form>\n");

        // Insertamos el JavaScript necesario para actualizar dinámicamente los campos
        html.append(generateJavaScript());

        html.append(getHtmlFooter());

        output.setValue("html", html.toString());
        return null;
    }

    private String generateJavaScript() {
        StringBuilder script = new StringBuilder("<script>\n");
        script.append("    function updateParamFields() {\n");
        script.append("      const selectedFunction = document.getElementById('function').value;\n");
        script.append("      const paramsDiv = document.getElementById('additionalParams');\n");
        script.append("      paramsDiv.innerHTML = '';\n");

        script.append("      const paramsByFunction = {\n");
        script.append("        'DATOS_TABLA': ['idTabla'],\n");
        script.append("        'DATOS_SERIE': ['idSerie'],\n");
        script.append("        'DATOS_METADATAOPERACION': ['idOperacion'],\n");
        script.append("        'OPERACION': ['idOperacion'],\n");
        script.append("        'VARIABLES_OPERACION': ['idOperacion'],\n");
        script.append("        'VALORES_VARIABLEOPERACION': ['idOperacion', 'idVariable'],\n");
        script.append("        'TABLAS_OPERACION': ['idOperacion'],\n");
        script.append("        'GRUPOS_TABLA': ['idTabla'],\n");
        script.append("        'VALORES_GRUPOSTABLA': ['idTabla', 'idGrupo'],\n");
        script.append("        'SERIE': ['idSerie'],\n");
        script.append("        'SERIES_OPERACION': ['idOperacion'],\n");
        script.append("        'VALORES_SERIE': ['idSerie'],\n");
        script.append("        'SERIES_TABLA': ['idTabla'],\n");
        script.append("        'SERIE_METADATAOPERACION': ['idSerie'],\n");
        script.append("        'PUBLICACIONES_OPERACION': ['idOperacion'],\n");
        script.append("        'PUBLICACIONFECHA_PUBLICACION': ['idPublicacion']\n");
        script.append("      };\n");

        script.append("      const params = paramsByFunction[selectedFunction] || [];\n");
        script.append("      params.forEach(param => {\n");
        script.append("        const paramDiv = document.createElement('div');\n");
        script.append("        paramDiv.style.marginTop = '10px';\n");

        script.append("        const label = document.createElement('label');\n");
        script.append("        label.setAttribute('for', param);\n");
        script.append("        label.textContent = param + ':';\n");

        script.append("        const input = document.createElement('input');\n");
        script.append("        input.setAttribute('type', 'text');\n");
        script.append("        input.setAttribute('id', param);\n");
        script.append("        input.setAttribute('name', param);\n");
        script.append("        input.setAttribute('placeholder', 'Introduce el ' + param);\n");
        script.append("        input.style.padding = '8px';\n");
        script.append("        input.style.width = '300px';\n");
        script.append("        input.style.marginTop = '5px';\n");

        script.append("        paramDiv.appendChild(label);\n");
        script.append("        paramDiv.appendChild(input);\n");
        script.append("        paramsDiv.appendChild(paramDiv);\n");
        script.append("      });\n");
        script.append("    }\n");

        script.append("    document.addEventListener('DOMContentLoaded', function () { updateParamFields(); });\n");
        script.append("</script>\n");

        return script.toString();
    }
}
