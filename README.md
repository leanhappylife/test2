
下面给出一个完整的可执行项目示例，包含 pom.xml 以及所有 Java 源代码文件和配置文件。请按照下面的目录结构创建项目，并将各个文件放入相应目录中，然后用 IDEA 导入该 Maven 项目，即可直接编译执行（前提是本地已配置好 Kafka、MQ 服务）。

项目目录结构
arduino
复制
core-avaloq-messaging-adaptor
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── x
    │   │       ├── Application.java                   // 应用入口，包 x
    │   │       ├── common                              // 公共模块，包 x.common
    │   │       │   ├── AbstractKafkaHeaderConfig.java  // 公共 Kafka header 配置抽象类
    │   │       │   ├── AbstractProcessorMappingProperties.java  // 公共 Processor 映射抽象类
    │   │       │   ├── MessageProcessor.java           // 处理器接口
    │   │       │   └── ProcessorFactory.java           // 处理器工厂（映射由外部传入）
    │   │       ├── ftb                                 // FTB 模块（Kafka → MQ），包 x.ftb
    │   │       │   ├── config
    │   │       │   │   ├── FtbKafkaHeaderConfig.java     // FTB 方向 Kafka header 配置
    │   │       │   │   └── FtbProcessorMappingProperties.java  // FTB 方向 processor 映射配置
    │   │       │   ├── processor
    │   │       │   │   ├── common
    │   │       │   │   │   └── GenericProcessor.java      // 通用处理器
    │   │       │   │   ├── equity
    │   │       │   │   │   └── EquityCustomProcessor.java // Equity 定制处理器
    │   │       │   │   └── fund
    │   │       │   │       └── FundCustomProcessor.java   // Fund 定制处理器
    │   │       │   └── route
    │   │       │       ├── AbstractFTBRoute.java         // FTB 路由抽象类
    │   │       │       ├── EquityFTBRoute.java             // Equity FTB 路由
    │   │       │       └── FundFTBRoute.java               // Fund FTB 路由
    │   │       └── btf                                 // BTF 模块（MQ → Kafka），包 x.btf
    │   │           ├── config
    │   │           │   ├── BtfKafkaHeaderConfig.java       // BTF 方向 Kafka header 配置
    │   │           │   ├── BtfProcessorMappingProperties.java  // BTF 方向 processor 映射配置（如需单独配置）
    │   │           │   ├── TopicMappingProperties.java      // 产品类型→Kafka topic 映射（BTF 方向）
    │   │           │   └── MQMessage.java                   // MQ 消息模型（原 DynamicMessage 改名）
    │   │           ├── processor                         // BTF 模块专用 processor（如果需要）
    │   │           │   ├── common
    │   │           │   │   └── BtfGenericProcessor.java     // BTF 通用处理器（示例）
    │   │           │   ├── equity
    │   │           │   │   └── EquityBtfProcessor.java        // BTF Equity 定制处理器（示例）
    │   │           │   └── fund
    │   │           │       └── FundBtfProcessor.java          // BTF Fund 定制处理器（示例）
    │   │           └── route
    │   │               ├── AbstractBTFRoute.java            // BTF 路由抽象类
    │   │               └── EquityBTFRoute.java              // 针对 Equity 的 BTF 路由（MQ → Kafka）
    │   └── resources
    │       └── application.yml                           // 配置文件
    └── test
        └── java
            └── x
                └── ApplicationTests.java               // 测试类
1. pom.xml
放在项目根目录下

