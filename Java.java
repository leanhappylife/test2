import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class SQLTableGenerator {

    private static final String YES = "YES";

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Please provide the Excel file path as a command-line argument.");
                return;
            }

            String excelFilePath = args[0];
            FileInputStream inputStream = new FileInputStream(excelFilePath);

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = firstSheet.iterator();

            Map<String, List<String>> tableColumns = new LinkedHashMap<>();
            Map<String, Set<String>> primaryKeys = new LinkedHashMap<>();

            while (rowIterator.hasNext()) {
                Row nextRow = rowIterator.next();
                if (nextRow.getRowNum() == 0) continue; // skip header

                // Get cell values with null checks
                String schema = getCellValue(nextRow.getCell(0));
                String tableName = getCellValue(nextRow.getCell(1));
                String columnName = getCellValue(nextRow.getCell(3));
                String dataType = getCellValue(nextRow.getCell(4));
                boolean isPrimaryKey = YES.equals(getCellValue(nextRow.getCell(2)));

                String fullTableName = schema + "." + tableName;
                String columnDefinition = columnName + " " + dataType;

                // Add column definition to table
                tableColumns.computeIfAbsent(fullTableName, k -> new ArrayList<>()).add(columnDefinition);

                // If it's a primary key, add it to the set of primary keys
                if (isPrimaryKey) {
                    primaryKeys.computeIfAbsent(fullTableName, k -> new LinkedHashSet<>()).add(columnName);
                }
            }

            workbook.close();
            inputStream.close();

            // Generate and print SQL statements
            tableColumns.forEach((tableName, columns) -> {
                Set<String> pks = primaryKeys.get(tableName);
                System.out.println(generateCreateTableSQL(tableName, columns, pks));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateCreateTableSQL(String tableName, List<String> columns, Set<String> primaryKeys) {
        StringBuilder createTableStatement = new StringBuilder();
        createTableStatement.append("CREATE TABLE ").append(tableName).append(" (\n");

        StringJoiner columnJoiner = new StringJoiner(",\n    ");
        for (String column : columns) {
            columnJoiner.add(column);
        }

        // Add primary key constraint if primary keys exist
        if (primaryKeys != null && !primaryKeys.isEmpty()) {
            StringJoiner pkJoiner = new StringJoiner(", ");
            primaryKeys.forEach(pkJoiner::add);
            columnJoiner.add("PRIMARY KEY (" + pkJoiner.toString() + ")");
        }

        createTableStatement.append(columnJoiner.toString());
        createTableStatement.append("\n);");

        return createTableStatement.toString();
    }

    private static String getCellValue(Cell cell) {
        return cell == null ? "" : cell.getStringCellValue();
    }
}
