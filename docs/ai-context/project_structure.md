# Bookkeeping 项目结构文档

## 一、项目概述

Bookkeeping 是一个前后端分离的记账应用，包含：
- **bk-server**: Spring Boot 后端服务
- **bk-h5**: 基于 Uni-app 的移动端 H5 页面

---

## 二、后端项目 (bk-server)

### 2.1 技术栈与版本

| 类别 | 框架/库 | 版本 |
|------|---------|------|
| 后端框架 | Spring Boot | 2.3.4.RELEASE |
| 数据库 | MyBatis-Plus | 3.3.2 |
| 数据库连接池 | Druid | 1.2.3 |
| 安全框架 | Spring Security | (Spring Boot 内置) |
| 缓存/分布式锁 | Redisson | 3.13.6 |
| JWT | jjwt | 0.11.1 |
| 文档 | Knife4j (Swagger) | 2.0.5 |
| 工具包 | Hutool | 5.7.7 |
| 对象映射 | MapStruct | 1.3.1.Final |
| 代码生成 | MyBatis-Plus Generator | 3.3.2 |
| 模板引擎 | FreeMarker | 2.3.30 |
| Java 版本 | JDK | 1.8 |
| 数据库驱动 | MySQL Connector | 8.0.11 |

### 2.2 核心模块划分

```
bk-server/src/main/java/com/hc/bookkeeping/
├── BookkeepingServerApplication.java      # 启动类
├── common/                                 # 公共模块
│   ├── annotation/                         # 自定义注解
│   │   ├── Anonymous.java                  # 匿名访问标记
│   │   ├── DataSourceKey.java              # 多数据源标记
│   │   ├── Log.java                        # 日志记录注解
│   │   └── Sort.java                       # 排序注解
│   ├── base/                               # 基础抽象类
│   │   ├── BaseDto.java                    # DTO基类
│   │   ├── BaseEntity.java                 # Entity基类
│   │   ├── BaseMapstruct.java              # MapStruct基类
│   │   ├── BaseService.java                # Service接口基类
│   │   ├── BaseServiceImpl.java            # Service实现基类
│   │   ├── IEnum.java                      # 枚举接口
│   │   └── Tree.java                       # 树形结构接口
│   ├── constants/                          # 常量定义
│   │   └── Constants.java
│   ├── exception/                          # 异常定义
│   │   ├── BaseException.java              # 基础异常
│   │   ├── BusinessException.java          # 业务异常
│   │   └── DataNotExsitException.java      # 数据不存在异常
│   ├── handler/                            # 处理器
│   │   ├── AuditorMetaObjectHandler.java   # 自动填充处理器
│   │   ├── ControllerExceptionHandler.java # 统一异常处理
│   │   └── PermissionsHandler.java         # 权限处理
│   ├── model/                              # 公共模型
│   │   ├── BoolEnum.java                   # 布尔枚举
│   │   ├── IResponseCode.java              # 响应码接口
│   │   ├── LogType.java                    # 日志类型
│   │   ├── Page.java                       # 分页模型
│   │   ├── Response.java                   # 统一响应
│   │   └── SortItem.java                   # 排序项
│   ├── support/                            # 支持类
│   │   ├── json/                           # JSON序列化
│   │   ├── mapstruct/                      # 对象映射
│   │   ├── redis/                          # Redis封装
│   │   └── valid/                          # 验证组
│   └── utils/                              # 工具类
│       ├── ExceptionUtil.java
│       ├── JsonUtil.java
│       ├── RedisUtil.java
│       ├── SpringContextUtil.java
│       ├── SpringSecurityUtil.java
│       ├── TreeUtil.java
│       └── WebUtil.java
├── config/                                 # 配置类
│   ├── CustomConfig.java
│   ├── MybatisConfig.java
│   ├── RedisConfig.java
│   ├── SpringSecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── ValidatorConfig.java
│   ├── WebMvcConfig.java
│   ├── properties/                         # 配置属性
│   └── thread/                             # 线程池配置
├── constants/                              # 系统常量
│   └── SystemConstants.java
└── modules/                                # 业务模块
    ├── admin/                              # 系统管理模块
    │   ├── controller/                     # 控制器
    │   ├── dto/                            # 数据传输对象
    │   │   └── vo/                         # 视图对象
    │   ├── entity/                         # 实体类
    │   ├── mapper/                         # 数据访问层
    │   └── service/                        # 业务逻辑层
    ├── bkeeping/                           # 记账核心模块
    │   ├── constants/                      # 业务常量
    │   ├── controller/                     # 控制器
    │   ├── dto/                            # 数据传输对象
    │   ├── entity/                         # 实体类
    │   ├── mapstruct/                      # 对象映射
    │   ├── mapper/                         # 数据访问层
    │   ├── model/                          # 业务模型
    │   └── service/                        # 业务逻辑层
    └── security/                           # 安全认证模块
        ├── controller/
        ├── dto/
        ├── entity/
        ├── mapper/
        └── service/
```

### 2.3 各层级职责

#### Controller 层
- **职责**：接收HTTP请求，参数校验，调用Service层，返回Response
- **特点**：
  - 使用 `@RestController` + `@RequestMapping`
  - 使用 `@Validated` 进行参数校验
  - 使用 `@Log` 注解记录操作日志
  - 使用 Knife4j (`@Api`, `@ApiOperation`) 生成API文档