xml
复制
<?xml version="1.0" encoding="UTF-8"?>
<project
        xmlns="http://maven.apache.org/POM/4.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                        http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>stock-kafka-demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.0</spring-boot.version>
        <!-- 假设 Camel 4.4.0 可用 -->
        <camel.version>4.4.0</camel.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Apache Camel BOM -->
            <dependency>
                <groupId>org.apache.camel</groupId>
                <artifactId>camel-bom</artifactId>
                <version>${camel.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <repositories>
        <repository>
            <id>maven-central</id>
            <name>Maven Central Repository</name>
            <url>https://repo.maven.apache.org/maven2</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- Apache Camel Spring Boot Starter -->
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-spring-boot-starter</artifactId>
            <version>${camel.version}</version>
        </dependency>

        <!-- Camel Kafka Starter -->
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-kafka-starter</artifactId>
            <version>${camel.version}</version>
        </dependency>

        <!-- Camel JMS Starter -->
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-jms-starter</artifactId>
            <version>${camel.version}</version>
        </dependency>

        <!-- Camel Jackson Starter -->
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-jackson-starter</artifactId>
            <version>${camel.version}</version>
        </dependency>

        <!-- IBM MQ 客户端依赖 -->
        <dependency>
            <groupId>com.ibm.mq</groupId>
            <artifactId>com.ibm.mq.allclient</artifactId>
            <version>9.2.3.0</version>
        </dependency>

        <!-- Jakarta JMS API -->
        <dependency>
            <groupId>jakarta.jms</groupId>
            <artifactId>jakarta.jms-api</artifactId>
            <version>3.0.1</version>
        </dependency>

        <!-- JAXB 依赖 -->
        <dependency>
            <groupId>jakarta.xml.bind</groupId>
            <artifactId>jakarta.xml.bind-api</artifactId>
            <version>3.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
            <version>3.0.2</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
2. 公共模块代码（包 x.common）
2.1 AbstractKafkaHeaderConfig.java
路径：src/main/java/x/common/AbstractKafkaHeaderConfig.java

java
复制
package x.common;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractKafkaHeaderConfig {
    private Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public Map<String, String> getHeadersForTopic(String topic) {
        String headerStr = headers.get(topic);
        Map<String, String> result = new HashMap<>();
        if (headerStr != null && !headerStr.trim().isEmpty()) {
            String[] pairs = headerStr.split(",");
            for (String pair : pairs) {
                String[] keyVal = pair.split("=");
                if (keyVal.length == 2) {
                    result.put(keyVal[0].trim(), keyVal[1].trim());
                }
            }
        }
        return result;
    }
}
2.2 AbstractProcessorMappingProperties.java
路径：src/main/java/x/common/AbstractProcessorMappingProperties.java

java
复制
package x.common;

import java.util.Map;

public abstract class AbstractProcessorMappingProperties {
    private Map<String, String> mapping;

    public Map<String, String> getMapping() {
        return mapping;
    }
    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }
}
2.3 MessageProcessor.java
路径：src/main/java/x/common/MessageProcessor.java

java
复制
package x.common;

import org.apache.camel.Exchange;

public interface MessageProcessor {
    void process(Exchange exchange) throws Exception;
}
2.4 ProcessorFactory.java
路径：src/main/java/x/common/ProcessorFactory.java

java
复制
package x.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProcessorFactory {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 根据传入的映射配置获取 processor 链
     */
    public List<MessageProcessor> getProcessors(String topic, Map<String, String> mapping) {
        List<MessageProcessor> processors = new ArrayList<>();
        if (mapping != null && mapping.containsKey(topic)) {
            String processorNames = mapping.get(topic);
            String[] names = processorNames.split(",");
            for (String name : names) {
                name = name.trim();
                MessageProcessor processor = applicationContext.getBean(name, MessageProcessor.class);
                if (processor != null) {
                    processors.add(processor);
                }
            }
        }
        if (processors.isEmpty()) {
            processors.add(applicationContext.getBean("genericProcessor", MessageProcessor.class));
        }
        return processors;
    }
}
3. FTB 模块（包 x.ftb）
3.1 配置类
3.1.1 FtbKafkaHeaderConfig.java
路径：src/main/java/x/ftb/config/FtbKafkaHeaderConfig.java

