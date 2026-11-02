# Jackson 2 → Jackson 3 兼容性验证提示词：审查修订版 v1.1

**用途：**交给能够访问真实项目源码的 Copilot Agent / Codex，实现并运行兼容性验证。

**本次交付范围：**审查并修订执行提示词，运行测试方法的针对性检查。本文不是你的生产项目兼容性验收报告。

**审查日期：**2026-11-02。

## 如何使用

将“执行提示词”部分完整复制给编码助手，或让编码助手读取本文件并执行 `EXECUTABLE_PROMPT_BEGIN` 与 `EXECUTABLE_PROMPT_END` 之间的内容。文末附录记录本次修订与验证证据，不代表八个真实生产类已经通过测试。

相对上一版，本版重点补强：实际源码与运行时配置证据、独立合同断言、跨版本 JSON 比较、继承与 transient 字段检查、有损日期格式、执行状态与兼容结论分离，以及缺失旧版基线时的处理。

---

## 执行提示词

<!-- EXECUTABLE_PROMPT_BEGIN -->

# 工作任务：实现并运行本项目 Jackson 2 → Jackson 3 兼容性验证

请在当前 Java 项目中实现一套可重复运行的 Jackson 2 与 Jackson 3 对照测试，覆盖下列 8 个真实业务类，生成逐类、逐场景的兼容性报告，并根据执行证据判断哪些类需要修改、哪些可以保持原样。

请完成源码检查、测试实现、编译运行、差异定位、必要的候选修复验证和最终报告。不要只给方案或解释，也不要仅凭命名规则判断后结束。

## 一、目标、范围及完成标准

我的目标是确认：项目迁移到 Spring Boot 4 / Jackson 3 后，现有 JSON 接口的字段名称、字段值、类型、日期格式和读取行为是否保持兼容。

请遵守：

1. 优先直接测试仓库中真实 DTO、Entity 及其原全限定类名的编译产物，完整保留真实父类、枚举、构造器、初始化逻辑和注解。
2. 下列字段清单用于定位和核对。实际源码与清单不一致时，记录差异，以实际运行使用的源码为准，不要把源码覆盖成截图版本。
3. 改名、移入测试内部类、删除父类、替换枚举或简化构造器的示例，只能标记为辅助最小复现，不能替代真实业务类验证。类身份变化可能影响 MixIn、类型标识和自定义处理器。
4. 允许新增隔离测试、测试适配器、测试依赖、测试配置、运行脚本和报告。避免让测试依赖改变生产模块的依赖解析结果；记录共享 `jackson-annotations` 等依赖的实际版本。
5. 第一阶段保留生产源码和全局配置现状，不为使测试通过而批量改字段名、添加 Jackson 注解或放宽 Mapper 设置。
6. 发现真实使用路径不兼容时，保存失败证据，提供最小候选修复，在隔离工作树或明确标识的测试变体中验证；不自动合入生产源码。
7. 保留已有未提交修改，遵守仓库开发和测试约定。查找旧基线时使用隔离目录或工作树，不重置当前工作区。
8. 使用确定性的合成测试数据。固定必要的时间、标识和追踪上下文，说明固定方式，不通过删除相关 JSON 属性来掩盖差异。
9. 只扩展到确实影响这八个类的配置和调用路径。不要为了普通 JPA 注解启动生产数据库，也不要为 `@Schema` 全量重建 Swagger 或迁移整个仓库。
10. 有阻塞时继续完成不受影响的工作。无法执行的场景统一记录执行状态 `NOT_RUN`，另记录具体原因，例如 `BASELINE_UNAVAILABLE`、`MISSING_SOURCE` 或 `DEPENDENCY_UNAVAILABLE`；不得臆造配置或把默认 Mapper 冒充旧应用。
11. 完成标准是覆盖清单、可重复执行的测试、差异证据和未验证项完整；不是通过改变合同或忽略失败，让所有格子都变成绿色。
12. 结论仅对应实际版本、配置和输入，不能扩大成全部 Jackson 2.x / 3.x 版本完全等价。

此前聊天中的独立实验使用过 Jackson 2.20.0、Jackson 3.0.0、Lombok 1.18.40 和 JDK 17。这些是历史实验版本，不能直接当作本项目实际基线；历史结论只是本次待复核的重点。

## 二、检查实际项目及证据来源

### 2.1 定位源码和使用方向

按“模块路径 + 全限定类名”定位下列八个类，记录源码路径、构建产物及运行时来源；存在同名类时确认实际引用，不要只选搜索结果中的第一个。

搜索调用点，将每个使用路径标记为：

