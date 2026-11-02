只想关闭新默认的字母排序
如果接口使用 Spring Boot 自动配置的 Jackson Mapper，可以设置：
spring:
  jackson:
    mapper:
      sort-properties-alphabetically: false

Spring Boot 4 支持通过 spring.jackson.mapper.<feature_name> 配置 MapperFeature。这类配置作用于自动配置的 Mapper；自行创建的 Mapper，需要检查它自己的配置。docs.spring.io

但要分清：
关闭字母排序，不保证完全恢复升级前每个字段的位置。

构造器识别、属性发现及显式排序规则仍可能影响输出顺序