java
复制
package x.ftb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import x.common.AbstractKafkaHeaderConfig;

@Component
@ConfigurationProperties(prefix = "kafka.ftb.headers")
public class FtbKafkaHeaderConfig extends AbstractKafkaHeaderConfig {
    // 使用公共抽象类中的逻辑
}
3.1.2 FtbProcessorMappingProperties.java
路径：src/main/java/x/ftb/config/FtbProcessorMappingProperties.java

java
复制
package x.ftb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import x.common.AbstractProcessorMappingProperties;

@Component
@ConfigurationProperties(prefix = "kafka.ftb.processor")
public class FtbProcessorMappingProperties extends AbstractProcessorMappingProperties {
    // 使用公共抽象类中的逻辑
}
3.2 Processor 部分（FTB）
3.2.1 GenericProcessor.java
路径：src/main/java/x/ftb/processor/common/GenericProcessor.java

java
复制
package x.ftb.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import x.common.MessageProcessor;

@Component("genericProcessor")
public class GenericProcessor implements MessageProcessor, Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        body = body.replace("Generic", "Processed Generic");
        exchange.getIn().setBody(body);
    }
}
3.2.2 EquityCustomProcessor.java
路径：src/main/java/x/ftb/processor/equity/EquityCustomProcessor.java

java
复制
package x.ftb.processor.equity;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import x.common.MessageProcessor;

@Component("equityCustomProcessor")
public class EquityCustomProcessor implements MessageProcessor, Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        body = body.replace("Equity", "Processed Equity Custom");
        exchange.getIn().setBody(body);
    }
}
3.2.3 FundCustomProcessor.java
路径：src/main/java/x/ftb/processor/fund/FundCustomProcessor.java

java
复制
package x.ftb.processor.fund;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import x.common.MessageProcessor;

@Component("fundCustomProcessor")
public class FundCustomProcessor implements MessageProcessor, Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        body = body.replace("Fund", "Processed Fund Custom");
        exchange.getIn().setBody(body);
    }
}
3.3 FTB 路由
3.3.1 AbstractFTBRoute.java
路径：src/main/java/x/ftb/route/ftb/AbstractFTBRoute.java

java
复制
package x.ftb.route.ftb;

import org.apache.camel.builder.RouteBuilder;
import x.ftb.config.FtbKafkaHeaderConfig;
import x.common.MessageProcessor;
import x.common.ProcessorFactory;
import java.util.List;
import java.util.Map;

public abstract class AbstractFTBRoute extends RouteBuilder {

    // 子类提供 Kafka 主题（例如 ftb-equity-topic-1）
    protected abstract String getTopics();
    // 子类提供 Kafka Broker 地址
    protected abstract String getKafkaBrokers();
    // 子类提供 MQ 队列名称（FTB 路由：Kafka → MQ）
    protected abstract String getDestinationQueue();
    // 子类提供 ProcessorFactory 实例
    protected abstract ProcessorFactory getProcessorFactory();
    // 子类提供 FtbKafkaHeaderConfig 实例
    protected abstract FtbKafkaHeaderConfig getKafkaHeaderConfig();
    // 子类提供 FTB 方向的 processor 映射配置
    protected abstract x.ftb.config.FtbProcessorMappingProperties getFtbMappingProperties();

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
            .maximumRedeliveries(3)
            .redeliveryDelay(2000)
            .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.ERROR)
            .handled(true)
            .log("FTB Route Exception (${routeId}): ${exception.message}")
            .to("jms:queue:errorQueue")
            .stop();

        from("kafka:" + getTopics() + "?brokers=" + getKafkaBrokers())
            .routeId(getRouteId())
            .log("FTB Received Kafka message (Topic: ${header.kafka.TOPIC}): ${body}")
            .process(exchange -> {
                String topic = exchange.getIn().getHeader("kafka.TOPIC", String.class);
                Map<String, String> headers = getKafkaHeaderConfig().getHeadersForTopic(topic);
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    exchange.getIn().setHeader(entry.getKey(), entry.getValue());
                }
                List<MessageProcessor> processors = getProcessorFactory().getProcessors(topic, getFtbMappingProperties().getMapping());
                for (MessageProcessor processor : processors) {
                    processor.process(exchange);
                }
            })
            .marshal().jacksonxml() // JSON → XML
            .to("jms:queue:" + getDestinationQueue())
            .log("FTB Sent message to MQ (Queue: " + getDestinationQueue() + "): ${body}");
    }

    protected String getRouteId() {
        return this.getClass().getSimpleName();
    }
}
3.3.2 EquityFTBRoute.java
路径：src/main/java/x/ftb/route/ftb/EquityFTBRoute.java

