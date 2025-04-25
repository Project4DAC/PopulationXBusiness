package org.ulpgc.inefeeder.servicios.general.commands;

import org.ulpgc.inefeeder.servicios.Command;
import org.ulpgc.inefeeder.servicios.Input;
import org.ulpgc.inefeeder.servicios.Output;

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

        // Language selector
        html.append("<div style='margin-top:10px;'><label for='language'>Idioma:</label>\n");
        html.append("<select id='language' name='language'>\n");
        html.append("<option value='es' selected>Español</option>\n");
        html.append("<option value='en'>Inglés</option>\n");
        html.append("</select></div>\n");

        // Function selector
        html.append("<div style='margin-top:10px;'><label for='function'>Función:</label>\n");
        html.append("<select name='function' id='function' onchange='updateParamFields()'>\n");
        for (FuncionesINE function : FuncionesINE.values()) {
            html.append("<option value='").append(function.name()).append("'>")
                    .append(function.name()).append("</option>\n");
        }
        html.append("</select></div>\n");

        // Additional params container
        html.append("<div id='additionalParams' style='margin-top:15px;'></div>\n");

        html.append("<div style='margin-top:15px;'><button type='submit'>Consultar datos</button></div>\n");
        html.append("</form>\n");

        // Daily fetch button
        html.append("<form method='post' action='/runDailyFetcher' style='margin-top:30px;'>\n");
        html.append("<button type='submit' style='background-color:#007bff; color:white; padding:10px 20px; border:none; border-radius:5px;'>Ejecutar Fetch Diario del INE</button>\n");
        html.append("</form>\n");

        // JavaScript for dynamic fields
        html.append(generateJavaScript());
        html.append(getHtmlFooter());

        output.setValue("html", html.toString());
        return null;
    }

    private String generateJavaScript() {
        return "<script>\n" +
                "const paramsByFunction = {\n" +
                "  'DATOS_TABLA': [{name: 'idTabla', alias: 'id', repeatable: false}, {name: 'núlt', alias: 'nult', repeatable: false}, {name: 'tv', alias: 'tv', repeatable: true}],\n" +
                "  'DATOS_SERIE': [{name: 'idSerie', alias: 'id', repeatable: false}],\n" +
                "  'TABLAS_OPERACION': [{name: 'idOperacion', alias: 'id', repeatable: false}],\n" +
                "  // ... agrega más funciones según necesites\n" +
                "};\n" +
                "function updateParamFields() {\n" +
                "  const selected = document.getElementById('function').value;\n" +
                "  const container = document.getElementById('additionalParams');\n" +
                "  container.innerHTML = '';\n" +
                "  const params = paramsByFunction[selected] || [];\n" +
                "  params.forEach(p => addParamField(container, p));\n" +
                "}\n" +
                "function addParamField(container, param) {\n" +
                "  const wrapper = document.createElement('div'); wrapper.style.marginTop = '10px';\n" +
                "  const label = document.createElement('label'); label.textContent = param.name + ':'; label.style.marginRight = '5px';\n" +
                "  wrapper.appendChild(label);\n" +
                "  const input = document.createElement('input');\n" +
                "  input.type = 'text'; input.name = param.alias; input.placeholder = 'Introduce ' + param.name; input.style.padding = '8px'; input.style.width = '200px';\n" +
                "  wrapper.appendChild(input);\n" +
                "  if (param.repeatable) {\n" +
                "    const btn = document.createElement('button'); btn.type = 'button'; btn.textContent = '+'; btn.style.marginLeft = '5px';\n" +
                "    btn.onclick = () => { addParamField(container, param); };\n" +
                "    wrapper.appendChild(btn);\n" +
                "  }\n" +
                "  container.appendChild(wrapper);\n" +
                "}\n" +
                "document.addEventListener('DOMContentLoaded', updateParamFields);\n" +
                "</script>";
    }
}
