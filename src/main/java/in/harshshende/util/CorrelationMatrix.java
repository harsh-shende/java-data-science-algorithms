package in.harshshende.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.util.List;

/**
 * Computes a Pearson correlation matrix for continuous columns in a Tablesaw {@link Table}.
 * <p>
 * Only columns with {@code INTEGER}, {@code LONG}, {@code FLOAT}, or {@code DOUBLE} types are included.
 * Missing values are excluded from mean, variance, and correlation calculations. The matrix is built
 * eagerly at construction time and can be retrieved via {@link #getCorrelationMatrix()}.
 */
public class CorrelationMatrix {

    /**
     * Logger for the {@link CorrelationMatrix} class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationMatrix.class);

    /**
     * The original table supplied at construction time.
     */
    private final Table inputTable;

    /**
     * The generated correlation matrix, populated during construction.
     * <p>
     * Each dimension corresponds to a continuous column in the same order returned
     * by {@link Utils#getContinuousColumnNames(Table)}.
     */
    private final double[][] correlationMatrix;

    /**
     * Creates a correlation matrix generator for the given input table.
     * <p>
     * The correlation matrix is built eagerly and can be retrieved via
     * {@link #getCorrelationMatrix()}.
     *
     * @param inputTable the table to analyze; must not be {@code null}
     */
    public CorrelationMatrix(Table inputTable) {
        LOGGER.info("CorrelationMatrix::CorrelationMatrix Initializing correlation matrix for table with {} rows and {} columns", inputTable.rowCount(), inputTable.columnNames().size());
        this.inputTable = inputTable;
        this.correlationMatrix = this.constructCorrelationMatrix();
        LOGGER.info("CorrelationMatrix::CorrelationMatrix Correlation matrix initialized successfully");
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
     * Returns the computed Pearson correlation matrix.
     * <p>
     * The matrix is square with one row and column per continuous ({@code INTEGER}, {@code LONG},
     * {@code FLOAT}, or {@code DOUBLE}) column in the input table. Entry {@code [i][j]} is the correlation
     * between the {@code i}-th and {@code j}-th continuous columns.
     *
     * @return a two-dimensional array of pairwise correlation coefficients
     */
    public double[][] getCorrelationMatrix() {
        return correlationMatrix;
    }

    /**
     * Builds the Pearson correlation matrix for all continuous columns in the input table.
     * <p>
     * For each column, the mean and sum of squared deviations are computed while skipping
     * missing values. Pairwise correlations are then derived from the cross-products of
     * deviations, normalized by the product of each column's sum of squared deviations.
     *
     * @return a square correlation matrix indexed by continuous column order
     */
    private double[][] constructCorrelationMatrix() {
        // Metadata
        int variRows = 0;
        Table inputTable = this.inputTable;
        int rowCount = inputTable.rowCount();

        // Identifying continuous columns
        List<String> continuousColumnNames = Utils.getContinuousColumnNames(this.inputTable);
        int continuousColumnCount = continuousColumnNames.size();
        LOGGER.debug("CorrelationMatrix::constructCorrelationMatrix Building correlation matrix for {} continuous columns across {} rows", continuousColumnCount, rowCount);

        // Initializing working arrays for correlation computation
        double[] mean = new double[continuousColumnCount];
        double[] variance = new double[continuousColumnCount];
        double[] sumOfSquareOfDeviationFromMean = new double[continuousColumnCount];
        double[][] deviationFromMean = new double[continuousColumnCount][rowCount];
        double[][] correlationMatrix = new double[continuousColumnCount][continuousColumnCount];

        // Computing mean, deviation from mean, and variance for each continuous column
        for (int i = 0; i < continuousColumnCount; i++) {
            Column<?> column = inputTable.column(continuousColumnNames.get(i));
            String columnType = column.type().name();
            LOGGER.debug("CorrelationMatrix::constructCorrelationMatrix Processing column '{}' (type: {})", continuousColumnNames.get(i), columnType);
            variRows = 0;

            // Computing column mean from non-missing values
            for (int j = 0; j < rowCount; j++) {
                if (column.isMissing(j)) {
                    continue;
                }

                double value;
                switch (columnType) {
                    case "INTEGER":
                        value = inputTable.intColumn(continuousColumnNames.get(i)).getInt(j);
                        break;
                    case "LONG":
                        value = inputTable.longColumn(continuousColumnNames.get(i)).getLong(j);
                        break;
                    case "FLOAT":
                        value = inputTable.floatColumn(continuousColumnNames.get(i)).getFloat(j);
                        break;
                    case "DOUBLE":
                        value = inputTable.doubleColumn(continuousColumnNames.get(i)).getDouble(j);
                        break;
                    default:
                        LOGGER.warn("CorrelationMatrix::constructCorrelationMatrix Unsupported column type '{}' for column '{}'", columnType, continuousColumnNames.get(i));
                        value = 0.0;
                }

                mean[i] += value;
                variRows += 1;
            }

            if (variRows == 0) {
                // Column has no usable values; leave its row/column as NaN in the output matrix.
                mean[i] = Double.NaN;
                variance[i] = Double.NaN;
                sumOfSquareOfDeviationFromMean[i] = 0.0;
                continue;
            }

            mean[i] /= variRows;

            // Computing deviations and sum of squared deviations from mean
            for (int j = 0; j < rowCount; j++) {
                if (column.isMissing(j)) {
                    continue;
                }

                double value;
                switch (columnType) {
                    case "INTEGER":
                        value = inputTable.intColumn(continuousColumnNames.get(i)).getInt(j);
                        break;
                    case "LONG":
                        value = (double) inputTable.longColumn(continuousColumnNames.get(i)).getLong(j);
                        break;
                    case "FLOAT":
                        value = inputTable.floatColumn(continuousColumnNames.get(i)).getFloat(j);
                        break;
                    case "DOUBLE":
                        value = inputTable.doubleColumn(continuousColumnNames.get(i)).getDouble(j);
                        break;
                    default:
                        LOGGER.warn("CorrelationMatrix::constructCorrelationMatrix Unsupported column type '{}' for column '{}'", columnType, continuousColumnNames.get(i));
                        value = 0.0;
                }

                double deviation = value - mean[i];
                deviationFromMean[i][j] = deviation;
                variance[i] += deviation * deviation;
            }

            sumOfSquareOfDeviationFromMean[i] = variance[i];
            variance[i] = variRows > 1 ? (variance[i] / (variRows - 1)) : Double.NaN;
        }

        // Calculating cross product of deviations
        for (int i = 0; i < continuousColumnCount; i++) {
            for (int j = 0; j < continuousColumnCount; j++) {
                for (int k = 0; k < inputTable.rowCount(); k++) {
                    // Accumulate cross-product for this column pair.
                    correlationMatrix[i][j] += deviationFromMean[i][k] * deviationFromMean[j][k];
                }
            }
        }

        // Calculating and normalizing correlation matrix
        for (int i = 0; i < continuousColumnCount; i++) {
            for (int j = 0; j < continuousColumnCount; j++) {
                // Normalize by both columns' deviation magnitudes.
                double denominator = Math.sqrt(sumOfSquareOfDeviationFromMean[i] * sumOfSquareOfDeviationFromMean[j]);
                if (denominator == 0.0 || Double.isNaN(denominator)) {
                    correlationMatrix[i][j] = Double.NaN;
                } else {
                    correlationMatrix[i][j] /= denominator;
                }
            }
        }

        LOGGER.info("CorrelationMatrix::constructCorrelationMatrix Correlation matrix built with dimensions {}x{}", continuousColumnCount, continuousColumnCount);
        return correlationMatrix;
    }

    /**
     * Prints the computed Pearson correlation matrix to standard output.
     * <p>
     * Each output line represents one row of the matrix. Values within a row are
     * space-separated correlation coefficients. The matrix is square, with one
     * row and column per continuous ({@code INTEGER}, {@code LONG}, {@code FLOAT}, or
     * {@code DOUBLE}) column in the input table, in the same order used during matrix construction.
     * <p>
     * For programmatic access to the matrix without console output, use
     * {@link #getCorrelationMatrix()}.
     *
     * @see #getCorrelationMatrix()
     */
    public void printCorrelationMatrix() {
        double[][] correlationMatrix = this.correlationMatrix;
        int rows = correlationMatrix.length;
        List<String> continuousColumnNames = Utils.getContinuousColumnNames(this.inputTable);

        LOGGER.info("CorrelationMatrix::printCorrelationMatrix Printing correlation matrix with dimensions {}x{}", rows, rows);

        if (rows == 0) {
            System.out.println("(empty correlation matrix)");
            LOGGER.info("CorrelationMatrix::printCorrelationMatrix Correlation matrix is empty");
            return;
        }

        String[] labels = new String[rows];
        int rowLabelWidth = 4;
        for (int i = 0; i < rows; i++) {
            labels[i] = i < continuousColumnNames.size() ? continuousColumnNames.get(i) : "C" + (i + 1);
            rowLabelWidth = Math.max(rowLabelWidth, labels[i].length());
        }

        // Print table header once so each column can be identified quickly.
        System.out.printf(java.util.Locale.US, "%-" + rowLabelWidth + "s", "");
        for (int i = 0; i < rows; i++) {
            System.out.printf(java.util.Locale.US, " %12s", labels[i]);
        }
        System.out.println();

        // Print each row with fixed precision and aligned columns.
        for (int i = 0; i < rows; i++) {
            LOGGER.debug("CorrelationMatrix::printCorrelationMatrix Printing row {} of {}", i + 1, rows);
            System.out.printf(java.util.Locale.US, "%-" + rowLabelWidth + "s", labels[i]);

            for (int j = 0; j < rows; j++) {
                double value = correlationMatrix[i][j];
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.printf(java.util.Locale.US, " %12s", "NA");
                } else {
                    System.out.printf(java.util.Locale.US, " %12.4f", value);
                }
            }
            System.out.println();
        }

        LOGGER.info("CorrelationMatrix::printCorrelationMatrix Correlation matrix printed successfully");
    }
}