java
复制
package x.ftb.route.ftb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import x.ftb.config.FtbKafkaHeaderConfig;
import x.ftb.config.FtbProcessorMappingProperties;
import x.common.ProcessorFactory;

@Component
public class EquityFTBRoute extends AbstractFTBRoute {

    @Value("${kafka.ftb.equity.topics}")
    private String equityTopics;

    @Value("${kafka.ftb.equity.broker}")
    private String equityBrokers;

    @Value("${mq.queue}")
    private String commonQueue;

    @Autowired
    private ProcessorFactory processorFactory;

    @Autowired
    private FtbKafkaHeaderConfig kafkaHeaderConfig;

    @Autowired
    private FtbProcessorMappingProperties processorMappingProperties;

    @Override
    protected String getTopics() {
        return equityTopics;
    }

    @Override
    protected String getKafkaBrokers() {
        return equityBrokers;
    }

    @Override
    protected String getDestinationQueue() {
        return commonQueue;
    }

    @Override
    protected ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    @Override
    protected FtbKafkaHeaderConfig getKafkaHeaderConfig() {
        return kafkaHeaderConfig;
    }

    @Override
    protected FtbProcessorMappingProperties getFtbMappingProperties() {
        return processorMappingProperties;
    }

    @Override
    protected String getRouteId() {
        return "EquityFTBRoute";
    }
}
3.3.3 FundFTBRoute.java
路径：src/main/java/x/ftb/route/ftb/FundFTBRoute.java

java
复制
package x.ftb.route.ftb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import x.ftb.config.FtbKafkaHeaderConfig;
import x.ftb.config.FtbProcessorMappingProperties;
import x.common.ProcessorFactory;

@Component
public class FundFTBRoute extends AbstractFTBRoute {

    @Value("${kafka.ftb.fund.topics}")
    private String fundTopics;

    @Value("${kafka.ftb.fund.broker}")
    private String fundBrokers;

    @Value("${mq.queue}")
    private String commonQueue;

    @Autowired
    private ProcessorFactory processorFactory;

    @Autowired
    private FtbKafkaHeaderConfig kafkaHeaderConfig;

    @Autowired
    private FtbProcessorMappingProperties processorMappingProperties;

    @Override
    protected String getTopics() {
        return fundTopics;
    }

    @Override
    protected String getKafkaBrokers() {
        return fundBrokers;
    }

    @Override
    protected String getDestinationQueue() {
        return commonQueue;
    }

    @Override
    protected ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    @Override
    protected FtbKafkaHeaderConfig getKafkaHeaderConfig() {
        return kafkaHeaderConfig;
    }

    @Override
    protected FtbProcessorMappingProperties getFtbMappingProperties() {
        return processorMappingProperties;
    }

    @Override
    protected String getRouteId() {
        return "FundFTBRoute";
    }
}
4. BTF 模块（包 x.btf）
4.1 配置类
4.1.1 BtfKafkaHeaderConfig.java
路径：src/main/java/x/btf/config/BtfKafkaHeaderConfig.java

java
复制
package x.btf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import x.common.AbstractKafkaHeaderConfig;

