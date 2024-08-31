=====================
    import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class CsvExportUtil {

    @PersistenceContext  // 注入 EntityManager 用于清理缓存
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public <T, ID> void exportDataToCsv(JpaRepository<T, ID> repository, String fileName, String[] headers, Function<T, List<Object>> dataMapper) {
        int pageSize = 300; // 每页的记录数量更小，减少内存占用
        int pageNumber = 0; // 初始页码
        final int flushThreshold = 1000; // 每1000条记录刷新一次
        final int[] recordCount = {0};  // 记录已写入的记录数量

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false), 1024 * 1024);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<T> page = repository.findAll(pageable);

                // 将结果转换为可修改的集合
                List<T> entities = new ArrayList<>(page.getContent());

                if (entities.isEmpty()) {
                    break; // 没有更多数据时退出循环
                }

                // 将每一页的数据写入 CSV 文件
                for (T entity : entities) {
                    csvPrinter.printRecord(dataMapper.apply(entity));
                    recordCount[0]++;

                    // 达到阈值时刷新
                    if (recordCount[0] % flushThreshold == 0) {
                        csvPrinter.flush();
                    }
                }

                entities.clear(); // 清空当前批次的数据
                entityManager.clear(); // 清理 Hibernate 的一级缓存
                pageNumber++; // 读取下一页数据
            }

            csvPrinter.flush(); // 确保最后一批数据被写入
            System.out.println("CSV file created successfully: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

    ========================




import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class CsvExportUtil {

    @Autowired
    private EntityManager entityManager; // 注入 EntityManager 用于清理缓存

    @Transactional(readOnly = true)
    public <T, ID> void exportDataToCsv(JpaRepository<T, ID> repository, String fileName, String[] headers, Function<T, List<Object>> dataMapper) {
        int pageSize = 300; // 每页的记录数量更小，减少内存占用
        int pageNumber = 0; // 初始页码
        final int flushThreshold = 1000; // 每1000条记录刷新一次
        final int[] recordCount = {0};  // 记录已写入的记录数量

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false), 1024 * 1024);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<T> page = repository.findAll(pageable);

                // 将结果转换为可修改的集合
                List<T> entities = new ArrayList<>(page.getContent());

                if (entities.isEmpty()) {
                    break; // 没有更多数据时退出循环
                }

                // 将每一页的数据写入 CSV 文件
                for (T entity : entities) {
                    csvPrinter.printRecord(dataMapper.apply(entity));
                    recordCount[0]++;

                    // 达到阈值时刷新
                    if (recordCount[0] % flushThreshold == 0) {
                        csvPrinter.flush();
                    }
                }

                entities.clear(); // 清空当前批次的数据
                entityManager.clear(); // 清理 Hibernate 的一级缓存
                pageNumber++; // 读取下一页数据
            }

            csvPrinter.flush(); // 确保最后一批数据被写入
            System.out.println("CSV file created successfully: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




@Repository
public interface MyEntityWithEmbeddedIdRepository extends JpaRepository<MyEntityWithEmbeddedId, MyCompositeKey> {

    @Query("SELECT e FROM MyEntityWithEmbeddedId e")
    @QueryHints(value = {@QueryHint(name = "org.hibernate.cacheable", value = "false")})
    Stream<MyEntityWithEmbeddedId> streamAllEntities();
}


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Component
public class CsvExportUtil {

    @Autowired
    private EntityManager entityManager; // 注入 EntityManager 用于清理缓存

    /**
     * 通用的CSV导出方法
     *
     * @param repository  JPA 仓库，支持分页查询
     * @param fileName    输出的CSV文件名
     * @param headers     CSV文件的列标题
     * @param dataMapper  数据转换函数，用于将实体类数据转换为CSV写入格式
     * @param <T>         实体类型
     * @param <ID>        主键类型
     */
    @Transactional(readOnly = true)
    public <T, ID> void exportDataToCsv(JpaRepository<T, ID> repository, String fileName, String[] headers, Function<T, List<Object>> dataMapper) {
        int pageSize = 300; // 每页的记录数量更小，减少内存占用
        int pageNumber = 0; // 初始页码
        final int flushThreshold = 1000; // 每1000条记录刷新一次
        final int[] recordCount = {0};  // 记录已写入的记录数量

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false), 1024 * 1024);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            // 循环分页读取数据，直到没有更多数据
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<T> page = repository.findAll(pageable);
                List<T> entities = page.getContent();

                if (entities.isEmpty()) {
                    break; // 没有更多数据时退出循环
                }

                // 将每一页的数据写入 CSV 文件
                for (T entity : entities) {
                    csvPrinter.printRecord(dataMapper.apply(entity));
                    recordCount[0]++;

                    // 达到阈值时刷新
                    if (recordCount[0] % flushThreshold == 0) {
                        csvPrinter.flush();
                    }
                }

                entities.clear(); // 清空当前批次的数据
                entityManager.clear(); // 清理 Hibernate 的一级缓存
                pageNumber++; // 读取下一页数据
            }

            csvPrinter.flush(); // 确保最后一批数据被写入
            System.out.println("CSV file created successfully: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

以下是修改后的所有可执行的代码，针对可能的内存问题进行了优化，确保在处理大数据量时（例如百万条记录）更高效并减少内存占用。

1. 实体类 MyEntityWithEmbeddedId
首先，定义一个使用 @EmbeddedId 作为复合主键的实体类。

复合主键类 MyCompositeKey
java
复制代码
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class MyCompositeKey implements Serializable {
    private Long idPart1;
    private String idPart2;

    // 构造器、Getter 和 Setter 方法
    public MyCompositeKey() {
    }

    public MyCompositeKey(Long idPart1, String idPart2) {
        this.idPart1 = idPart1;
        this.idPart2 = idPart2;
    }

    public Long getIdPart1() {
        return idPart1;
    }

    public void setIdPart1(Long idPart1) {
        this.idPart1 = idPart1;
    }

    public String getIdPart2() {
        return idPart2;
    }

    public void setIdPart2(String idPart2) {
        this.idPart2 = idPart2;
    }

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyCompositeKey that = (MyCompositeKey) o;

        if (!idPart1.equals(that.idPart1)) return false;
        return idPart2.equals(that.idPart2);
    }

    @Override
    public int hashCode() {
        int result = idPart1.hashCode();
        result = 31 * result + idPart2.hashCode();
        return result;
    }
}
实体类 MyEntityWithEmbeddedId
java
复制代码
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class MyEntityWithEmbeddedId {
    @EmbeddedId
    private MyCompositeKey id; // 嵌入式复合主键
    private String name;
    private String email;
    private String country;

    // Getters and Setters
    public MyCompositeKey getId() {
        return id;
    }

    public void setId(MyCompositeKey id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
2. 仓库接口 MyEntityWithEmbeddedIdRepository
定义支持分页查询的仓库接口：

java
复制代码
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyEntityWithEmbeddedIdRepository extends JpaRepository<MyEntityWithEmbeddedId, MyCompositeKey> {
}
3. 通用 CSV 导出工具类 CsvExportUtil
优化后的 CsvExportUtil 类，增加了对大数据量处理的内存管理：

java
复制代码
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Component
public class CsvExportUtil {

    /**
     * 通用的CSV导出方法
     *
     * @param repository  JPA 仓库，支持分页查询
     * @param fileName    输出的CSV文件名
     * @param headers     CSV文件的列标题
     * @param dataMapper  数据转换函数，用于将实体类数据转换为CSV写入格式
     * @param <T>         实体类型
     * @param <ID>        主键类型
     */
    @Transactional(readOnly = true)
    public <T, ID> void exportDataToCsv(JpaRepository<T, ID> repository, String fileName, String[] headers, Function<T, List<Object>> dataMapper) {
        int pageSize = 300; // 每页的记录数量更小，减少内存占用
        int pageNumber = 0; // 初始页码
        final int flushThreshold = 1000; // 每1000条记录刷新一次
        final int[] recordCount = {0};  // 记录已写入的记录数量

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false), 1024 * 1024);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            // 循环分页读取数据，直到没有更多数据
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<T> page = repository.findAll(pageable);
                List<T> entities = page.getContent();

                if (entities.isEmpty()) {
                    break; // 没有更多数据时退出循环
                }

                // 将每一页的数据写入 CSV 文件
                for (T entity : entities) {
                    csvPrinter.printRecord(dataMapper.apply(entity));
                    recordCount[0]++;

                    // 达到阈值时刷新
                    if (recordCount[0] % flushThreshold == 0) {
                        csvPrinter.flush();
                    }
                }

                entities.clear(); // 清空当前批次的数据
                pageNumber++; // 读取下一页数据
            }

            csvPrinter.flush(); // 确保最后一批数据被写入
            System.out.println("CSV file created successfully: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
4. 服务类 CsvWriterEmbeddedIdService
使用通用的 CSV 导出工具类导出带有 @EmbeddedId 主键的实体数据：

java
复制代码
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CsvWriterEmbeddedIdService {

    @Autowired
    private MyEntityWithEmbeddedIdRepository myEntityWithEmbeddedIdRepository;

    @Autowired
    private CsvExportUtil csvExportUtil;

    public void exportDataToCsv(String fileName) {
        String[] headers = {"ID Part 1", "ID Part 2", "Name", "Email", "Country"};

        csvExportUtil.exportDataToCsv(
                myEntityWithEmbeddedIdRepository, // 传入特定的Repository
                fileName,
                headers,
                // 数据转换函数，将 MyEntityWithEmbeddedId 对象转换为 CSV 行
                entity -> Arrays.asList(
                        entity.getId().getIdPart1(),    // 获取嵌入式主键的部分1
                        entity.getId().getIdPart2(),    // 获取嵌入式主键的部分2
                        entity.getName(),
                        entity.getEmail(),
                        entity.getCountry()
                )
        );
    }
}
5. 控制器类 MyEntityController
定义一个控制器来触发 CSV 导出操作：

java
复制代码
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyEntityController {

    @Autowired
    private CsvWriterEmbeddedIdService csvWriterEmbeddedIdService;

    @GetMapping("/export")
    public String exportData() {
        csvWriterEmbeddedIdService.exportDataToCsv("output.csv");
        return "Data export started.";
    }
}
6. Maven 依赖
确保在 pom.xml 中添加 Apache Commons CSV 的依赖：

xml
复制代码
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version> <!-- 使用最新的稳定版本 -->
</dependency>
关键优化点
分页查询：

每次分页查询的数量减少到 300 条，以减少内存占用。
批量刷新：

每 1000 条记录刷新一次，减少频繁 I/O 操作对性能的影响。
及时清理数据：

处理完每一页数据后，清空集合 entities.clear()，释放内存。
调整 JVM 内存配置：

配置 JVM 堆内存，以提供足够的内存空间进行大数据处理。
总结
通过这些优化措施，修改后的代码能够更高效地处理大数据量的导出操作，减少内存使用，防止内存溢出，并确保程序在有限的内存环境中稳定运行
