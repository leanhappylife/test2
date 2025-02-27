# IBM MQ Global Configuration (mapped by IbmMqProperties)
ibm:
  mq:
    queueManager: "QM1"
    channel: "DEV.APP.SVRCONN"
    connName: "localhost(1414)"
    user: "app"
    password: "pas"
    queue: "commonQueue"

# ===============================
# 1) Kafka Common Configuration
# ===============================
kafkaConfig:
  kafka_1:
    bootstrap-servers: "xx:9092"
    security.protocol: "SASL_SSL"
    sasl.mechanism: "SCRAM-SHA-512"
    sasl.jaas.config: >
      org.apache.kafka.common.security.scram.ScramLoginModule required
      username="kafka_consumer_user"
      password="YOUR_CONSUMER_PASSWORD";
    consumer:
      group-id: "share-broker-order-domain-event"
      auto-offset-reset: "earliest"
    producer:
      client-id: "xx-service"
      acks: "all"
      retries: 0

# ===============================
# 2) Unified Kafka Wrapper Configuration
# ===============================
kafkaWrapper:
  sharedPoolSize: 20
  products:
    equity:
      broker: "${kafkaConfig.kafka_1.bootstrap-servers}"
      security:
        protocol: "${kafkaConfig.kafka_1.security.protocol}"
        saslMechanism: "${kafkaConfig.kafka_1.sasl.mechanism}"
        saslJaasConfig: "${kafkaConfig.kafka_1.sasl.jaas.config}"
      consumer:
        groupId: "${kafkaConfig.kafka_1.consumer.group-id}"
        autoOffsetReset: "${kafkaConfig.kafka_1.consumer.auto-offset-reset}"
      topics:
        topic_1:
          name: "equity-topic-wrapper"
          headers:
            header1: value1
            header2: value2
          concurrency: 3
          eventProcessorMapping:
            orderCreated: "equityCustomProcessor,genericProcessor"
            orderCancelled: "genericProcessor"
        topic_2:
          name: "topic_2"
          headers:
            header1: value1
            header2: value2
          concurrency: 3
          eventProcessorMapping:
            orderCreated2: "equityCustomProcessor,genericProcessor"
            orderCancelled2: "genericProcessor"
    fund:
      broker: "yy:9092"
      security:
        protocol: "SASL_SSL"
        saslMechanism: "SCRAM-SHA-512"
        saslJaasConfig: >
          org.apache.kafka.common.security.scram.ScramLoginModule required
          username="fund_kafka_user"
          password="YOUR_FUND_PASSWORD";
      consumer:
        groupId: "fund-group"
        autoOffsetReset: "latest"
      topics:
        topic_1:
          name: "fund-topic-wrapper"
          headers:
            headerA: foo
            headerB: bar
          concurrency: 2
          eventProcessorMapping:
            subscriptionCreated: "fundCustomProcessor,genericProcessor"
            subscriptionCancelled: "genericProcessor"

# ===============================
# 3) Kafka Business Configuration for Dispatcher (MQ -> Kafka)
# ===============================
kafkaDispatcher:
  equity:
    broker: "${kafkaConfig.kafka_1.bootstrap-servers}"
    sourceQueue: "equityDispatcherQueue"
    targetTopic: "equity-topic-dispatcher"
    concurrency: 2
