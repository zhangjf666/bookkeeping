# 注册功能详细设计文档 (feature_register_prd.md)

## 1. 功能概述

### 1.1 功能描述
用户注册功能允许新用户通过填写用户名、密码等信息创建账号，成为记账系统的正式用户。

### 1.2 使用场景
- 新用户首次使用记账系统时创建账号
- 已登录用户不允许访问注册页面
- 注册成功后自动登录并跳转到首页

---

## 2. 后端设计

### 2.1 API 接口

#### 2.1.1 注册接口
| 项目 | 内容 |
|------|------|
| 请求路径 | POST /auth/register |
| 请求头 | Content-Type: application/json |
| 是否需要认证 | 否 (@Anonymous) |

#### 2.1.2 请求参数 (RegisterUserDto)
| 字段 | 类型 | 必填 | 描述 | 验证规则 |
|------|------|------|------|----------|
| username | String | 是 | 用户名 | 3-20字符，不能为空 |
| password | String | 密码 | 是 | 6-20字符，不能为空 |
| repeatPassword | String | 确认密码 | 是 | 必须与password一致 |

#### 2.1.3 响应参数 (Response)
| 字段 | 类型 | 描述 |
|------|------|------|
| code | Integer | 0=成功，其他=失败 |
| msg | String | 消息 |
| data | Object | 返回数据(注册成功则返回空) |

### 2.2 后端实现

#### 2.2.1 已有的 DTO
```java
@Data
public class RegisterUserDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String repeatPassword;
}
```

#### 2.2.2 已有的 Service 方法
```java
// UserService.java
boolean registerUser(RegisterUserDto dto);
```

#### 2.2.3 已有的 Controller 方法 (AuthenticationController.java)
```java
@Anonymous
@ApiOperation("注册用户")
@PostMapping("/register")
public Response create(@Validated(Insert.class) @RequestBody RegisterUserDto dto) {
    if(userService.checkExist(dto.getUsername())) {
        throw new BusinessException("用户名已存在");
    }
    if(!dto.getPassword().equals(dto.getRepeatPassword())){
        throw new BusinessException("2次密码不相同");
    }
    userService.registerUser(dto);
    return Response.ok();
}
```

#### 2.2.4 注册逻辑 (UserServiceImpl.registerUser)
1. 验证用户名是否已存在
2. 验证两次密码是否一致
3. 对密码进行 BCrypt 加密
4. 创建用户记录（默认分配普通用户角色���
5. 返回成功

---

## 3. 前端设计

### 3.1 页面设计

#### 3.1.1 注册页面路由
| 项目 | 内容 |
|------|------|
| 路由路径 | /register |
| 组件 | src/views/register/index.vue |

#### 3.1.2 页面结构
- 用户名输入框
- 密码输入框
- 确认密码输入框
- 验证码输入框（刷新按钮）
- 注册按钮
- 已有账号？去登录 链接

### 3.2 API 调用

#### 3.2.1 API 文件
`src/api/bookkeeping/auth.ts` (已存在)
```typescript
export function register(data: any) {
  return http.post("/auth/register", { data });
}
```

### 3.3 数据类型

#### 3.3.1 注册参数 (RegisterParams)
```typescript
interface RegisterParams {
  username: string;      // 用户名
  password: string;      // 密码
  repeatPassword: string; // 确认密码
  captcha: string;       // 验证码
  uuid: string;          // 验证码UUID
}
```

### 3.4 交互流程

#### 3.4.1 注册流程
1. 用户进入注册页面
2. 系统自动获取验证码并显示
3. 用户填写用户名、密码、确认密码、验证码
4. 点击注册按钮
5. 前端校验表单数据
6. 调用注册 API
7. 注册成功后自动登录
8. 跳转到首页

---

## 4. 表单验证规则

### 4.1 用��名
| 规则 | 描述 |
|------|------|
| 必填 | 用户名不能为空 |
| 长度 | 3-20 个字符 |
| 格式 | 字母、数字、下划线 |

### 4.2 密码
| 规则 | 描述 |
|------|------|
| 必填 | 密码不能为空 |
| 长度 | 6-20 个字符 |
| 一致性 | 两次输入密码必须一致 |

### 4.3 验证码
| 规则 | 描述 |
|------|------|
| 必填 | 验证码不能为空 |
| 长度 | 4 位字符 |

---

## 5. 错误处理

### 5.1 后端错误码
| code | msg | 描述 |
|------|-----|------|
| 0 | ok | 成功 |
| 500 | 用户名已存在 | 用户名重复 |
| 500 | 2次密码不相同 | 密码不一致 |
| 500 | 验证码错误 | 验证码不正确 |
| 500 | 验证码已过期 | 验证码超时 |

### 5.2 前端错误处理
- 验证码错误：刷新验证码，提示用户
- 用户名已存在：提示用户更换用户名
- 网络错误：提示用户检查网络

---

## 6. 安全考虑

### 6.1 密码安全
- 密码使用 BCrypt 加密存储
- 密码不在日志中输出
- 密码不在响应中返回

### 6.2 防止暴力注册
- 验证码机制防止自动化注册
- 同一 IP 注册频率限制（可选）

---

## 7. 实施任务

### 7.1 后端任务
- [x] RegisterUserDto 已存在
- [x] registerUser 方法已存在
- [x] /auth/register 接口已实现

### 7.2 前端任务
- [ ] 创建注册页面组件 `src/views/register/index.vue`
- [ ] 添加注册路由 `src/router/modules/remaining.ts`
- [ ] 完善登录页面添加注册链接

---

## 8. 验收标准

### 8.1 功能验收
- [ ] 可以成功注册新用户
- [ ] 用户名重复时提示错误
- [ ] 两次密码不一致时提示错误
- [ ] 验证码错误时提示错误
- [ ] 注册成功后自动登录
- [ ] 注册成功后跳转到首页

### 8.2 界面验收
- [ ] 注册页面布局美观
- [ ] 表单验证提示清晰
- [ ] 错误信息显示正确
- [ ] 响应式布局正常

---

## 9. 参考资料

- 后端代码位置：`bk-server/src/main/java/com/hc/bookkeeping/modules/security/controller/AuthenticationController.java`
- 前端 API：`bk-pc/src/api/bookkeeping/auth.ts`
- 登录页面参考：`bk-pc/src/views/login/index.vue`