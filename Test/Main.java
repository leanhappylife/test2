public class Main {
    public static void main(String[] args) {
        String excelFilePath = args.length > 0 ? args[0] : "path/to/your/excel/file.xlsx";

        ExcelReader excelReader = new ExcelReader(excelFilePath);
        List<ExcelReader.TableInfo> tableInfos = excelReader.readExcelFile();

        SqlGenerator sqlGenerator = new SqlGenerator();
        String sqlStatements = sqlGenerator.generateSql(tableInfos);

        System.out.println(sqlStatements);
    }
}
