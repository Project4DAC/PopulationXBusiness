package org.ulpgc.business.service;

import com.zaxxer.hikari.HikariDataSource;
import org.ulpgc.business.interfaces.DataProcessor;
import org.ulpgc.business.operations.DAO.DAOFactory;
import org.ulpgc.business.operations.DAO.IndicadorDAO;
import org.ulpgc.business.operations.POJO.Indicador;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Logger;

public class INEDataMartProcessor implements DataProcessor {
    private static final Logger LOGGER = Logger.getLogger(INEDataMartProcessor.class.getName());
    private final String outputDirectory;
    private final HikariDataSource dataSource;

    public INEDataMartProcessor(String outputDirectory, HikariDataSource dataSource) {
        this.outputDirectory = outputDirectory;
        this.dataSource = dataSource;
    }

    @Override
    public void process() {
        LOGGER.info("Procesando datos INE para DataMart en: " + outputDirectory);
        try (Connection connection = dataSource.getConnection()) {
            IndicadorDAO indicadorDAO = DAOFactory.getInstance().getIndicadorDAO();
            indicadorDAO.setConnection(connection);

            List<Indicador> indicadores = indicadorDAO.findAll(); // Este método debe implementarse

            for (Indicador indicador : indicadores) {
                String filePath = outputDirectory + "/" + indicador.getCod() + ".csv";
                try (FileWriter writer = new FileWriter(filePath)) {
                    writer.write("Fecha,Valor,Anyo,Periodo\n");
                    indicador.getData().forEach(dato -> {
                        try {
                            writer.write(dato.getFecha() + "," + dato.getUnidad() + "," + dato.getFecha() + "\n");
                        } catch (IOException e) {
                            LOGGER.warning("Error escribiendo dato: " + e.getMessage());
                        }
                    });
                }
            }

            LOGGER.info("Exportación completa al DataMart.");
        } catch (Exception e) {
            LOGGER.severe("Error al procesar DataMart INE: " + e.getMessage());
        }
    }
}