以下是完整的代码示例，包含所有相关的类和方法，并且已经在 UserCacheService 中添加了内存使用情况的打印功能，以监控缓存操作前后的内存变化。

1. User 实体类
java
复制代码
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;

    // 构造函数、getter 和 setter 方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}
2. UserRepository 接口
UserRepository 接口使用 Spring Data JPA 提供的数据访问功能。

java
复制代码
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 自定义查询，使用流式处理（Streaming）返回所有用户数据
    @Query("SELECT u FROM User u")
    Stream<User> findAllByCustomQueryAndStream();
}
3. UserService 类
UserService 类用于处理与 User 实体相关的业务逻辑，并在查询方法上添加 @Transactional(readOnly = true) 注解。

java
复制代码
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository; // 注入 UserRepository

    @PersistenceContext
    private EntityManager entityManager; // 注入 EntityManager，用于管理持久化上下文

    // 从数据库获取用户数据，并将其缓存到 Redis
    @Transactional(readOnly = true) // 事务只读模式
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        // 使用 UserRepository 查询数据库获取用户数据
        User user = userRepository.findById(userId).orElse(null);
        
        if (user != null) {
            // 将特定的用户对象从持久化上下文（一级缓存）中移除
            entityManager.detach(user);
            System.out.println("User detached from persistence context: " + userId);
        }
        
        return user; // 返回用户对象
    }
}
4. UserCacheService 类
UserCacheService 类负责在服务器启动时缓存所有用户的详细信息到 Redis 中，同时检查内存使用情况和缓存操作的耗时。在类级别上添加 @Transactional(readOnly = true) 注解，并在缓存操作前后打印内存使用情况。

java
复制代码
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true) // 在类级别上添加事务只读模式
public class UserCacheService {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final double MEMORY_THRESHOLD_PERCENTAGE = 0.40; // 内存剩余比例阈值
    private static final long MAX_CACHE_DURATION_MILLIS = 30 * 60 * 1000; // 最大缓存时长为30分钟（以毫秒为单位）

    // 在服务器启动时缓存所有用户的详细信息到 Redis 中
    @PostConstruct
    public void cacheAllUserDetailsOnStartup() {
        long startTime = System.currentTimeMillis(); // 获取开始时间

        try (Stream<User> userStream = userRepository.findAllByCustomQueryAndStream()) {
            userStream.forEach(user -> {
                if (!isMemoryAvailable()) {
                    System.out.println("Memory usage is too high. Stopping caching process.");
                    return; // 内存不足，停止缓存
                }

                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime > MAX_CACHE_DURATION_MILLIS) {
                    System.out.println("Caching process took too long (exceeded 30 minutes). Stopping caching process.");
                    return; // 如果耗时超过30分钟，停止缓存
                }

                // 打印缓存操作前的内存状态
                printMemoryUsage("Before caching user ID: " + user.getId());

                cacheUserDetail(user.getId());

                // 打印缓存操作后的内存状态
                printMemoryUsage("After caching user ID: " + user.getId());
            });
        }

        clearHibernateCache(); // 清除 Hibernate 一级缓存
        System.out.println("Caching process completed with available memory check and time limit.");
    }

    // 缓存单个用户详细信息到 Redis 中
    @CachePut(value = "userDetails", key = "#userId")
    public User cacheUserDetail(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    // 清除 Hibernate 一级缓存
    private void clearHibernateCache() {
        entityManager.clear();
        System.out.println("Hibernate Session cache cleared.");
    }

    // 检查服务器内存是否足够（剩余内存超过 40%）
    private boolean isMemoryAvailable() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // JVM 最大内存
        long allocatedMemory = runtime.totalMemory(); // 当前分配的内存
        long freeMemory = runtime.freeMemory(); // 当前 JVM 中可用的空闲内存
        long availableMemory = freeMemory + (maxMemory - allocatedMemory); // 计算实际可用内存

        double availableMemoryPercentage = (double) availableMemory / maxMemory; // 可用内存比例

        System.out.println("Available memory: " + (availableMemory / (1024 * 1024)) + " MB (" + (availableMemoryPercentage * 100) + "%)");

        return availableMemoryPercentage > MEMORY_THRESHOLD_PERCENTAGE; // 判断是否超过 40%
    }

    // 打印内存使用情况
    private void printMemoryUsage(String message) {
        Runtime runtime = Runtime.getRuntime();
        long allocatedMemory = runtime.totalMemory(); // 当前分配的内存
        long freeMemory = runtime.freeMemory(); // 当前 JVM 中可用的空闲内存
        long usedMemory = allocatedMemory - freeMemory; // 当前 JVM 中已用的内存

        System.out.println(message + " - Used memory: " + (usedMemory / (1024 * 1024)) + " MB");
    }
}
5. 配置类（可选，启用异步支持）
如果你想要使用异步操作，可以使用以下配置启用 Spring 的异步支持。

java
复制代码
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
}
总结
User 实体类：表示数据库中的用户表。
UserRepository 接口：定义了访问 User 数据库表的方法。
UserService 类：提供业务逻辑以获取用户数据，并在需要时将特定用户对象从持久化上下文中移除。
UserCacheService 类：负责在服务器启动时缓存所有用户详细信息到 Redis 中，同时检查内存使用情况和缓存操作的耗时，并在操作前后打印内存使用情况。
异步配置（可选）：启用异步操作支持以提高性能和响应速度。
这些代码组成了一个完整的 Spring Boot 应用程序，可以在服务器启动时缓存用户信息，同时管理内存使用和性能。