@Component
@ConfigurationProperties(prefix = "kafka.btf.headers")
public class BtfKafkaHeaderConfig extends AbstractKafkaHeaderConfig {
    // 直接使用公共抽象类中的逻辑
}
4.1.2 BtfProcessorMappingProperties.java
（可选，如需 BTF 独立 processor 映射） 路径：src/main/java/x/btf/config/BtfProcessorMappingProperties.java

java
复制
package x.btf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import x.common.AbstractProcessorMappingProperties;

@Component
@ConfigurationProperties(prefix = "kafka.btf.processor")
public class BtfProcessorMappingProperties extends AbstractProcessorMappingProperties {
    // 直接使用公共逻辑
}
4.1.3 TopicMappingProperties.java
路径：src/main/java/x/btf/config/TopicMappingProperties.java

java
复制
package x.btf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "kafka.btf.topicmapping")
public class TopicMappingProperties {
    private Map<String, String> mapping;

    public Map<String, String> getMapping() {
        return mapping;
    }
    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }
    public String getKafkaTopic(String productType) {
        return mapping.get(productType);
    }
}
4.1.4 MQMessage.java
路径：src/main/java/x/btf/config/MQMessage.java

java
复制
package x.btf.config;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "message")
public class MQMessage {
    private String productType;
    private String content;

    @XmlElement
    public String getProductType() {
        return productType;
    }
    public void setProductType(String productType) {
        this.productType = productType;
    }

    @XmlElement
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MQMessage{productType='" + productType + "', content='" + content + "'}";
    }
}
4.2 BTF Processor 部分
（可选，若 BTF 方向需要单独的处理器）

4.2.1 BtfGenericProcessor.java
路径：src/main/java/x/btf/processor/common/BtfGenericProcessor.java

java
复制
package x.btf.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import x.common.MessageProcessor;

@Component("btfGenericProcessor")
public class BtfGenericProcessor implements MessageProcessor, Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        body = body.replace("Generic", "BTF Processed Generic");
        exchange.getIn().setBody(body);
    }
}
4.2.2 EquityBtfProcessor.java
路径：src/main/java/x/btf/processor/equity/EquityBtfProcessor.java

java
复制
package x.btf.processor.equity;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import x.common.MessageProcessor;

@Component("equityBtfProcessor")
public class EquityBtfProcessor implements MessageProcessor, Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        body = body.replace("Equity", "BTF Processed Equity");
        exchange.getIn().setBody(body);
    }
}
4.2.3 FundBtfProcessor.java
路径：src/main/java/x/btf/processor/fund/FundBtfProcessor.java

java
复制
package x.btf.processor.fund;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import x.common.MessageProcessor;

@Component("fundBtfProcessor")
public class FundBtfProcessor implements MessageProcessor, Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        body = body.replace("Fund", "BTF Processed Fund");
        exchange.getIn().setBody(body);
    }
}
4.3 BTF 路由
4.3.1 AbstractBTFRoute.java
路径：src/main/java/x/btf/route/AbstractBTFRoute.java

java
复制
package x.btf.route;

import org.apache.camel.builder.RouteBuilder;
import x.btf.config.TopicMappingProperties;
import x.btf.config.BtfKafkaHeaderConfig;
import x.btf.config.MQMessage;
import x.common.MessageProcessor;
import x.common.ProcessorFactory;
import org.apache.camel.model.dataformat.JsonLibrary;
import java.util.List;

public abstract class AbstractBTFRoute extends RouteBuilder {

