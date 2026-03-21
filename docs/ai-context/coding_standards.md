# 编码规范文档

本文档记录项目的编码风格和约定，所有代码修改必须严格遵守此规范。

---

## 一、命名规范

### 1.1 Java 后端

| 类型 | 规范 | 示例 |
|------|------|------|
| 包名 | 全小写，按模块分层 | `com.hc.bookkeeping.modules.bkeeping` |
| 类名 | 大驼峰 (PascalCase) | `IncomeExpenseController`, `IncomeExpenseServiceImpl` |
| 接口名 | 大驼峰，可加I前缀(本项目不加) | `IncomeExpenseService` |
| 方法名 | 小驼峰 (camelCase) | `queryList`, `querySummary` |
| 变量名 | 小驼峰 | `incomeExpenseService`, `queryDto` |
| 常量名 | 全大写下划线 | `SUM_PERIOD_MONTH`, `DEFAULT_EXPENSE_LIMIT` |
| DTO类名 | 大驼峰，以Dto结尾 | `IncomeExpenseDto`, `SummaryDto` |
| QueryDto类名 | 大驼峰，以QueryDto结尾 | `IncomeExpenseQueryDto` |
| Entity类名 | 大驼峰，直接对应表名 | `IncomeExpense` |
| Mapper接口 | 大驼峰，以Mapper结尾 | `IncomeExpenseMapper` |

### 1.2 JavaScript / Vue 前端

| 类型 | 规范 | 示例 |
|------|------|------|
| 目录名 | 小写下划线 | `utils`, `my-components` |
| JS文件名 | 小写下划线 | `request.js`, `incomeExpense.js` |
| Vue组件名 | 大驼峰 | `RecordList.vue`, `Index.vue` |
| 组件import | 与文件名一致 | `import recordList from "@/my-components/recordList.vue"` |
| 方法名 | 小驼峰 | `getUserSummary`, `goRecordPage` |
| 变量名 | 小驼峰 | `expenseValue`, `summary` |
| 常量名 | 全大写下划线 | `PREFIX`, `BASE_URL` |

---

## 二、后端编码规范

### 2.1 Controller 层

```java
@Slf4j
@RestController
@RequestMapping("/incomeExpense")
@RequiredArgsConstructor
@Api(tags = "收入支出接口")
public class IncomeExpenseController {

    private final IncomeExpenseService incomeExpenseService;

    @Log("查询收入支出")
    @ApiOperation("查询收入支出")
    @GetMapping
    public Response get(@Validated IncomeExpenseQueryDto queryDto){
        List<IncomeExpenseDto> list = incomeExpenseService.queryList(queryDto);
        return Response.ok(list);
    }
}
```

**规范要点**：
- 类上使用 `@Slf4j` (Lombok) 记录日志
- 类上使用 `@RestController` + `@RequestMapping`
- 使用 `@RequiredArgsConstructor` 注入依赖
- 每个接口使用 `@Log` 记录操作日志
- 使用 `@Api` + `@ApiOperation` 生成API文档
- 参数使用 `@Validated` 校验
- 返回 `Response` 统一响应对象

### 2.2 Service 接口层

```java
public interface IncomeExpenseService extends BaseService<IncomeExpenseDto, IncomeExpense> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(IncomeExpenseQueryDto queryDto, Page page);
}
```

**规范要点**：
- 继承 `BaseService<DTO, Entity>`
- 使用Javadoc注释每个方法
- 参数和返回值必须明确

### 2.3 Service 实现层

```java
@Slf4j
@Service
public class IncomeExpenseServiceImpl extends BaseServiceImpl<IncomeExpenseMapstruct, IncomeExpenseDto, IncomeExpenseMapper, IncomeExpense> implements IncomeExpenseService {

    @Autowired
    private ClassifyService classifyService;

    @Override
    public List<IncomeExpenseDto> queryList(IncomeExpenseQueryDto queryDto) {
        List<IncomeExpenseDto> list = queryList(QueryUtil.bulid(queryDto));
        // 业务逻辑
        return list;
    }
}
```

**规范要点**：
- 类上使用 `@Slf4j` 和 `@Service`
- 继承 `BaseServiceImpl<..., DTO, Mapper, Entity>`
- 实现对应接口
- 依赖注入使用 `@Autowired` (字段注入) 或构造器注入
- 重写方法使用 `@Override` 标记
- 私有方法放在类末尾

