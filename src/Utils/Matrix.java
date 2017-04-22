package Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mayur Kulkarni <mayurkulkarni012@gmail.com>
 */

public class Matrix {

    /**
     * Holds list of columns indexed by column name
     * name.
     */
    public Map<String, List> values;

    /**
     * column names
     */
    public List<String> columnList;

    private boolean independentColumnsComputed = false;
    private String dependentVariable;
    public long rowCount = 0;

    public String getDependentVariable() {
        if (dependentVariable == null) throw new IllegalStateException("Dependent variable not set!");
        return dependentVariable;
    }

    private List<String> independentColumns;

    public boolean isNumericalData = false;

    public Matrix() {
        values = new HashMap<>();
    }

    public List<String> getIndependentColumns() {
        if (independentColumns == null) throw new IllegalStateException("Independent column list null");
        if (!independentColumnsComputed) {
            independentColumnsComputed = true;
            this.independentColumns.remove(dependentVariable);
        }
        return independentColumns;
    }

    /**
     * Create matrix from CSV
     *
     * @param filePath      file path of CSV
     * @return              matrix created from CSV file
     * @throws IOException  if the CSV is not found
     */
    public static Matrix fromCSV(String filePath) throws IOException {
        Matrix matrix = new Matrix();
        String absPath = new java.io.File(".").getCanonicalPath();
        BufferedReader br = new BufferedReader(new FileReader(absPath + filePath));
        String currLine;
        String[] firstLine = br.readLine().split(",");
        for (int i = 0; i < firstLine.length; i++)
            matrix.values.put(String.valueOf(i), new ArrayList<>());
        for (int i = 0; i < firstLine.length; i++)
            matrix.values.get(String.valueOf(i)).add(firstLine[i]);
        long rowSize = 1;
        while ((currLine = br.readLine()) != null) {
            String[] splitLine = currLine.split(",");
            for (int i = 0; i < splitLine.length; i++)
                matrix.values.get(String.valueOf(i)).add(splitLine[i]);
            rowSize++;
        }
        matrix.columnList = new ArrayList<>(matrix.values.keySet());
        matrix.rowCount = rowSize;
        List<String> independentColumns = new ArrayList<>(matrix.columnList);
        independentColumns.remove(matrix.dependentVariable);
        matrix.independentColumns = independentColumns;
        return matrix;
    }

    public void attemptNumericalConversion() {
        if (!isNumericalData) {
            System.out.println("Attempting to convert data to numerical");
            try {
                System.out.println(getIndependentColumns());
                for (String col : getIndependentColumns()) {
                    List<String> currCol = values.get(col);
                    List<Double> modifiedList = currCol.stream()
                            .map(Double::parseDouble)
                            .collect(Collectors.toList());
                    values.replace(col, currCol, modifiedList);
                }
                isNumericalData = true;
                System.out.println("Numerical conversion successful");
            } catch (Exception e) {
                System.err.println("Numerical Conversion failed, some operations cannot be permitted");
                e.printStackTrace();
            }
        }
    }


    /** todo fix this noob toString
     * Print complete matrix
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Row size : ").append(rowCount).append("\n");
        for (int i = 0; i < rowCount; i++) {
            for (String str : columnList) {
                String currValue = String.valueOf(get(str, i));
                sb.append(currValue.length() > 13 ? currValue.substring(0, 13) : currValue).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Get column indexed by column name
     * @param columnName    column name
     * @return              List of values
     */
    public List getColumn(String columnName) {
        return values.get(columnName);
    }


    /**
     * Get a particular value in matrix
     * indexed by row and column
     * @param column    column name (String)
     * @param index     row integer index
     * @return          value at the column and row
     */
    public Object get(String column, int index) {
        if (column.equals("def")) return 1d;
        if (!values.keySet().contains(column) || values.get(column).size() <= index)
            throw new RuntimeException("Error fetching column : " + column + " row : " + index);
        return values.get(column).get(index);
    }

    /**
     * Set the current dependent variable
     * @param dependentVariable new dependent variable name
     * @throws RuntimeException if dependent variable is invalid
     */
    public void setDependentVariable(String dependentVariable) {
        if (!values.keySet().contains(dependentVariable))
            throw new RuntimeException("unknown Dependent variable : " + dependentVariable);
        this.dependentVariable = dependentVariable;
        independentColumnsComputed = false;
    }

    public void addColumn(String columnName, List vals) {
        if (values.keySet().contains(columnName))
            throw new RuntimeException("Can't add " + columnName + ". Value already present");
        columnList.add(columnName);
        getIndependentColumns().add(columnName);
        values.put(columnName, vals);
    }

    public void addColumn(String columnName, List vals, boolean dontAddIndependent) {
        if (values.keySet().contains(columnName))
            throw new RuntimeException("Can't add " + columnName + ". Value already present");
        columnList.add(columnName);
        values.put(columnName, vals);
    }

    public void addNDegrees(int n) {
        if (!isNumericalData) attemptNumericalConversion();
        MiscUtils.addNDegrees(this, n);
    }

    /**
     * Saves matrix to CSV
     * @param fileName fileName to save
     * @throws FileNotFoundException irrelevant
     * @throws UnsupportedEncodingException uses UTF-8 by default, so nothing to worry
     */
    public void toCSV(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        columnList.forEach((x) -> writer.print(x + ','));
        writer.println();
        for (int i = 0; i < this.rowCount; i++) {
            for (int j = 0; j < columnList.size(); j++) {
                if (j == columnList.size() - 1) writer.println(this.get(columnList.get(j), i));
                else {
                    writer.print(this.get(columnList.get(j), i));
                    writer.print(',');
                }
            }
        }
        writer.close();
    }
}