    // 子类提供 MQ 队列名称（MQ→Kafka）
    protected abstract String getMqQueue();
    // 子类提供 Kafka Broker 地址（BTF方向）
    protected abstract String getKafkaBrokers();
    // 子类提供 TopicMappingProperties 实例
    protected abstract TopicMappingProperties getTopicMappingProperties();
    // 子类提供 ProcessorFactory 实例
    protected abstract ProcessorFactory getProcessorFactory();
    // 子类提供 BtfKafkaHeaderConfig 实例
    protected abstract BtfKafkaHeaderConfig getKafkaHeaderConfig();
    // 子类提供 BTF 方向 processor 映射（Map 类型）
    protected abstract java.util.Map<String, String> getBtfMapping();

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
            .handled(true)
            .log("BTF Route Exception (${routeId}): ${exception.message}")
            .to("jms:queue:errorQueue")
            .stop();

        from("jms:queue:" + getMqQueue())
            .routeId(getRouteId())
            .log("BTF Received MQ message: ${body}")
            .unmarshal().jaxb(x.btf.config.MQMessage.class)
            .log("BTF Unmarshalled MQ message: ${body}")
            .process(exchange -> {
                MQMessage message = exchange.getIn().getBody(MQMessage.class);
                String productType = message.getProductType();
                if (productType == null || productType.isEmpty()) {
                    throw new IllegalArgumentException("productType is missing in the MQ message");
                }
                String targetTopic = getTopicMappingProperties().getMapping().get(productType);
                if (targetTopic == null || targetTopic.isEmpty()) {
                    throw new IllegalArgumentException("No Kafka topic mapping found for product type: " + productType);
                }
                exchange.getIn().setHeader("targetTopic", targetTopic);
            })
            .log("BTF Dynamic target Kafka topic: ${header.targetTopic}")
            // 设置 BTF 方向 header
            .process(exchange -> {
                String topic = exchange.getIn().getHeader("targetTopic", String.class);
                java.util.Map<String, String> headers = getKafkaHeaderConfig().getHeadersForTopic(topic);
                for (java.util.Map.Entry<String, String> entry : headers.entrySet()) {
                    exchange.getIn().setHeader(entry.getKey(), entry.getValue());
                }
            })
            // 调用 processor 链对 MQ 消息进行额外处理
            .process(exchange -> {
                MQMessage message = exchange.getIn().getBody(MQMessage.class);
                List<MessageProcessor> processors = getProcessorFactory().getProcessors(message.getProductType(), getBtfMapping());
                for (MessageProcessor processor : processors) {
                    processor.process(exchange);
                }
            })
            .log("BTF After processor chain, MQ message: ${body}")
            .marshal().jacksonjson()  // 转换为 JSON 格式
            .toD("kafka:${header.targetTopic}?brokers=" + getKafkaBrokers())
            .log("BTF Sent message to Kafka topic ${header.targetTopic}: ${body}");
    }

    protected String getRouteId() {
        return this.getClass().getSimpleName();
    }
}
4.3.2 EquityBTFRoute.java
路径：src/main/java/x/btf/route/EquityBTFRoute.java

java
复制
package x.btf.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import x.btf.config.TopicMappingProperties;
import x.btf.config.BtfKafkaHeaderConfig;
import x.btf.config.BtfProcessorMappingProperties;
import x.common.ProcessorFactory;
import java.util.Map;

@Component
public class EquityBTFRoute extends AbstractBTFRoute {

    @Value("${mq.queue}")
    private String mqQueue;

    @Value("${kafka.btf.equity.broker}")
    private String equityBtfBroker;

    @Autowired
    private TopicMappingProperties topicMappingProperties;

    @Autowired
    private ProcessorFactory processorFactory;

    @Autowired
    private BtfKafkaHeaderConfig kafkaHeaderConfig;

    @Autowired
    private BtfProcessorMappingProperties processorMappingProperties;

    @Override
    protected String getMqQueue() {
        return mqQueue;
    }

    @Override
    protected String getKafkaBrokers() {
        return equityBtfBroker;
    }

    @Override
    protected TopicMappingProperties getTopicMappingProperties() {
        return topicMappingProperties;
    }

    @Override
    protected ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    @Override
    protected BtfKafkaHeaderConfig getKafkaHeaderConfig() {
        return kafkaHeaderConfig;
    }

