import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlGenerator {
    public String generateSql(List<ExcelReader.TableInfo> tables) {
        Map<String, List<ExcelReader.TableInfo>> groupedTables = tables.stream()
                .collect(Collectors.groupingBy(ExcelReader.TableInfo::getTableName));

        return groupedTables.entrySet().stream()
                .map(this::createTableStatement)
                .collect(Collectors.joining("\n\n"));
    }

    private String createTableStatement(Map.Entry<String, List<ExcelReader.TableInfo>> entry) {
        String tableName = entry.getKey();
        List<ExcelReader.TableInfo> columns = entry.getValue();

        StringBuilder createStatement = new StringBuilder("CREATE TABLE " + tableName + " (\n");
        StringBuilder primaryKeyStatement = new StringBuilder();

        for (ExcelReader.TableInfo column : columns) {
            createStatement.append("    ").append(column.getColumnName())
                    .append(" ").append(column.getDataType());

            if (column.isPrimaryKey()) {
                primaryKeyStatement.append(column.getColumnName()).append(", ");
            } else {
                createStatement.append(",\n");
            }
        }

        if (primaryKeyStatement.length() > 0) {
            primaryKeyStatement.setLength(primaryKeyStatement.length() - 2); // remove trailing comma
            createStatement.append(",\n    PRIMARY KEY (").append(primaryKeyStatement).append(")\n");
        } else {
            createStatement.setLength(createStatement.length() - 2); // remove trailing comma from the last column
        }

        createStatement.append(");");
        return createStatement.toString();
    }
}