#### Service 层
- **职责**：处理业务逻辑，事务管理
- **接口** (`Service`)：定义业务方法
- **实现** (`ServiceImpl`)：实现具体业务逻辑
- **特点**：
  - 继承 `BaseService`，复用CRUD
  - 使用 `@Service` 注解
  - 使用 `@Slf4j` 记录日志
  - 依赖注入使用 `@Autowired`

#### Mapper 层 (DAO)
- **职责**：数据库操作
- **特点**：
  - 继承 `BaseMapper<T>` (MyBatis-Plus)
  - 可定义自定义SQL方法
  - 使用 `@Mapper` 注解

#### DTO (Data Transfer Object)
- **职责**：数据传输对象，用于API参数和返回值
- **特点**：
  - 使用 `@Data` (Lombok) 生成getter/setter
  - 使用 `@ApiModel` / `@ApiModelProperty` 生成Swagger文档

#### Entity
- **职责**：数据库实体映射
- **特点**：
  - 使用 `@TableName` 指定表名
  - 使用 `@TableId` / `@TableField` 映射字段
  - 使用 `@Data` (Lombok)

#### MapStruct
- **职责**：DTO与Entity之间的对象转换
- **特点**：
  - 继承 `BaseMapstruct`
  - 使用 `componentModel = "spring"` 自动注入

### 2.4 数据流向

```
HTTP请求
    ↓
Controller (参数校验 @Validated)
    ↓
DTO (接收参数)
    ↓
Service (业务处理)
    ↓
MapStruct (DTO ↔ Entity 转换)
    ↓
Mapper (数据库操作)
    ↓
MySQL数据库
    ↓
返回流程反向执行
```

### 2.5 异常处理机制

- **统一异常处理**：`ControllerExceptionHandler` 使用 `@ControllerAdvice`
- **自定义异常**：
  - `BaseException`: 基础异常
  - `BusinessException`: 业务异常
  - `DataNotExsitException`: 数据不存在异常
- **响应格式**：`Response` 包含 code, msg, data

---

## 三、前端项目 (bk-h5)

### 3.1 技术栈与版本

| 类别 | 框架/库 | 版本 |
|------|---------|------|
| 框架 | Vue | 2.6.11 |
| 状态管理 | Vuex | 3.2.0 |
| UI框架 | Uni-app (H5) | 2.0.0-31920210709003 |
| HTTP请求 | Flyio | 0.6.2 |
| 日期处理 | Moment | 2.29.1 |
| 构建工具 | Vue CLI | 4.5.0 |
| CSS预处理器 | Sass (node-sass) | 5.0.0 |

### 3.2 目录结构

```
bk-h5/src/
├── main.js                                # 入口文件
├── api/                                   # API接口
│   ├── auth.js                            # 认证相关
│   └── incomeExpense.js                   # 收支接口
├── components/                            # 第三方/UI组件
│   └── uni-*                              # Uni-app官方组件
├── my-components/                         # 自定义公共组件
│   ├── billSelector.vue
│   ├── datePicker.vue
│   ├── echarts/
│   └── recordList.vue
├── pages/                                 # 页面
│   ├── login/                             # 登录注册
│   │   ├── login.vue
│   │   └── reg.vue
│   ├── index/                             # 首页
│   ├── bill/                              # 账单
│   ├── record/                            # 记账
│   ├── statistics/                        # 统计
│   ├── search/                            # 搜索
│   └── mine/                              # 我的
├── static/                                # 静态资源
├── store/                                 # Vuex状态管理
│   └── index.js
├── utils/                                 # 工具函数
│   ├── request.js                         # HTTP封装
│   └── utils.js
└── uni.scss                               # Uni-app样式变量
```

### 3.3 API调用规范

- **请求封装**：`utils/request.js` 统一处理
- **接口定义**：`api/*.js` 按模块划分
- **请求方式**：基于 Flyio + uni.request
- **响应处理**：统一判断 code=0 为成功

### 3.4 数据流向

```
用户操作
    ↓
Vue组件 (pages/)
    ↓
API方法 (api/*.js)
    ↓
请求封装 (utils/request.js)
    ↓
uni.request → HTTP请求
    ↓
后端服务
```

---

## 四、核心业务流程

### 4.1 记账流程

1. 用户打开首页 (`index.vue`)
2. 调用 `querySummary` 获取本月收支摘要
3. 点击"记一笔"跳转到 `record.vue`
4. 填写金额、分类、备注等信息
5. 调用 `createIncomeExpense` 创建记录
6. 返回首页刷新数据

### 4.2 认证流程

1. 用户登录 (`login.vue`)
2. 调用认证接口获取Token
3. Token存储到 `uni.setStorageSync('token')`
4. 每次请求在header中携带Token

---

## 五、配置文件

### 后端配置文件
- `application.yml` / `application.properties`：Spring Boot配置

### 前端环境配置
- 开发环境: `http://127.0.0.1:8080`
- 测试环境: `http://192.168.2.88:8080`
- 生产环境: `https://www.zhiizh.com/bookkeeping/api`