针对刚才“JSON 转 UserLoginBO 时，Jackson 3 选中了全参构造器”的问题，推荐显式指定无参构造器。你这份代码只需要改一行注解。
1. 推荐改法：给无参构造器加 @JsonCreator
把原来的：
@NoArgsConstructor

改成：
@NoArgsConstructor(onConstructor_ = @JsonCreator)

完整代码：
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
public class UserLoginBO {

    private String userId;

    private boolean isSuccess;

    private UserLoginConst text;
}

onConstructor_ 表示让 Lombok 把 @JsonCreator 加到生成的构造器上。给无参构造器显式加 @JsonCreator，也是 Jackson 3 官方针对这个构造器选择变化给出的解决方式。projectlombok.org

这里即使使用 Jackson 3，导入的基础注解仍是：
import com.fasterxml.jackson.annotation.JsonCreator;

Jackson 3 保留了这部分注解的原包名。GitHub
2. 为什么这样可以解决？
修改后，Jackson 会：
1. 使用无参构造器创建 UserLoginBO。
2. 将 JSON 中的 success 交给 Lombok 生成的 setSuccess(boolean)。
3. 由这个 setter 给 Java 字段 isSuccess 赋值。
例如输入：
{
  "userId": "u001",
  "success": true
}

读取后：
UserLoginBO bo = mapper.readValue(json, UserLoginBO.class);

System.out.println(bo.getUserId());  // u001
System.out.println(bo.isSuccess());  // true

这样就明确了对象的创建方式，避免依赖全参构造器中的参数名 isSuccess。
3. 修复后的行为
我已在 Lombok 1.18.40、Jackson 2.20.0 / 3.0.0，并开启 -parameters 的最小复现中验证：
操作	修复后的结果
Jackson 2 读取 {"success":true}	成功，`bo.isSuccess()` 为 true
Jackson 3 读取 {"success":true}	成功，`bo.isSuccess()` 为 true
将对象返回给 UI	成功标志仍输出为 success
使用 Builder 创建对象	仍使用 .isSuccess(true)


现有 Builder 调用可以继续这样写：
UserLoginBO bo = UserLoginBO.builder()
        .userId("u001")
        .isSuccess(true)
        .build();

这处修改解决的是接收 JSON 时的构造器选择问题，接口字段继续使用 success。如果这个类始终只负责返回给 UI，则按你前面描述的用途，仍可以保留原代码。
