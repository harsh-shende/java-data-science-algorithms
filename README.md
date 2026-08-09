# java-data-science-algorithms

## Overview

`java-data-science-algorithms` is a small Java/Maven project for working with tabular datasets using [Tablesaw](https://github.com/jtablesaw/tablesaw). It provides utility classes for:

- identifying categorical, temporal, and continuous columns,
- generating per-column statistical summaries,
- splitting a table into training, test, and validation subsets.

The project currently includes a simple `Main` class entry point and a set of reusable utilities that are intended to support data analysis and machine learning workflows on structured CSV data.

## What this project does

This project is focused on basic data preparation and exploratory analysis:

- **Column classification**: detects which columns are categorical, temporal, or continuous.
- **Table summarization**: computes missing-value counts, unique counts, and descriptive statistics for numeric columns.
- **Table splitting**: creates train/test splits and a validation subset from a `Tablesaw` table.
- **Dataset support**: includes multiple sample CSV datasets under `src/main/resources/datasets` for experimentation.

## Project metadata

| Item | Value                          |
|------|--------------------------------|
| Project artifact | `java-data-science-algorithms` |
| Group ID | `in.harshshende.datascience`   |
| Version | `1.0-SNAPSHOT`                 |
| Java version | `25`                           |
| Maven compiler source | `25`                           |
| Maven compiler target | `25`                           |
| Source encoding | `UTF-8`                        |

## Dependencies

The project uses the following Maven dependencies:

| Dependency | Version | Purpose |
|------------|---------|---------|
| `tech.tablesaw:tablesaw-core` | `0.40.0` | Core table/data-frame operations and CSV-style tabular analysis |
| `org.slf4j:slf4j-simple` | `2.0.16` | Simple logging backend for SLF4J |

## Project structure

```text
src/
  main/
    java/
      in/harshshende/datascience/
        Main.java
        util/
          Utils.java
          TableSummary.java
          TableSplitter.java
    resources/
      datasets/
        *.csv
  test/
    java/
```

### Package responsibilities

- **`in.harshshende.datascience`**
  - Contains the application entry point in `Main.java`.
  - `Main` currently prints a simple message and serves as the starting class for the project.

- **`in.harshshende.datascience.util`**
  - Contains reusable helper classes for table processing.
  - `Utils` provides column-type detection helpers.
  - `TableSummary` builds a statistical summary table for a `Tablesaw` table.
  - `TableSplitter` creates train, test, and validation subsets from a `Tablesaw` table.

## Notes

- The project is organized as a Maven application.
- Sample datasets are stored in `src/main/resources/datasets` and can be used for testing or analysis.
- The current `Main` class is a placeholder entry point; the reusable logic lives in the `util` package.

