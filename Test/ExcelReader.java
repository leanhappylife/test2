import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    private final String filePath;

    public ExcelReader(String filePath) {
        this.filePath = filePath;
    }

    public List<TableInfo> readExcelFile() {
        List<TableInfo> tables = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) { // skip the header row
                    continue;
                }
                String schema = getCellValue(row.getCell(0));
                String tableName = getCellValue(row.getCell(1));
                String primaryKey = getCellValue(row.getCell(2));
                String columnName = getCellValue(row.getCell(3));
                String dataType = getCellValue(row.getCell(4));

                TableInfo tableInfo = new TableInfo(schema, tableName, "YES".equalsIgnoreCase(primaryKey), columnName, dataType);
                tables.add(tableInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    public static class TableInfo {
        private final String schema;
        private final String tableName;
        private final boolean primaryKey;
        private final String columnName;
        private final String dataType;

        public TableInfo(String schema, String tableName, boolean primaryKey, String columnName, String dataType) {
            this.schema = schema;
            this.tableName = tableName;
            this.primaryKey = primaryKey;
            this.columnName = columnName;
            this.dataType = dataType;
        }

        public String getSchema() {
            return schema;
        }

        public String getTableName() {
            return tableName;
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getDataType() {
            return dataType;
        }
    }
}
