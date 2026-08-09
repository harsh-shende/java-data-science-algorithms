package in.harshshende.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class containing helper methods for processing Tablesaw {@link Table} objects.
 *
 * <p>Currently provides helper methods for identifying categorical, continuous and temporal columns in a given table.
 * This class is not meant to be instantiated.</p>
 */
public class Utils {

    /**
     * Logger for the {@link Utils} class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Column data types treated as categorical for classification analysis.
     */
    private static final Set<String> CATEGORICAL_COLUMN_DATA_TYPE = Set.of("BOOLEAN", "STRING", "TEXT");

    /**
     * Column data types treated as temporal for analysis.
     */
    private static final Set<String> TEMPORAL_COLUMN_DATA_TYPE = Set.of("TIME", "DATE", "INSTANT", "DATE_TIME");

    /**
     * Column data types treated as continuous for correlation analysis.
     */
    private static final Set<String> CONTINUOUS_COLUMN_DATA_TYPE = Set.of("SHORT", "INTEGER", "LONG", "FLOAT", "DOUBLE");

    /**
     * Prevents instantiation of this utility class.
     */
    private Utils() {
    }

    /**
     * Identifies categorical columns in the input table by inspecting its structure metadata.
     *
     * @param inputTable table whose column metadata should be scanned; must not be {@code null}
     * @return an ordered list of column names whose types are {@code BOOLEAN}, {@code STRING} or {@code TEXT}
     */
    public static List<String> getCategoricalColumnNames(Table inputTable) {
        // Metadata
        Table structureTable = inputTable.structure();
        int structureTableRowCount = structureTable.rowCount();
        List<String> categoricalColumnNames = new ArrayList<>();
        Column<String> nameColumn = structureTable.stringColumn("Column Name");
        Column<String> typeColumn = structureTable.stringColumn("Column Type");
        LOGGER.info("Utils::getCategoricalColumnNames Scanning {} columns for categorical types", structureTableRowCount);

        // Filtering columns by categorical data types
        for (int i = 0; i < structureTableRowCount; i++) {
            String columnName = nameColumn.getString(i);
            String columnType = typeColumn.getString(i);

            if (Utils.CATEGORICAL_COLUMN_DATA_TYPE.contains(columnType)) {
                LOGGER.debug("Utils::getCategoricalColumnNames Identified categorical column '{}' (type: {})", columnName, columnType);
                categoricalColumnNames.add(columnName);
            }
        }

        LOGGER.info("Utils::getCategoricalColumnNames Found {} categorical columns", categoricalColumnNames.size());
        return categoricalColumnNames;
    }

    /**
     * Identifies temporal columns in the input table by inspecting its structure metadata.
     *
     * @param inputTable table whose column metadata should be scanned; must not be {@code null}
     * @return an ordered list of column names whose types are {@code TIME}, {@code DATE}, {@code INSTANT} or {@code DATE_TIME}
     */
    public static List<String> getTemporalColumnNames(Table inputTable) {
        // Metadata
        Table structureTable = inputTable.structure();
        int structureTableRowCount = structureTable.rowCount();
        List<String> temporalColumnNames = new ArrayList<>();
        Column<String> nameColumn = structureTable.stringColumn("Column Name");
        Column<String> typeColumn = structureTable.stringColumn("Column Type");
        LOGGER.info("Utils::getTemporalColumnNames Scanning {} columns for temporal types", structureTableRowCount);

        // Filtering columns by temporal data types
        for (int i = 0; i < structureTableRowCount; i++) {
            String columnName = nameColumn.getString(i);
            String columnType = typeColumn.getString(i);

            if (Utils.TEMPORAL_COLUMN_DATA_TYPE.contains(columnType)) {
                LOGGER.debug("Utils::getTemporalColumnNames Identified temporal column '{}' (type: {})", columnName, columnType);
                temporalColumnNames.add(columnName);
            }
        }

        LOGGER.info("Utils::getTemporalColumnNames Found {} temporal columns", temporalColumnNames.size());
        return temporalColumnNames;
    }

    /**
     * Identifies continuous columns in the input table by inspecting its structure metadata.
     *
     * @param inputTable table whose column metadata should be scanned; must not be {@code null}
     * @return an ordered list of column names whose types are {@code SHORT}, {@code INTEGER}, {@code LONG}, {@code FLOAT} or {@code DOUBLE}
     */
    public static List<String> getContinuousColumnNames(Table inputTable) {
        // Metadata
        Table structureTable = inputTable.structure();
        int structureTableRowCount = structureTable.rowCount();
        List<String> continuousColumnNames = new ArrayList<>();
        Column<String> nameColumn = structureTable.stringColumn("Column Name");
        Column<String> typeColumn = structureTable.stringColumn("Column Type");
        LOGGER.info("Utils::getContinuousColumnNames Scanning {} columns for continuous types", structureTableRowCount);

        // Filtering columns by continuous data types
        for (int i = 0; i < structureTableRowCount; i++) {
            String columnName = nameColumn.getString(i);
            String columnType = typeColumn.getString(i);

            if (Utils.CONTINUOUS_COLUMN_DATA_TYPE.contains(columnType)) {
                LOGGER.debug("Utils::getContinuousColumnNames Identified continuous column '{}' (type: {})", columnName, columnType);
                continuousColumnNames.add(columnName);
            }
        }

        LOGGER.info("Utils::getContinuousColumnNames Found {} continuous columns", continuousColumnNames.size());
        return continuousColumnNames;
    }
}