- 只输出 JSON。
- 读取 JSON。
- 双向使用。
- 仅数据库或内部 Java 调用，没有进入 Jackson。
- 使用的 HTTP converter/codec、Feign、RestTemplate、WebClient 或手工 Mapper。

已有业务说明：`UserLoginBO` 主要用于返回 UI。请结合调用点核实；只用于输出时，其辅助读取实验发现差异，不自动构成当前业务回归。

### 2.2 版本和编译信息

检查实际 JDK、编译目标、Maven/Gradle Wrapper、Spring Boot 旧版与目标版本、Jackson core/databind/annotations/模块、Lombok 和测试框架版本。

同时记录：

- 依赖树或等效解析结果，以及运行时库版本。
- 基线和目标的提交或构建标识。
- 注解处理器及 `-parameters` 配置。
- 编译产物中构造参数名是否真实存在。
- 旧基线取自哪个提交、分支、构建产物或已知配置。

旧应用不可取得时，继续做可执行的审查和库级诊断，报告不能声称已经完成旧、新项目实况对照。

### 2.3 Lombok 实际生成结果

检查当前模块及父目录生效的 `lombok.config`，以及真实代码中的：

- `@Data`、`@Getter`、`@Setter`、`@Builder`。
- `@NoArgsConstructor`、`@AllArgsConstructor`。
- `@Accessors`、`@Jacksonized`（如果存在）。
- getter 的 `is` 前缀、capitalization、前缀和 fluent 设置。
- `lombok.anyConstructor.addConstructorProperties`、注解复制设置。
- equals/hashCode 的字段选择和父类参与规则。

使用反射、`javap`、编译产物或只读 delombok 确认 getter、setter、builder、构造器、参数名和生成的注解，不覆盖生产源码。

区分三种证据：源码声明、生成的字节码、运行时映射结果。反射看到某个方法或构造器，不等于已经证明 Jackson 采用了它；需要有辨识度的输入或输出实验。

### 2.4 实际 Mapper 和注解来源

检查旧、新应用真正生效的 Mapper Bean、builder/customizer、注册模块、命名策略、字段可见性、MixIn、自定义 serializer/deserializer、creator 配置、coercion、null/未知属性处理、日期配置及 HTTP converter/codec。

如果项目存在多个 Mapper，按实际使用路径分别记录，不能只测试其中一个 Bean。

对于 `spring.jackson.use-jackson2-defaults` 等兼容选项，检查具体 Spring Boot 版本和作用范围，不假定其完整模拟 Jackson 2。

区分“从源码推断的配置”“手工重建的配置”和“已启动并取得的实际 Mapper”。测试切片若未加载生产相关 customizer，不得称为实际配置已验证。

按实际版本适配已移动、改名或删除的 API。`JsonCreator`、`JsonProperty`、`JsonAlias`、`JsonFormat` 等共享注解与 databind 中的 `JsonSerialize`、`JsonDeserialize`、`JsonNaming` 等分别核对，不笼统声称所有 Jackson 注解的包名都不变。如果使用 `@Jacksonized`，检查其生成的 databind 注解是否匹配目标版本。

## 三、八个真实类的定位清单

以下是截图中已展示的结构。实际源码、继承成员、计算属性及忽略规则仍必须另外核实。

### 3.1 DSPUserResponse

类注解：`@Data`。截图共 23 个声明字段：

```java
private String username;
private String realm;
private List<String> mail;
private List<String> displayName;
private List<String> givenName;
private List<String> objectClass;
private List<String> createTimeStamp;
private List<String> distinguishedName;
private List<String> dn;
private List<String> employeeID;
private List<String> cn;
private List<String> modifyTimeStamp;
private List<String> hsbcAdDomainName;
private List<String> hsbcAdSAMAccountName;
private List<String> postalAddress;
private List<String> universalid;
private List<String> name;
private List<String> objectGUID;
private List<String> memberOf;
private List<String> sn;
private List<String> hsbcAdManagerEmplid;
private List<String> userPrincipalName;
private List<String> roles;
```

特别保留上述精确拼写。`createTimeStamp` 和 `modifyTimeStamp` 是字符串列表，不是时间类型。

### 3.2 ResponseDTO<T, V>

类注解：`@Data`；实现 `Serializable`。截图字段：

```java
private Long code;
private Boolean success;
private String msgType;
private transient T msg;
private transient V data;
private String traceId;
```

存在手写方法：

```java
public boolean isSuccess() {
    if (getCode() >= HttpStatusConst.OK
            && getCode() < HttpStatusConst.MULTIPLE_CHOICES) {
        return true;
    }
    return success;
}
```

