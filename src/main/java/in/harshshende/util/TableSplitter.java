package in.harshshende.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.List;

/**
 * Splits a Tablesaw {@link Table} into train, test, and validation subsets.
 *
 * <p>The split is performed eagerly at construction time. The train subset contains the first 80% of rows, the test
 * subset contains the remaining 20% of rows, and the validation subset collects rows with no missing values in any
 * column, up to 50% of the total row count.</p>
 */
public class TableSplitter {

    /**
     * Logger for the {@link TableSplitter} class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TableSplitter.class);

    /**
     * The original table supplied at construction time.
     */
    private final Table inputTable;

    /**
     * The training subset, containing the first 80% of rows from the input table.
     */
    private final Table trainTable;

    /**
     * The test subset, containing the remaining 20% of rows from the input table.
     */
    private final Table testTable;

    /**
     * The validation subset, containing rows with no missing values, capped at 50% of the input row count.
     * May be {@code null} if no complete rows are found.
     */
    private final Table validationTable;

    /**
     * Creates a splitter for the given input table.
     *
     * <p>Train, test, and validation tables are built eagerly and can be retrieved via the corresponding getter
     * methods.</p>
     *
     * @param inputTable the table to split; must not be {@code null}
     */
    public TableSplitter(Table inputTable) {
        LOGGER.info("TableSplitter::TableSplitter Initializing split for table with {} rows and {} columns", inputTable.rowCount(), inputTable.columnNames().size());
        this.inputTable = inputTable;
        this.trainTable = this.constructTrainTable();
        this.testTable = this.constructTestTable();
        this.validationTable = this.constructValidationTable();
        LOGGER.info("TableSplitter::TableSplitter Table split initialized successfully");
    }

    /**
     * Returns the original input table.
     *
     * @return the table passed to the constructor
     */
    public Table getInputTable() {
        return inputTable;
    }

    /**
     * Returns the training subset.
     *
     * @return a table containing the first 80% of rows from the input table
     */
    public Table getTrainTable() {
        return trainTable;
    }

    /**
     * Returns the test subset.
     *
     * @return a table containing the remaining 20% of rows from the input table
     */
    public Table getTestTable() {
        return testTable;
    }

    /**
     * Returns the validation subset.
     *
     * @return a table of rows with no missing values (up to 50% of the input row count),
     * or {@code null} if no such rows exist
     */
    public Table getValidationTable() {
        return validationTable;
    }

    /**
     * Builds the training table from the first 80% of rows in the input table.
     *
     * @return the training subset
     */
    private Table constructTrainTable() {
        // Metadata
        int rowCount = this.inputTable.rowCount();
        int trainTableRowCount = (int) ((0.8) * (rowCount));
        LOGGER.info("TableSplitter::constructTrainTable Building train table with {} rows from {} total rows", trainTableRowCount, rowCount);

        // Extracting first 80% of rows for training
        Table trainTable = this.inputTable.first(trainTableRowCount);
        LOGGER.info("TableSplitter::constructTrainTable Train table built with {} rows", trainTable.rowCount());
        return trainTable;
    }

    /**
     * Builds the test table from the remaining 20% of rows in the input table.
     *
     * @return the test subset
     */
    private Table constructTestTable() {
        // Metadata
        int rowCount = this.inputTable.rowCount();
        int testTableRowCount = (int) ((0.2) * (rowCount));
        LOGGER.info("TableSplitter::constructTestTable Building test table with {} rows from {} total rows", testTableRowCount, rowCount);

        // Extracting last 20% of rows for testing
        Table testTable = this.inputTable.last(testTableRowCount);
        LOGGER.info("TableSplitter::constructTestTable Test table built with {} rows", testTable.rowCount());
        return testTable;
    }

    /**
     * Builds the validation table by collecting rows with no missing values in any column.
     *
     * <p>Iterates through the input table in row order and appends complete rows until the validation table reaches
     * 50% of the input row count or all rows have been scanned.</p>
     *
     * @return the validation subset, or {@code null} if no complete rows are found
     */
    private Table constructValidationTable() {
        // Metadata
        Table validationTable = null;
        int rowCount = this.inputTable.rowCount();
        int threshold = (int) ((0.5) * (rowCount));
        List<String> inputTableColumnNames = this.inputTable.columnNames();
        LOGGER.info("TableSplitter::constructValidationTable Building validation table from {} rows (max {} complete rows)", rowCount, threshold);

        // Scanning rows for complete records without missing values
        for (int i = 0; i < rowCount; i++) {
            // Assume row is complete until a missing value is found.
            boolean flag = true;
            Row row = this.inputTable.row(i);

            // Checking each column for missing values
            for (String columnName : inputTableColumnNames) {
                if (row.isMissing(columnName)) {
                    // Mark this row as incomplete and stop checking more columns.
                    flag = false;
                    break;
                }
            }

            // Appending complete rows up to validation threshold
            if (flag) {
                if (validationTable == null) {
                    validationTable = this.inputTable.first(0);
                }

                if ((validationTable.rowCount()) < threshold) {
                    // Append complete row while under validation-size threshold.
                    validationTable = validationTable.append(row);
                } else {
                    LOGGER.debug("TableSplitter::constructValidationTable Validation threshold of {} rows reached", threshold);
                    break;
                }
            }
        }

        // Logging validation table build result
        if (validationTable == null) {
            LOGGER.info("TableSplitter::constructValidationTable No complete rows found; validation table is null");
        } else {
            LOGGER.info("TableSplitter::constructValidationTable Validation table built with {} rows", validationTable.rowCount());
        }
        return validationTable;
    }
}
