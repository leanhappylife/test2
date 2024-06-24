是的，在Spring Boot中可以有多个实现了CommandLineRunner或ApplicationRunner接口的类。Spring Boot会在应用程序启动时调用所有这些实现的run方法。你可以使用这些类来执行各种启动后的初始化任务。

如果你有多个CommandLineRunner或ApplicationRunner实现，Spring Boot将按照它们在Spring应用上下文中的定义顺序调用它们。你也可以使用@Order注解或实现Ordered接口来指定调用顺序。

示例：多个CommandLineRunner类
下面是一个完整的示例，展示如何定义和使用多个CommandLineRunner实现。

项目结构
css
复制代码
spring-boot-multiple-commandlinerunner
├── src
│   └── main
│       ├── java
│       │   └── com
│       │       └── example
│       │           └── demo
│       │               ├── DemoApplication.java
│       │               ├── runner
│       │               │   ├── FirstStartupRunner.java
│       │               │   └── SecondStartupRunner.java
│       │               └── controller
│       │                   └── HelloController.java
│       └── resources
│           └── application.properties
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
1. 主应用程序类 DemoApplication.java
java
复制代码
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
2. 第一个 CommandLineRunner 实现 FirstStartupRunner.java
java
复制代码
package com.example.demo.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class FirstStartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("FirstStartupRunner is called.");
        // 这里可以执行你的初始化逻辑
    }
}
3. 第二个 CommandLineRunner 实现 SecondStartupRunner.java
java
复制代码
package com.example.demo.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class SecondStartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("SecondStartupRunner is called.");
        // 这里可以执行你的初始化逻辑
    }
}
4. 控制器类 HelloController.java
java
复制代码
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
5. 配置文件 application.properties
properties
复制代码
# src/main/resources/application.properties
# 这里可以配置Spring Boot的属性
6. Maven 配置文件 pom.xml
确保你有一个有效的 pom.xml，包括 Spring Boot Starter 依赖：

xml
复制代码
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>spring-boot-multiple-commandlinerunner</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>spring-boot-multiple-commandlinerunner</name>
    <description>Demo project for Spring Boot</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <java.version>11</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
运行应用程序
打开终端，导航到项目目录。
运行以下命令启动应用程序：
sh
复制代码
./mvnw spring-boot:run
测试
启动应用程序后，你应该会在控制台上看到以下输出，表示两个 CommandLineRunner 实现类的 run 方法被依次调用：

sh
复制代码
FirstStartupRunner is called.
SecondStartupRunner is called.
同时，你可以访问 http://localhost:8080/hello，应该会看到以下响应：

复制代码
Hello, World!
总结
你可以在Spring Boot中定义多个实现了CommandLineRunner或ApplicationRunner接口的类，并使用@Order注解来指定它们的执行顺序。在应用启动完成后，Spring Boot将按照定义的顺序依次调用它们的run方法。