截图出现过以下构造器签名，需与真实源码逐一核对并保留其逻辑：

```text
ResponseDTO()
ResponseDTO(boolean success, long errorCode, String messageType, T message, V data)
ResponseDTO(boolean isSuccess, String messageType, T message)
ResponseDTO(boolean isSuccess, String messageType, T message, V data)
ResponseDTO(long errorCode, String messageType, T message, V data)
ResponseDTO(long errorCode, V data)
ResponseDTO(long errorCode, String messageType, T message)
ResponseDTO(boolean isSuccess, V data)
```

读取真实状态码常量和追踪上下文初始化。优先使用项目测试工具初始化确定性的真实上下文；确需 mock 外部依赖时，在测试层进行并声明范围。不能只复制两个构造器或替换追踪逻辑后，声称整个生产类通过。

### 3.3 UserLoginBO

截图结构：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginBO {
    private String userId;
    private boolean isSuccess;
    private UserLoginConst text;
}
```

`userId` 的 d 是小写，`isSuccess` 是 primitive boolean。必须读取真实 `UserLoginConst` 的定义、值和相关 Jackson 注解。示例枚举不能替代真实类型的验收。

### 3.4 ApplicationNoticeMailEntity

截图类注解：

```java
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "APPLICATION_NOTICE_MAIL", schema = "sa")
```

| 类型 | 字段 | JPA 列名 |
|---|---|---|
| String | uuidKey | UUID_KEY，同时有 @Id |
| String | activeFlag | ACTIVE_FLAG |
| String | mailSubject | MAIL_SUBJECT |
| String | mailBody | MAIL_BODY |
| String | mailAddressTo | MAIL_ADDRESS_TO |
| String | mailAddressCC | MAIL_ADDRESS_CC |
| String | mailToUserId | MAIL_TO_USER_ID |
| LocalDateTime | mailRequestTimestamp | MAIL_REQUEST_TIMESTAMP |
| LocalDateTime | createTimestamp | CREATE_TIMESTAMP |

`mailAddressCC` 结尾两个 C 都是大写。截图中的两个时间字段没有显式 `@JsonFormat`。核对真实 JPA imports；数据库列名与 JSON 名称分别检查。

普通实体 Mapper 测试不需要启动数据库。只有实际 Jackson 使用路径涉及代理或相关模块时，再添加相应隔离测试并说明范围。

### 3.5 ApplicationResourceResult

注解：`@Builder`、`@Data`、`@AllArgsConstructor`、`@NoArgsConstructor`；实现 `Serializable`。

```java
private String appID;
private String resourceID;
private String description;
private String allowPublic;
private String updReason;
```

保留 appID、resourceID 的大写 ID；allowPublic 是 String，不是 boolean。

### 3.6 ApplicationDatafilterEnqResult

注解：`@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`；实现 `Serializable`。截图共 22 个声明字段：

```java
private String datafilterId;
private String datafilterDescription;
private String applicationId;
private String customerAC;
private String trustNo;
private String userType;
private String grade;
private String team;
private String officer;
private String assistant;
private String fundManagerCode;
private String icCode;
private String entity;
private String locationCD;
private String resourceID;
private String remark;

@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createTimestamp;

private String createUserName;
private String createUserId;

@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime updateTimestamp;

private String updateUserName;
private String updateUserId;
```

保留 customerAC、locationCD、resourceID；icCode 前两个字符都是小写。createTimestamp 与 DSPUserResponse 中 createTimeStamp 的拼写不同。保留两个现有日期模式，不擅自更改时区、精度或 yyyy/uuuu。

### 3.7 ApplicationDatafilterRequest

截图类结构：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Application Datafilter Request")
@Validated
public class ApplicationDatafilterRequest
        extends BaseRequest
        implements Serializable {
    // 字段见下表；真实源码及父类必须读取。
}
```

16 个子类字段均为 String：

| 字段 | @NotNull | @Size(max = ...) |
|---|---|---:|
| datafilterId | 有 | 10 |
| datafilterDescription | 有 | 64 |
| applicationId | 有 | 10 |
| customerAC | 无 | 30 |
| trustNo | 无 | 30 |
| userType | 无 | 30 |
| grade | 无 | 30 |
| team | 无 | 30 |
| officer | 无 | 30 |
| assistant | 无 | 30 |
| fundManagerCode | 无 | 30 |
| icCode | 无 | 30 |
| entity | 无 | 30 |
| locationCD | 无 | 30 |
| resourceID | 无 | 30 |
| remark | 有 | 255 |

