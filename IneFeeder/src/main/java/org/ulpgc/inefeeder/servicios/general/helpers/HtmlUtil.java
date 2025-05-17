package org.ulpgc.inefeeder.servicios.general.helpers;

public class HtmlUtil {
    public static String getHtmlHeader() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>"'" + title + "'"</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; }
                    h1 { color: #333; }
                    label { display: block; margin-bottom: 5px; }
                    select, input { padding: 8px; width: 300px; }
                    button { padding: 8px 16px; background-color: #4CAF50; color: white; border: none; cursor: pointer; }
                    button:hover { background-color: #45a049; }
                    pre { white-space: pre-wrap; }
                </style>
            </head>
            <body>
        """;
    }

    public static String getHtmlFooter() {
        return "</body></html>";
    }
}
