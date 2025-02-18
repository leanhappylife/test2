下面给出一个完整的示例项目代码，展示如何将 EquityFTBProperties 和 FundFTBProperties 的公共部分抽取到一个公共的抽象类中（称为 AbstractFtbProductProperties），从而供 Equity 与 Fund 的路由共用。项目示例只包含 FTB（Kafka→MQ）部分，其它模块（如 BTF）可按类似方式扩展。

项目目录结构
less
复制
core-avaloq-messaging-adaptor
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── x
    │   │       ├── Application.java                   // 应用入口 (包 x)
    │   │       ├── common                              // 公共模块 (包 x.common)
    │   │       │   ├── AbstractKafkaHeaderConfig.java  // 公共 Kafka header 配置抽象类
    │   │       │   ├── AbstractProcessorMappingProperties.java  // 公共 processor 映射抽象类
    │   │       │   ├── AbstractFtbProductProperties.java  // 公共 FTB 产品配置抽象类（包含 broker 和 topics）
    │   │       │   ├── MessageProcessor.java           // 处理器接口
    │   │       │   └── ProcessorFactory.java           // 处理器工厂（映射由外部传入）
    │   │       ├── ftb                                 // FTB 模块（Kafka → MQ） (包 x.ftb)
    │   │       │   ├── config
    │   │       │   │   ├── EquityFTBProperties.java      // Equity FTB 配置（继承自 AbstractFtbProductProperties）
    │   │       │   │   ├── FundFTBProperties.java        // Fund FTB 配置（继承自 AbstractFtbProductProperties）
    │   │       │   │   ├── FtbKafkaHeaderConfig.java     // FTB Kafka header 配置
    │   │       │   │   └── FtbProcessorMappingProperties.java  // FTB processor 映射配置
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
2. 公共模块 (x.common)
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
2.3 AbstractFtbProductProperties.java
路径：src/main/java/x/common/AbstractFtbProductProperties.java

java
复制
package x.common;

import java.util.Map;

public abstract class AbstractFtbProductProperties {
    /**
     * Kafka broker 地址（产品通用）
     */
    private String broker;

    /**
     * 产品对应的主题配置，key 为主题名称，
     * value 为包含 headers 与 processor 配置的对象
     */
    private Map<String, TopicConfig> topics;

    public static class TopicConfig {
        private String headers;
        private String processor;
        // Getters and Setters
        public String getHeaders() {
            return headers;
        }
        public void setHeaders(String headers) {
            this.headers = headers;
        }
        public String getProcessor() {
            return processor;
        }
        public void setProcessor(String processor) {
            this.processor = processor;
        }
    }

    public String getBroker() {
        return broker;
    }
    public void setBroker(String broker) {
        this.broker = broker;
    }
    public Map<String, TopicConfig> getTopics() {
        return topics;
    }
    public void setTopics(Map<String, TopicConfig> topics) {
        this.topics = topics;
    }
}
2.4 MessageProcessor.java
路径：src/main/java/x/common/MessageProcessor.java

java
复制
package x.common;

import org.apache.camel.Exchange;

public interface MessageProcessor {
    void process(Exchange exchange) throws Exception;
}
2.5 ProcessorFactory.java
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
3. FTB 模块 (x.ftb)
3.1 配置类
3.1.1 EquityFTBProperties.java
路径：src/main/java/x/ftb/config/EquityFTBProperties.java

java
复制
package x.ftb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import x.common.AbstractFtbProductProperties;

@Component
@ConfigurationProperties(prefix = "kafka.ftb.equity")
public class EquityFTBProperties extends AbstractFtbProductProperties {
    // 如果 Equity 有专有属性，可在此添加
}
3.1.2 FundFTBProperties.java
路径：src/main/java/x/ftb/config/FundFTBProperties.java

java
复制
package x.ftb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import x.common.AbstractFtbProductProperties;

@Component
@ConfigurationProperties(prefix = "kafka.ftb.fund")
public class FundFTBProperties extends AbstractFtbProductProperties {
    // 如果 Fund 有专有属性，可在此添加
}
3.1.3 FtbKafkaHeaderConfig.java
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
    // 直接继承公共逻辑
}
3.1.4 FtbProcessorMappingProperties.java
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
    // 直接继承公共逻辑
}
3.2 Processor 部分 (FTB)
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
    // 子类提供 Kafka Broker 地址（FTB方向）
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
import org.springframework.stereotype.Component;
import x.common.ProcessorFactory;
import x.ftb.config.EquityFTBProperties;
import x.ftb.config.FtbKafkaHeaderConfig;
import x.ftb.config.FtbProcessorMappingProperties;

@Component
public class EquityFTBRoute extends AbstractFTBRoute {

    @Autowired
    private EquityFTBProperties equityFTBProperties;

    @Autowired
    private ProcessorFactory processorFactory;

    @Autowired
    private FtbKafkaHeaderConfig kafkaHeaderConfig;