每个字段还带 @Schema，截图 name 与字段拼写一致，type 为 string。保留实际 description、example、校验 message 和分组。

必须找到真实 BaseRequest 并检查必要继承链；继承属性、方法及 Jackson 注解纳入覆盖。不能使用空父类后宣布完整对象兼容。

检查 @NotNull/@Size 实际导入包和 provider。Jackson 绑定、Bean Validation、Controller 校验分别验证。此前聊天对此类只做过截图分析；实际父类没有提供，不能沿用“已运行通过”的状态。

### 3.8 UserStaffDTO

截图结构：

```java
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStaffDTO {
    private String userID;
    private String staffID;
    private String value;
    private String type;
}
```

保留 userID、staffID 的大写 ID，以及实际 getter/setter、builder 和构造器 API。

上述清单合计 88 个截图声明字段，另有真实父类成员。分别记录源码字段数、有效读写属性数及测试覆盖数；不要求最终 JSON 必须出现 88 个键，也不自动将每个 Java 字段视作 JSON 合同属性。

## 四、分层实现对照测试

优先沿用现有测试框架，必要时创建独立测试模块。旧、新运行环境可以分开进程执行，读取同一批 fixtures，输出中性的结果格式。

### A. 库级诊断

在依赖兼容时，对同一份真实 DTO 编译产物分别使用 Jackson 2 和 3，尽量隔离库本身的影响。

```java
new com.fasterxml.jackson.databind.ObjectMapper();
```

```java
tools.jackson.databind.json.JsonMapper.builder().build();
```

这只是示意。真实依赖、版本专用注解或 JDK 不允许共存时使用隔离模块/进程，不强行拼接 classpath。声明共同注解依赖版本等剩余变量，不把“换了 Mapper”自动解释为唯一变量已控制。

日期注册了 JavaTimeModule 时，应明确称为“Jackson 2 + JavaTimeModule，其余相关设置为默认值”，不要称为完全未配置的 Mapper。

### B. 旧、新应用实况对照

使用各自真实 DTO、Lombok 编译结果、Spring 配置、模块和实际使用的 Mapper。这是判断生产迁移影响的主要依据。

不要先人为对齐所有设置，再声称应用原本兼容。无法获得真实旧应用时，B 组明确未完成，不能用 A 组替代。

### C. 原因定位和候选修复

只针对具体差异增加对照，例如日期形状对齐、大小写开关、参数名检测或显式 creator。单独记录配置和目的，保留 A、B 组原始结果。

编译参数或 Lombok 升级也变化时，按必要性增加控制实验，区分库变化、生成代码变化和项目配置变化，不机械测试所有版本和开关组合。

## 五、比较器与断言规则

### 5.1 独立合同预期

从真实接口约定、既有样例、既有测试或明确标注来源的旧版行为基线建立 expected。历史旧行为存在业务问题时，单列问题，不偷偷用理想化行为替代现行兼容合同。

expected 来源于旧版行为时，应将已核对来源的结果固化为独立 fixture，记录版本和适用合同；不要在同一测试断言中临时用被测 Mapper 的输出生成唯一 expected。更新 fixture 必须有明确合同变更依据，不能因为目标版本输出不同而自动更新。

每个类用完整规范样本覆盖全部合同属性，各属性使用可区分的值。检查两版各自符合 expected，再比较两版之间的结果。不得仅用“两版相等”判断正确；两版可能同时漏同一个属性。

### 5.2 JSON 比较

将两版 JSON 字符串交给同一个固定比较器解析，不直接对 Jackson 2 JsonNode 与 Jackson 3 JsonNode 调用 equals。

默认比较时：

- 忽略 JSON 对象属性排列顺序。
- 保留数组顺序。
- 保留属性缺失和显式 null 的区别。
- 保留字符串、数字、布尔、对象、数组等 JSON 类型区别。
- 保留字符串大小写、日期形状和精度。
- 数字 1 与 1.0 是否等价按真实合同决定并记录；不能混同字符串 "1" 与数字 1，也不要在未有合同依据时强制数字文本完全一致或静默归一化。

若项目确实对原始 JSON 文本进行签名、缓存键计算或文本快照，再单列顺序及文本格式测试。

调用 booleanValue、asBoolean、asText 等宽松读取方法前，先断言节点存在且类型正确。预期 false 应检查“属性存在 + Boolean 类型 + false”；预期 null 与预期缺失分别断言。

### 5.3 反序列化状态

对接口合同涉及的属性逐一断言，或投影为不依赖生产 equals 的中性结构；DTO.equals 只可作为补充。

