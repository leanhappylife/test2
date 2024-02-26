<dependencies>
    <!-- Apache POI dependency for Excel file operations -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.3</version> <!-- Use the latest version available -->
    </dependency>
</dependencies>


启动 IntelliJ IDEA 并创建一个新的 Maven 项目：

选择 "Create New Project"。
在左侧面板选择 "Maven"，不选择任何 archetype。
点击 "Next"，填写 GroupId 和 ArtifactId，例如 com.example 和 SQLTableGenerator。
点击 "Next" 然后 "Finish"。
配置 pom.xml 添加 Apache POI 依赖：

xml
Copy code
<dependencies>
    <!-- Apache POI dependency for Excel file operations -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.3</version> <!-- Use the latest version available -->
    </dependency>
</dependencies>
创建主 Java 类：

在 src/main/java 下创建一个新的 Java 类，例如 SQLTableGenerator.java。
复制并粘贴上述 Java 代码到新创建的类文件中。
配置运行参数：

在 IDEA 中，选择 "Run" -> "Edit Configurations"。
点击 "+" 并选择 "Application"。
设置 "Name" 为你想要的运行配置名称。
设置 "Main class" 为你的主类全称，例如 com.example.SQLTableGenerator。
在 "Program arguments" 中，如果你的程序需要，指定 Excel 文件的路径。
点击 "Apply" 和 "OK"。
运行项目：

点击运行按钮或使用快捷键 Shift + F10。
以下是完整的 SQLTableGenerator.java 示例代码，用于生成 SQL 创建表的语句：

java
Copy code
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class SQLTableGenerator {

    public static void main(String[] args) {
        try {
            if(args.length < 1) {
                System.out.println("Please provide the Excel file path as a command-line argument.");
                return;
            }

            String excelFilePath = args[0];
            FileInputStream inputStream = new FileInputStream(excelFilePath);

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = firstSheet.iterator();

            Map<String, List<String>> tableColumns = new HashMap<>();
            Map<String, String> primaryKeys = new HashMap<>();

            while (rowIterator.hasNext()) {
                Row nextRow = rowIterator.next();
                if (nextRow.getRowNum() == 0) continue; // skip header

                String schema = nextRow.getCell(0).getStringCellValue();
                String tableName = nextRow.getCell(1).getStringCellValue();
                boolean isPrimaryKey = "YES".equalsIgnoreCase(nextRow.getCell(2).getStringCellValue());
                String columnName = nextRow.getCell(3).getStringCellValue();
                String dataType = nextRow.getCell(4).getStringCellValue();

                String fullTableName = schema + "." + tableName;
                String columnDefinition = columnName + " " + dataType;

                // Add column definition to table
                tableColumns.computeIfAbsent(fullTableName, k -> new ArrayList<>()).add(columnDefinition);

                // Define primary key
                if (isPrimaryKey) {
                    primaryKeys.put(fullTableName, columnName);
                }
            }

            // Close resources
            workbook.close();
            inputStream.close();

            // Generate and print SQL statements
            tableColumns.forEach((tableName, columns) -> {
                String primaryKey = primaryKeys.get(tableName);
                System.out.println(generateCreateTableSQL(tableName, columns, primaryKey));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateCreateTableSQL(String tableName, List<String> columns, String primaryKey) {
        StringJoiner columnsJoiner = new StringJoiner(",\n    ");
        columns.forEach(columnsJoiner::add);
        if (primaryKey != null) {
            columnsJoiner.add("PRIMARY KEY (" + primaryKey + ")");
        }

        return "CREATE TABLE " + tableName + " (\n    " + columnsJoiner.toString() + "\n);";
    }
}
将上述 Maven 依赖添加到你的 pom.xml 文件中，并将 Java 代码添加到你的主类中。然后，你可以通过 IntelliJ IDEA 运行你的 Maven 项目。确保你的 Excel 文件路径已经作为命令行参数提供。