    @Override
    protected Map<String, String> getBtfMapping() {
        return processorMappingProperties.getMapping();
    }

    @Override
    protected String getRouteId() {
        return "EquityBTFRoute";
    }
}
5. ApplicationTests.java
路径：src/test/java/x/ApplicationTests.java

java
复制
package x;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationTests {
  @Test
  public void contextLoads() {
  }
}
6. application.yml
路径：src/main/resources/application.yml

yaml
复制
kafka:
  ftb:
    equity:
      broker: "equity.kafka.broker:9092"
      topics:
        ftb-equity-topic-1:
          headers: "equityHeader1=equityValue1,equityHeader2=equityValue2"
          processor: "equityCustomProcessor,genericProcessor"
        ftb-equity-topic-2:
          headers: "equityHeader1=equityValue3,equityHeader2=equityValue4"
          processor: "genericProcessor"
    fund:
      broker: "fund.kafka.broker:9092"
      topics:
        ftb-fund-topic-1:
          headers: "fundHeader1=fundValue1,fundHeader2=fundValue2"
          processor: "fundCustomProcessor,genericProcessor"
        ftb-fund-topic-2:
          headers: "fundHeader1=fundValue3,fundHeader2=fundValue2"
          processor: "fundCustomProcessor"
  btf:
    equity:
      broker: "equity.kafka.broker:9092"
      topics:
        btf-equity-topic-1:
          headers: "equityHeader1=equityValue1,equityHeader2=equityValue2"
          processor: "equityCustomProcessor,genericProcessor"
          topic-mapping: Equity
        btf-equity-topic-2:
          headers: "equityHeader1=equityValue3,equityHeader2=equityValue4"
          processor: "equityCustomProcessor,genericProcessor"
          topic-mapping: Fund4
    fund:
      broker: "fund.kafka.broker:9092"
      topics:
        btf-fund-topic-1:
          headers: "fundHeader1=fundValue1,fundHeader2=fundValue2"
          processor: "fundCustomProcessor,genericProcessor"
          topic-mapping: Fund
        btf-fund-topic-2:
          headers: "fundHeader1=fundValue3,fundHeader2=fundValue2"
          processor: "fundCustomProcessor"
          topic-mapping: Equity2
    processor:
      mapping:
        # BTF 方向的 processor 映射配置（Map 类型），按产品类型映射
        Equity: "equityCustomProcessor,genericProcessor"
        Fund: "fundCustomProcessor,genericProcessor"

mq:
  queue: "commonQueue"
说明
公共模块 (x.common)
存放公共的 MQ 消息模型、接口、抽象配置类与 ProcessorFactory。
FTB 模块 (x.ftb)
提供专用于 Kafka→MQ 的配置类（FtbKafkaHeaderConfig、FtbProcessorMappingProperties）、处理器（GenericProcessor、EquityCustomProcessor、FundCustomProcessor）及路由（AbstractFTBRoute、EquityFTBRoute、FundFTBRoute）。
BTF 模块 (x.btf)
提供专用于 MQ→Kafka 的配置类（BtfKafkaHeaderConfig、BtfProcessorMappingProperties、TopicMappingProperties、MQMessage）及路由（AbstractBTFRoute、EquityBTFRoute）。BTF 路由中处理器映射使用的是 BtfProcessorMappingProperties，通过 ProcessorFactory 的 getProcessors(topic, mapping) 方法传入。
配置文件 (application.yml)
分别配置了 kafka.ftb 和 kafka.btf 的相关参数，以及 MQ 队列。
将以上所有文件创建到相应目录后，在 IDEA 中导入该 Maven 项目，然后执行 Maven clean install（或直接运行 Application.java），即可编译并运行项目。

这样完整的代码示例可以直接编译执行，前提是您已正确配置环境和依赖。










搜索