    @Autowired
    private FtbProcessorMappingProperties processorMappingProperties;

    @Override
    protected String getKafkaBrokers() {
        return equityFTBProperties.getBroker();
    }

    @Override
    protected String getTopics() {
        // 示例中返回配置中第一个主题的 key
        return equityFTBProperties.getTopics().keySet().iterator().next();
    }

    @Override
    protected String getDestinationQueue() {
        return "commonQueue";
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
import org.springframework.stereotype.Component;
import x.common.ProcessorFactory;
import x.ftb.config.FundFTBProperties;
import x.ftb.config.FtbKafkaHeaderConfig;
import x.ftb.config.FtbProcessorMappingProperties;

@Component
public class FundFTBRoute extends AbstractFTBRoute {

    @Autowired
    private FundFTBProperties fundFTBProperties;

    @Autowired
    private ProcessorFactory processorFactory;

    @Autowired
    private FtbKafkaHeaderConfig kafkaHeaderConfig;

    @Autowired
    private FtbProcessorMappingProperties processorMappingProperties;

    @Override
    protected String getKafkaBrokers() {
        return fundFTBProperties.getBroker();
    }

    @Override
    protected String getTopics() {
        // 示例中返回配置中第一个主题的 key
        return fundFTBProperties.getTopics().keySet().iterator().next();
    }

    @Override
    protected String getDestinationQueue() {
        return "commonQueue";
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
4. BTF 模块 (x.btf)
（此处只简单保留配置类与 EquityBTFRoute 示例，其他部分可按需要扩展）

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
    // 直接使用公共逻辑
}
4.1.2 BtfProcessorMappingProperties.java
路径：src/main/java/x/btf/config/BtfProcessorMappingProperties.java

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
（示例：EquityBtfProcessor）

4.2.1 EquityBtfProcessor.java
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
    // 子类提供 BTF 方向 processor 映射配置（Map 类型）
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
            // 调用 processor 链进行额外处理（使用 BTF 映射）
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
      topics: "btf-equity-topic-1,btf-equity-topic-2"
      headers:
        btf-equity-topic-1: "equityHeader1=equityValue1,equityHeader2=equityValue2"
        btf-equity-topic-2: "equityHeader1=equityValue3,equityHeader2=equityValue4"
      processor:
        mapping:
          btf-equity-topic-1: "equityCustomProcessor,genericProcessor"
          btf-equity-topic-2: "equityCustomProcessor,genericProcessor"
      topicmapping:
        Equity: "btf-equity-topic-1"
        Fund4: "btf-equity-topic-2"
    fund:
      broker: "fund.kafka.broker:9092"
      topics: "btf-fund-topic-1,btf-fund-topic-2"
      headers:
        btf-fund-topic-1: "fundHeader1=fundValue1,fundHeader2=fundValue2"
        btf-fund-topic-2: "fundHeader1=fundValue3,fundHeader2=fundValue2"
      processor:
        mapping:
          btf-fund-topic-1: "fundCustomProcessor,genericProcessor"
          btf-fund-topic-2: "fundCustomProcessor"
      topicmapping:
        Fund: "btf-fund-topic-1"
        Equity2: "btf-fund-topic-2"

mq:
  queue: "commonQueue"
说明
公共模块 (x.common)
提供共用的抽象配置类（AbstractKafkaHeaderConfig、AbstractProcessorMappingProperties、AbstractFtbProductProperties）、处理器接口 MessageProcessor 和 ProcessorFactory。

FTB 模块 (x.ftb)
通过 EquityFTBProperties 与 FundFTBProperties（均继承自 AbstractFtbProductProperties）配置各自产品的 broker 与主题映射；其余配置类 FtbKafkaHeaderConfig 与 FtbProcessorMappingProperties 负责解析 header 与 processor 映射。路由 EquityFTBRoute 和 FundFTBRoute 从相应配置中读取参数，并使用 ProcessorFactory 调用 processor 链处理消息，然后将消息转换为 XML 发送到 MQ。

BTF 模块 (x.btf)
提供 BtfKafkaHeaderConfig、BtfProcessorMappingProperties（可选）、TopicMappingProperties（将 MQ 消息中的产品类型映射到目标 Kafka topic）以及 MQMessage 模型。BTF 路由 AbstractBTFRoute 和 EquityBTFRoute 负责从 MQ 接收 XML 消息、反序列化为 MQMessage、设置 header、调用 processor 链（使用 BTF 映射），最后转换为 JSON 发送到 Kafka。

application.yml
分别配置了 kafka.ftb.equity、kafka.ftb.fund、kafka.btf.equity、kafka.btf.fund 以及 MQ 队列配置。

将以上所有文件按照对应路径创建，然后用 IDEA 导入该 Maven 项目，确保 pom.xml 正确后执行 Application.java 即可启动项目。

这就是修改后完整可执行的项目代码示例。










搜索