### 2.4 DTO 层

```java
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="IncomeExpense对象", description="收入支出表")
public class IncomeExpenseDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;
}
```

**规范要点**：
- 使用 `@Data` (Lombok) 生成getter/setter
- 使用 `@EqualsAndHashCode(callSuper = false)` 避免继承问题
- 使用 `@ApiModel` 生成Swagger文档
- 字段使用 `@ApiModelProperty` 标注说明

### 2.5 Entity 层

```java
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("income_expense")
@ApiModel(value="IncomeExpense对象", description="收入支出表")
public class IncomeExpense implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
}
```

**规范要点**：
- 使用 `@TableName` 指定表名
- 主键使用 `@TableId` 标注
- 特殊字段使用 `@TableField` 映射
- 填充字段使用 `fill` 属性

### 2.6 异常处理

- 使用 `@ControllerAdvice` 统一处理异常
- 业务异常抛出 `BusinessException`
- 数据不存在抛出 `DataNotExsitException`

```java
@ResponseBody
@ExceptionHandler(value = BaseException.class)
public Response handlerBaseException(BaseException e) {
    log.error(e.getMessage(), e);
    return Response.fail(e.getErrorCode(), e.getLocalizedMessage());
}
```

### 2.7 注释风格

**类注释**：
```java
/**
 * <p>
 * 收入支出表 服务类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
```

**方法注释**：
```java
/**
 * 分页查询
 * @param queryDto 查询条件
 * @param page 分页
 * @return
 */
```

---

## 三、前端编码规范

### 3.1 Vue 页面结构

```vue
<template>
  <view class="content">
    <!-- 页面内容 -->
  </view>
</template>

<script>
import { mapState, mapMutations } from "vuex";
import { querySummary } from "@/api/incomeExpense.js";

export default {
  components: {
    // 组件注册
  },
  data() {
    return {
      // 响应式数据
    };
  },
  async onLoad() {
    // 页面加载
  },
  methods: {
    // 方法
  },
  computed: {
    // 计算属性
  },
  watch: {
    // 监听器
  }
};
</script>

<style lang="scss" scoped>
// 样式
</style>
```

**规范要点**：
- 顺序：components → data → onLoad → methods → computed → watch
- 使用 `<script>` 语法
- 使用 SCSS 预处理器
- 使用 `scoped` 限定样式作用域

### 3.2 API 定义

```javascript
import request from '@/utils/request.js'

let prefix = '/incomeExpense'

export function getIncomeExpense(data) {
    return request({
        url: prefix,
        method: 'GET',
        data,
        header: {'content-type': 'application/json'}
    })
}
```

**规范要点**：
- 统一从 `request.js` 导入
- 定义 `prefix` 变量作为接口前缀
- 导出命名函数
- 使用ES6语法

### 3.3 请求封装

```javascript
function service(options = {}) {
    options.url = `${base_url}${options.url}`;
    if(options.header == null) {
        options.header = {
            'content-type': 'application/x-www-form-urlencoded'
        }
    }
    options.header["Authorization"] = uni.getStorageSync('token');

    return new Promise((resolved, rejected) => {
        options.success = (res) => {
            if (Number(res.data.code) == 0) {
                resolved(res.data.data);
            } else {
                uni.showToast({
                    icon: 'none',
                    duration: 3000,
                    title: `${res.data.msg}`
                });
                rejected(res.data.msg);
            }
        }
        options.fail = (err) => {
            rejected(err);
        }
        uni.request(options);
    });
}
export default service;
```

**规范要点**：
- 使用 Promise 封装
- 统一处理 token
- 统一处理响应码
- 错误时使用 `uni.showToast` 提示

---

## 四、Git 提交规范

### 提交信息格式
```
<type>(<scope>): <subject>

<body>
```

### Type 类型
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试

---

## 五、注意事项

1. **禁止使用下划线命名法** (除JS文件目录和常量外)
2. **DTO必须实现Serializable接口**
3. **所有接口必须有Swagger文档注解**
4. **Controller必须使用统一响应格式 Response**
5. **前端必须使用async/await处理异步**
6. **使用Lombok减少样板代码**
7. **保持方法简洁，单一职责**