重点覆盖参与合同的 transient 和父类属性。反射扫描不能只调用一次 getDeclaredFields 就认为包含继承成员。不同 ClassLoader/进程中的 DTO 不以对象身份或 equals 判等。

按真实合同区分可写、只读、只写、计算、忽略属性和纯内存状态。不要要求所有原始 Java 状态都能 JSON 无损往返，也不要将正常只读属性的行为当成回归。

对实际参与读取的类型执行：

- Jackson 2 输出 → Jackson 3 读取。
- Jackson 3 输出 → Jackson 2 读取。
- 规范输入 → 两版各自读取 → 独立预期值断言。

泛型使用各版本正确的 TypeReference/JavaType，覆盖实际调用中的代表性 payload，不能只用 raw ResponseDTO.class 后忽略类型丢失。

### 5.4 负例、执行证据与状态

关键负例必须明确接受/拒绝预期。成功时断言值，预期异常时断言确实抛出并检查合理的异常类别、属性路径或失败阶段。

捕获异常后只打印不算通过。不要求不同版本完整异常包名或整段错误文本相同。

每条结果至少分开记录：

| 维度 | 允许的示例值 |
|---|---|
| 执行状态 | PASS / FAIL / ERROR / NOT_RUN |
| 行为结论 | EQUIVALENT / EXPECTED_DEFAULT_DIFF / CONTRACT_REGRESSION / EXISTING_ISSUE / UNDETERMINED |
| 证据类型 | RUNTIME_ASSERTED / OBSERVED_ONLY / BYTECODE_INSPECTED / SOURCE_ONLY |
| 适用性 | REQUIRED_PATH / DIAGNOSTIC_ONLY / NOT_APPLICABLE |

“断言两版默认行为不同”的测试可以 PASS，但行为结论仍是 EXPECTED_DEFAULT_DIFF。不得把执行通过直接汇总成所有业务行为一致。

`BASELINE_UNAVAILABLE` 等阻塞标识记录为原因；未能执行的场景记为 `NOT_RUN`，行为结论为 `UNDETERMINED`。已尝试编译或执行但出现非预期构建、类加载或运行错误的场景记为 `ERROR`，并保留错误证据；预期异常由断言验证，不自动记为 `ERROR`。

报告实测命令、退出码、发现/执行/跳过/失败的测试数量。零测试、跳过必需用例、构建错误或吞掉异常不得显示完成验收。

分别记录单项测试进程退出码和整体兼容性验收状态。已执行测试可以全部退出 0，但必需的真实基线或使用路径未验证时，整体验收仍为 `INCOMPLETE`，聚合门禁返回非零；这不等于已确认存在 `CONTRACT_REGRESSION`。`DIAGNOSTIC_ONLY` 和 `NOT_APPLICABLE` 不自动升级为必需业务验收范围。

聚合运行入口应在真实合同失败、非预期执行错误或必需范围未完成时返回明确的非零结果；已知差异诊断按预期完成可通过。区分失败和未完成的原因，并在运行文档中说明。不要用忽略测试失败的选项或吞掉退出码制造成功。

## 六、各机制的必要场景

### 6.1 普通属性、大小写、未知字段

每类至少有一个完整规范样本检查全部有效属性。对大小写机制选择代表性字段，不机械生成全部字段 × 全部输入 × 全部配置的笛卡尔积。

重点名称变体：

```text
userID / userId / UserID / userid
staffID / staffId
resourceID / resourceId / ResourceID
objectGUID / objectGuid
employeeID / employeeId
mailAddressCC / mailAddressCc / mailAddresscc / MAIL_ADDRESS_CC
customerAC / customerAc
locationCD / locationCd
icCode / iCCode / ICCode
```

区分规范名称、仅大小写不同、下划线命名和完全未知字段。无异常不等于成功赋值：若忽略了错误属性，必须检查最终值仍是 null/默认值的情况。

大小写不敏感只能作为独立诊断配置，不直接修改生产全局设置。大小写匹配也不能替代下划线命名转换。

使用代表性用例覆盖空对象、部分字段、显式 null、空字符串、不匹配类型、未知属性和实际 coercion 行为。

### 6.2 DSPUserResponse 列表

验证正常数组、多元素顺序、空数组、null、缺失、标量字符串，以及有辨识度的错误元素类型。记录单值转数组和类型转换设置。

两个 TimeStamp 名称字段按字符串列表处理，不能套用 LocalDateTime 规则。

### 6.3 ResponseDTO getter、业务逻辑与 transient

分别记录原始 success 值、生成的 getSuccess、手写 isSuccess 直接调用、JSON success 及 msg/data 的实际输出。

