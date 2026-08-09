package in.harshshende.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds a column-wise statistical summary from a Tablesaw {@link Table}.
 *
 * <p>For each column in the input table, the summary includes data-quality metrics (missing values, unique counts)
 * and, for numeric columns, descriptive statistics such as mean, median, mode, quartiles, min/max, range, standard
 * deviation, and variance. Non-numeric columns receive {@code "Type Mismatch"} for numeric-only statistics.</p>
 */
public class TableSummary {

    /**
     * Logger for the {@link TableSummary} class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TableSummary.class);

    /**
     * The original table supplied at construction time.
     */
    private final Table inputTable;

    /**
     * The generated summary table, populated during construction.
     */
    private final Table summaryTable;

    /**
     * Creates a summary for the given input table.
     *
     * <p>The summary table is built eagerly and can be retrieved via {@link #getSummaryTable()}.</p>
     *
     * @param inputTable the table to summarize; must not be {@code null}
     */
    public TableSummary(Table inputTable) {
        LOGGER.info("TableSummary::TableSummary Initializing summary for table with {} rows and {} columns", inputTable.rowCount(), inputTable.columnNames().size());
        this.inputTable = inputTable;
        this.summaryTable = this.constructSummaryTable();
        LOGGER.info("TableSummary::TableSummary Summary table initialized successfully");
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
     * Returns the generated summary table.
     *
     * @return a table containing per-column summary statistics
     */
    public Table getSummaryTable() {
        return summaryTable;
    }

    /**
     * Creates a summary table with the same column layout as the input table and populates it with data-quality and
     * descriptive statistics.
     *
     * @return a fully populated summary table
     */
    private Table constructSummaryTable() {
        // Metadata
        long rowCount = this.inputTable.rowCount();
        Table summaryTable = this.inputTable.structure();
        List<String> inputTableColumnNames = this.inputTable.columnNames();
        LOGGER.info("TableSummary::constructSummaryTable Building summary table for {} columns", inputTableColumnNames.size());

        // Data Quality
        StringColumn isMissing = StringColumn.create("Has Missing");
        StringColumn nanCount = StringColumn.create("Missing Count");
        StringColumn uniqueCount = StringColumn.create("Unique Values");

        // Central Tendency
        StringColumn mean = StringColumn.create("Mean");
        StringColumn median = StringColumn.create("Median (Q2)");
        StringColumn mode = StringColumn.create("Mode");

        // Distribution
        StringColumn quartile01 = StringColumn.create("Quartile 1 (Q1)");
        StringColumn quartile03 = StringColumn.create("Quartile 3 (Q3)");

        // Boundaries
        StringColumn minimum = StringColumn.create("Minimum");
        StringColumn maximum = StringColumn.create("Maximum");
        StringColumn range = StringColumn.create("Range");

        // Variability
        StringColumn standardDeviation = StringColumn.create("Std. Deviation");
        StringColumn variance = StringColumn.create("Variance");

        // Identifying numeric columns
        List<NumericColumn<?>> inputTableNumericColumns = this.inputTable.numericColumns();
        List<String> inputTableNumericColumnNames = inputTableNumericColumns.stream().map(inputTableNumericColumn -> inputTableNumericColumn.name()).collect(Collectors.toList());

        // Computing statistics for each input column
        for (String columnName : inputTableColumnNames) {
            LOGGER.debug("TableSummary::constructSummaryTable Processing column '{}'", columnName);

            // Computing data quality metrics
            long uniqueValuesCount = this.inputTable.column(columnName).countUnique();
            long missingValuesCount = this.inputTable.column(columnName).countMissing();
            isMissing.append(missingValuesCount > 0 ? "true" : "false");
            nanCount.append(this.formatIntoFraction(missingValuesCount, rowCount));
            uniqueCount.append(this.formatIntoFraction(uniqueValuesCount, rowCount));

            if (inputTableNumericColumnNames.contains(columnName)) {
                LOGGER.debug("TableSummary::constructSummaryTable Computing numeric statistics for column '{}'", columnName);
                NumericColumn<?> numericColumn = inputTableNumericColumns.get(inputTableNumericColumnNames.indexOf(columnName));

                // Computing central tendency statistics
                mean.append(String.format("%.3f", numericColumn.mean()));
                median.append(String.format("%.3f", numericColumn.median()));

                Object modeValue = null;
                long maximumFrequency = 0L;
                Map<Object, Long> frequencyMap = new HashMap<>();

                for (int i = 0; i < numericColumn.size(); i++) {
                    if (numericColumn.isMissing(i)) {
                        continue;
                    }

                    Object value = numericColumn.get(i);
                    frequencyMap.put(value, frequencyMap.getOrDefault(value, 0L) + 1);
                }

                for (Map.Entry<Object, Long> entry : frequencyMap.entrySet()) {
                    if (entry.getValue() > maximumFrequency) {
                        maximumFrequency = entry.getValue();
                        modeValue = entry.getKey();
                    }
                }

                mode.append(modeValue == null ? "NA" : modeValue.toString());

                // Computing distribution statistics
                quartile01.append(String.format("%.3f", numericColumn.quartile1()));
                quartile03.append(String.format("%.3f", numericColumn.quartile3()));

                // Computing boundary statistics
                minimum.append(String.format("%.3f", numericColumn.min()));
                maximum.append(String.format("%.3f", numericColumn.max()));
                range.append(String.format("%.3f", numericColumn.range()));

                // Computing variability statistics
                standardDeviation.append(String.format("%.3f", numericColumn.standardDeviation()));
                variance.append(String.format("%.3f", numericColumn.variance()));
            } else {
                // Marking non-numeric columns as type mismatch
                LOGGER.debug("TableSummary::constructSummaryTable Column '{}' is non-numeric; marking numeric statistics as type mismatch", columnName);
                mean.append("Type Mismatch");
                median.append("Type Mismatch");
                mode.append("Type Mismatch");

                quartile01.append("Type Mismatch");
                quartile03.append("Type Mismatch");

                minimum.append("Type Mismatch");
                maximum.append("Type Mismatch");
                range.append("Type Mismatch");

                standardDeviation.append("Type Mismatch");
                variance.append("Type Mismatch");
            }
        }

        // Assembling summary table with statistic columns
        summaryTable.addColumns(isMissing, nanCount, uniqueCount, mean, median, mode, quartile01, quartile03, minimum, maximum, range, standardDeviation, variance);

        LOGGER.info("TableSummary::constructSummaryTable Summary table built with {} statistic columns for {} input columns", summaryTable.columnCount(), inputTableColumnNames.size());
        return summaryTable;
    }

    /**
     * Formats a count as a fraction string ({@code numerator/denominator}).
     *
     * @param numerator   the part count (e.g. missing or unique values)
     * @param denominator the total row count
     * @return a string in the form {@code "numerator/denominator"}
     */
    private String formatIntoFraction(long numerator, long denominator) {
        return numerator + "/" + denominator;
    }
}
