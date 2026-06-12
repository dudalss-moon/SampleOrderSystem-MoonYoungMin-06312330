package ssemi.order;

import ssemi.order.db.DatabaseConfig;
import ssemi.order.db.SchemaInitializer;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.ProductionQueueRepository;
import ssemi.order.repository.SampleRepository;
import ssemi.order.repository.jdbc.JdbcOrderRepository;
import ssemi.order.repository.jdbc.JdbcProductionQueueRepository;
import ssemi.order.repository.jdbc.JdbcSampleRepository;
import ssemi.order.ui.ConsoleMenu;
import ssemi.order.ui.InputHandler;

import java.sql.Connection;

public final class Main {

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:h2:file:./data/ssemi;DB_CLOSE_ON_EXIT=FALSE";
        DatabaseConfig dbConfig = new DatabaseConfig(jdbcUrl);
        Connection conn = dbConfig.getConnection();
        SchemaInitializer.initialize(conn);

        SampleRepository sampleRepository = new JdbcSampleRepository(conn);
        OrderRepository orderRepository = new JdbcOrderRepository(conn);
        ProductionQueueRepository queueRepository = new JdbcProductionQueueRepository(conn, orderRepository);

        InputHandler inputHandler = new InputHandler();
        ConsoleMenu menu = new ConsoleMenu(inputHandler, sampleRepository, orderRepository, queueRepository, 1000L);
        menu.run();

        dbConfig.close();
    }
}