必须包括能区分访问器结果的样本：code=200、success=false。检查真实状态码常量，再覆盖 code=null、199、200、299、300、500 与 success=null/true/false 的相关组合。

直接调用 isSuccess 的异常和实际 JSON 序列化结果是两组断言，不能把前者的 NPE 直接等同于后者一定失败。

检查真实构造器初始化，尤其没有赋 code/success 的路径；设置和清理追踪上下文，记录任何测试层隔离。

若真实用途包含读取，使用参数化类型逐项检查实际泛型内容，包括 msg/data；不要依赖排除了 transient 字段的 equals。

历史观察需重新验证：两版可能都使用 getSuccess 返回字段值；transient 的 msg/data 可能通过 getter 进入 JSON；手写方法存在 null 拆箱风险。这些不是预设的项目结论。

不要将下列改动自动当成迁移修复：

```java
public Boolean getSuccess() {
    return isSuccess();
}
```

它可能改变现有 JSON success。原有业务问题单列，不能在兼容测试中顺便改变业务语义。

### 6.4 UserLoginBO 的 boolean 与 creator

检查实际生成的 isSuccess、setSuccess、builder.isSuccess 和全参构造器的 isSuccess 参数；对方法存在性、参数名存在性及规范输出作断言。

根据真实配置验证：

```json
{"success": true}
```

```json
{"success": false}
```

```json
{"isSuccess": true}
```

```json
{}
```

```json
{"success": null}
```

有必要时增加两个名称同时出现的场景，检查输入顺序影响。核对 Jackson 2 参数名模块、Jackson 3 creator/参数名设置和实际编译产物。

不能因为存在无参构造器就假定一定使用它；不能因为有 @Builder 就假定 Jackson 使用 builder。无法直接观察 creator 选择时，标明实测结果和机制推断的边界。

只在真实入站路径需要读取旧 success 合同并复现 creator 名称分歧时，验证最小候选方案：

```java
import com.fasterxml.jackson.annotation.JsonCreator;

@NoArgsConstructor(onConstructor_ = @JsonCreator)
```

修复验收包括规范旧输入恢复、输出合同保留、getter/setter、builder 和原构造器 API 可用。该方案不自动统一 primitive null、未知属性等其他默认行为。

不要关闭 primitive null 检查来冒充名称修复，也不要未经验证改名或直接给字段添加 @JsonProperty("isSuccess")。

实际只返回 UI 时，规范输出通过即可支持该输出路径兼容；辅助读取差异标记 DIAGNOSTIC_ONLY。

增加小型辅助命名用例验证 boolean success、boolean isSuccess、Boolean success、Boolean isSuccess，以及必要的手写 isSuccess 组合。辅助类不能代替生产类结果。

### 6.5 时间类型

覆盖 MailEntity 的 mailRequestTimestamp/createTimestamp，EnqResult 的 createTimestamp/updateTimestamp，检查实际模块、输出形状、模式、null、缺失、空字符串、规范日期、T 分隔、小数秒及纳秒精度。

EnqResult 保留现有 yyyy-MM-dd HH:mm:ss。普通往返样本先使用秒精度值，再单独检查带纳秒值：

1. 序列化不应偷偷修改原对象。
2. JSON 按既有合同输出到秒。
3. 读取预期为按合同减少精度后的时间。
4. 比较两版在上述条件下是否一致。

不能错误要求秒级有损格式保留原纳秒值，也不能随意归一化未知日期差异。

明确区分 Jackson 2 的模块注册、timestamp 默认设置、关闭 timestamp 的对齐实验，以及 Jackson 3 的实际支持和项目配置。对齐后的相同结果不代表两版原始默认值相同。

核对 @JsonFormat 在实际选用的 creator/属性路径上是否生效。不要自动修改模式、添加时区或改变日期精度。

### 6.6 BaseRequest、Schema、Bean Validation

覆盖真实父类的有效属性和继承的 Jackson 配置。父类不存在时，只能完成子类及其他类的可执行检查，并对完整请求对象标记未验证。

@Schema 是文档描述，不能代替 Jackson 属性配置。保留实际文档注解，检查文档与运行时名称是否冲突，不为本任务全量重建 Swagger。

对真实约束及分组，用代表性数据检查 null、空字符串、合法值、最大长度及超长值。JSON 绑定、Validator 执行、Controller 触发校验分别给出证据。

不把 DTO 类级 @Validated 当成 readValue 自动校验的证明，也不擅自把 @NotNull 改成 @NotBlank 或改变长度限制。

## 七、实际 HTTP 使用路径

对确实对外提供 JSON 的路径，在可用的现有测试设施中使用真实 Controller、相关配置和 converter/codec，隔离业务服务与外部依赖。

验证实际请求绑定、响应合同、校验触发、HTTP 状态和既有错误响应结构。使用 MockMvc、WebFlux 对应设施或项目已有方式，不指定与实际版本不兼容的测试注解。

测试切片必须确认加载了相关真实 Jackson 配置。额外测试端点或独立 Mapper 不等于真实接口已经通过。

HTTP 层缺少运行条件时，完成 Mapper 层并单列阻塞；不能为了扩大覆盖连接真实生产系统。普通 JPA 实体的 Mapper 检查无需数据库，全量端到端改造不在本次范围内。

## 八、候选修复和证据保留

真实使用路径发生接口合同回归，或迁移直接造成编译、类加载、必要 Mapper 初始化失败时，才列为需要修复的实际问题。仅由测试环境或旧基线缺失造成的阻塞应单独报告，不能据此臆定生产代码有缺陷。不能把所有库默认差异或仅输出类型的诊断性读取差异列为生产缺陷。

保留修改前的输入、预期、实际输出、异常及失败断言。在隔离版本验证最小修复，记录修改前后差异以及对字段名、值、日期、构造器和 builder API 的影响。

改名类、测试内部类或简化变体只能证明方案的局部可行性。只有在隔离工作树或等效隔离构建中，使用原全限定类名、真实依赖对象和实际 Mapper 配置验证后，才能标记真实使用路径的候选修复已通过验收；否则保留辅助证据标记。

不要批量改 ID/GUID/CC/AC/CD 名称，不要批量增加 JsonProperty/JsonCreator，不要通过全局放宽大小写、未知字段或 primitive null 来掩盖局部问题。

允许单独建立配置对照来定位原因，但不能用诊断配置覆盖真实配置结果，也不能删除失败场景或把目标版本当前输出自动回写为 expected。

## 九、执行顺序和交付

执行顺序：

1. 检查仓库约定和工作区；定位八类、必要依赖、版本和使用方向。
2. 建立真实源码、生成成员、实际配置及合同属性的清单。
3. 实现完整规范样本、独立预期和公共比较器；先验证比较器自身能检出典型错误。
4. 完成可执行的真实读写路径、父类、泛型及高风险边界。
5. 只为解释具体差异增加库默认、编译参数或配置对照。
6. 完成可用的 HTTP/校验验证和必要候选修复实验。
7. 输出证据、差异与未验证项。发现阻塞时继续其他部分，最后集中说明。

至少交付：

- 实际测试代码、必要测试依赖和确定性 fixtures。
- 可重复运行的一条或少量命令。
- 版本、提交/构建标识、编译选项和 Mapper 配置记录。
- 每次运行的命令、退出码、测试数量和结果文件。
- 每类每场景的 JSON 或 CSV 结果，以及 Markdown 报告。
- 必要时，经隔离验证的最小候选补丁。

按仓库约定选择目录，例如独立测试模块或 tools/jackson-compat，不强制固定结构。

报告至少包含：

| 类/模块/路径 | 使用方向 | 配置组 | 场景 | 执行状态 | 行为结论 | 证据类型 | 输入及差异路径 | 是否需修改 | 未覆盖/阻塞 |
|---|---|---|---|---|---|---|---|---|---|

最终用中文回答：哪些可保持原样、哪些仅在特定路径/配置下要改、哪些是原有问题、哪些是真实迁移回归、哪些仅静态分析、哪些未运行，以及执行命令和结果。

明确说明缺失旧基线、父类或实际 Mapper 时不能出具的结论。不要使用“八类全部兼容”掩盖未覆盖路径。

现在开始检查当前仓库，简要说明发现，然后实现、运行、定位并报告，完成所有当前可执行的工作。

<!-- EXECUTABLE_PROMPT_END -->

---

## 附录 A：本次审查的主要修订

| 修订点 | 解决的问题 |
|---|---|
| 实际源类、原全限定类名与编译产物优先 | 避免重建内部类改变 MixIn、类型标识、父类或构造器条件 |
| 库级、项目实况、诊断修复三组分开 | 避免把人为对齐配置后的结果当成原项目兼容 |
| 独立 expected + 同一个 JSON 比较器 | 避免两版同时输出错误字段还通过；避免跨库节点 equals 假失败 |
| 属性存在、类型、值分步断言 | 避免 null 或错误类型被宽松读取成 false 等默认值 |
| 逐合同属性检查，equals 只作补充 | 避免漏掉 transient、继承状态或跨 ClassLoader 类身份差异 |
| 按读写访问规则和日期精度定义往返预期 | 避免把只读属性、有损秒精度日期误判为迁移失败 |
| 执行状态与行为结论分开 | 测试证明“存在差异”可以执行通过，但不能汇总成行为一致 |
| 检查测试发现数量、退出码和未运行范围 | 避免零测试、跳过、吞异常被当作验收通过 |
| 基线缺失、父类缺失有明确状态 | 避免为了完成任务臆造旧配置或空父类 |
| 按真实使用方向判断修改必要性 | UserLoginBO 只返回 UI 时，不因辅助读取差异强制修改 |
| 保留字段清单，限制无意义组合 | 88 个声明字段用于核对，不等于必须输出 88 个 JSON 属性 |
| 共享注解与 databind 注解分别核对 | 避免“所有 Jackson 注解都不换包”的错误概括 |

## 附录 B：本次提示词方法验证记录

### 验证范围

本次运行的是独立 Java 验证程序 `PromptVerificationProbe`，用于检验提示词中的比较和断言方法能否避免典型假通过/假失败；并复核此前已经定位的两个重点机制。运行环境为 OpenJDK 17、Jackson 2.20.0、Jackson 3.0.0、Lombok 1.18.40；相关构造器实验保留了参数名。

这是方法级验证。没有取得并运行用户完整工程，也没有验证真实 BaseRequest、真实 UserLoginConst、Spring Boot 运行时 Mapper 或生产 HTTP 接口。代码中使用的 DTO 是此前按截图建立的独立复现类。

### 执行结果

**14 项针对性检查通过，Java 程序退出码为 0。** 编译时出现了一条 Lombok 关于子类 equals 未调用父类的警告；这是刻意构造的默认继承比较场景，与第 9 项验证相对应。

| 编号 | 实际检查 | 结果 |
|---:|---|---|
| 1 | 内容相同的 Jackson 2/3 JsonNode 直接 equals 不适合作为共同语义比较 | 通过 |
| 2 | 同一解析器比较 JSON 对象可以忽略属性排列顺序 | 通过 |
| 3 | 同一树比较保留属性缺失与显式 null 的区别 | 通过 |
| 4 | 比较保留数组顺序 | 通过 |
| 5 | 比较区分字符串和数字类型 | 通过 |
| 6 | 两版同样输出错误字段时，独立 expected 能识别错误 | 通过 |
| 7 | null 的 booleanValue 可能是 false，单独检查该值不充分 | 通过 |
| 8 | Lombok equals 可忽略 Jackson 实际输出的 transient 字段差异 | 通过 |
| 9 | 默认 Lombok 子类 equals 可忽略父类字段差异 | 通过 |
| 10 | 现有秒精度 JsonFormat 在所测两版产生相同明确日期形状 | 通过 |
| 11 | 日期回读预期应符合秒精度合同，而非与原纳秒值完全相等 | 通过 |
| 12 | 错误大小写输入可被某版忽略，无异常但属性仍未赋值 | 通过 |
| 13 | 保留参数名时 UserLoginBO 的 success 入站差异可复现 | 通过 |
| 14 | 显式无参 creator 恢复目标 success=true 输入 | 通过 |

实际程序结束输出：

```text
VERSIONS Jackson2=2.20.0, Jackson3=3.0.0
RESULT: 14 targeted review checks passed; project integration was not run.
```

“14 项通过”说明这些比较器防护和诊断机制的断言成立，其中包含明确检测到的版本差异；不表示八个生产类在所有输入下行为相同。

文件交付前已检查八类字段清单（合计 88 个截图声明字段）、21 个 Markdown 代码围栏块、执行提示词边界、关键保护条款和 14 项运行记录的一致性，检查通过。没有执行用户工程的内容不得升级为项目通过结论。

## 附录 C：官方参考资料

- [Jackson 3.0 行为变化与构造器发现](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0)
- [Jackson 3 迁移指南](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
- [Jackson Java 8 模块及 Jackson 3 合并说明](https://github.com/FasterXML/jackson-modules-java8)
- [Lombok Getter/Setter](https://projectlombok.org/features/GetterSetter)
- [Lombok 构造器](https://projectlombok.org/features/constructor)
- [Lombok EqualsAndHashCode](https://projectlombok.org/features/EqualsAndHashCode)
- [Spring MVC RequestBody 与参数校验](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html)

请在目标仓库执行时结合实际使用版本核对官方文档和源码，不把本文件所用实验版本当作项目版本。
