# Pure-Admin-Thin 项目结构文档

## 一、项目概述

Pure-Admin-Thin 是一个基于 Vue 3 + Element Plus + TypeScript 的轻量级中后台管理系统模板，采用 Vite 构建，支持动态路由、权限控制、主题切换等功能。

---

## 二、技术栈与版本

### 2.1 核心依赖

| 类别 | 框架/库 | 版本 |
|------|---------|------|
| 前端框架 | Vue | ^3.5.22 |
| 路由 | Vue Router | ^4.6.3 |
| 状态管理 | Pinia | ^3.0.3 |
| UI 组件库 | Element Plus | ^2.11.5 |
| CSS 框架 | Tailwind CSS | ^4.1.16 |
| HTTP 客户端 | Axios | ^1.12.2 |
| 构建工具 | Vite | ^7.1.12 |
| 类型检查 | TypeScript | ^5.9.3 |
| 日期处理 | dayjs | ^1.11.18 |
| 图表 | ECharts | ^6.0.0 |
| 工具库 | @pureadmin/utils | ^2.6.2 |
| 表格组件 | @pureadmin/table | ^3.3.0 |
| Vue 组合式API | @vueuse/core | ^14.0.0 |
| 本地存储 | localforage | ^1.10.0 |
| 路由动画 | @vueuse/motion | ^3.0.3 |
| Cookie | js-cookie | ^3.0.5 |
| 参数序列化 | qs | ^6.14.0 |
| 排序库 | sortablejs | ^1.15.6 |

### 2.2 开发依赖

| 框架/库 | 版本 |
|---------|------|
| Sass | ^1.93.2 |
| ESLint | ^9.38.0 |
| Prettier | ^3.6.2 |
| Stylelint | ^16.25.0 |
| Husky | ^9.1.7 |
| Commitlint | ^20.1.0 |

### 2.3 Node 环境要求

- Node.js: ^20.19.0 || >=22.13.0
- pnpm: >=9

---

## 三、核心模块说明

### 3.1 目录结构

```
pure-admin-thin/src/
├── api/                              # API 接口定义
│   ├── routes.ts                     # 路由接口
│   └── user.ts                       # 用户接口
├── assets/                           # 静态资源
│   ├── iconfont/                     # iconfont 图标
│   ├── login/                        # 登录页资源
│   ├── svg/                          # SVG 图标
│   └── table-bar/                    # 表格操作图标
├── components/                       # 公共组件
│   ├── ReAuth/                       # 权限认证组件
│   ├── ReCol/                        # 栅格布局
│   ├── ReDialog/                     # 弹窗组件
│   ├── ReIcon/                       # 图标组件
│   ├── RePerms/                      # 权限指令组件
│   ├── RePureTableBar/               # 表格工具栏
│   ├── ReSegmented/                  # 分段控制器
│   └── ReText/                       # 文本组件
├── config/                           # 全局配置
│   └── index.ts                      # 配置项
├── directives/                       # 自定义指令
│   ├── auth/                         # 权限指令
│   ├── copy/                         # 复制指令
│   ├── longpress/                    # 长按指令
│   ├── optimize/                     # 性能优化指令
│   ├── perms/                        # 权限指令
│   └── ripple/                       # 水波纹指令
├── layout/                           # 布局组件
│   ├── components/                   # 布局子组件
│   │   ├── lay-content/              # 内容区
│   │   ├── lay-footer/               # 页脚
│   │   ├── lay-frame/                # iframe 嵌套
│   │   ├── lay-navbar/               # 导航栏
│   │   ├── lay-notice/               # 通知中心
│   │   ├── lay-panel/                # 面板
│   │   ├── lay-search/               # 搜索
│   │   ├── lay-setting/              # 设置
│   │   ├── lay-sidebar/              # 侧边栏
│   │   └── lay-tag/                  # 标签页
│   ├── frame.vue                     # iframe 布局
│   └── hooks/                        # 布局 hooks
│       ├── useBoolean.ts
│       ├── useDataThemeChange.ts
│       ├── useLayout.ts
│       ├── useMultiFrame.ts
│       ├── useNav.ts
│       └── useTag.ts
├── router/                           # 路由配置
│   ├── index.ts                      # 路由入口
│   ├── modules/                      # 路由模块
│   │   ├── error.ts                  # 错误页面
│   │   ├── home.ts                   # 首页
│   │   └── remaining.ts              # 剩余路由
│   └── utils.ts                      # 路由工具函数
├── store/                            # Pinia 状态管理
│   ├── index.ts                      # store 入口
│   ├── modules/                      # store 模块
│   │   ├── app.ts                    # 应用配置
│   │   ├── epTheme.ts                # 主题配置
│   │   ├── multiTags.ts              # 多标签页
│   │   ├── permission.ts             # 权限管理
│   │   ├── settings.ts               # 系统设置
│   │   └── user.ts                   # 用户信息
│   ├── types.ts                      # 类型定义
│   └── utils.ts                      # 工具函数
├── utils/                            # 工具函数
│   ├── auth.ts                       # 认证工具
│   ├── http/                         # HTTP 请求封装
│   │   ├── index.ts                  # axios 封装
│   │   └── types.d.ts                # 类型定义
│   ├── localforage/                  # 本地存储
│   ├── message.ts                    # 消息提示
│   ├── mitt.ts                       # 事件总线
│   ├── progress/                     # 进度条
│   ├── tree.ts                       # 树形工具
│   └── ...
├── views/                            # 页面视图
│   ├── error/                        # 错误页面
│   │   ├── 403.vue
│   │   ├── 404.vue
│   │   └── 500.vue
│   ├── login/                        # 登录页
│   │   └── index.vue
│   ├── permission/                   # 权限示例
│   │   └── button/
│   └── welcome/                      # 首页
│       └── index.vue
├── App.vue                           # 根组件
└── main.ts                           # 入口文件
```

---

## 四、各层级职责

### 4.1 路由层 (Router)

**文件**: `src/router/index.ts`

**职责**:
- 静态路由定义与动态路由加载
- 路由守卫 (beforeEach) 实现权限验证
- 路由元信息 (meta) 管理
- 路由层级处理 (二级、三级拍平)

**核心功能**:
```typescript
// 静态路由自动导入
const modules = import.meta.glob(["./modules/**/*.ts", "!./modules/**/remaining.ts"], { eager: true });

// 路由守卫核心逻辑
router.beforeEach((to, from, next) => {
  // 1. 权限验证
  // 2. Token 验证
  // 3. 动态路由加载
  // 4. 标签页处理
});
```

### 4.2 状态管理层 (Store - Pinia)

**User Store** (`src/store/modules/user.ts`)
- 用户登录/登出
- Token 管理
- 用户信息存储
- 角色/权限存储

**Permission Store** (`src/store/modules/permission.ts`)
- 静态路由管理
- 动态路由组装
- 菜单生成
- 页面缓存管理

**MultiTags Store** (`src/store/modules/multiTags.ts`)
- 多标签页管理
- 标签页操作 (增删改)

**App Store** (`src/store/modules/app.ts`)
- 侧边栏状态
- 主题配置
- 国际化

### 4.3 API 层

**HTTP 请求封装** (`src/utils/http/index.ts`)

```typescript
class PureHttp {
  // 请求拦截: Token 自动注入、Token 过期刷新
  // 响应拦截: 统一错误处理
  // 方法: request, post, get
}
```

**接口定义** (`src/api/*.ts`)
- `user.ts`: 登录、刷新 Token
- `routes.ts`: 获取动态路由

### 4.4 视图层 (Views)

**特点**:
- 使用 `<script setup>` 语法
- TypeScript 类型定义
- 权限控制 (组件/指令/函数三种方式)

**权限示例**:
```vue
<!-- 组件方式 -->
<Auth value="permission:btn:add">
  <el-button>新增</el-button>
</Auth>

<!-- 指令方式 -->
<el-button v-auth="'permission:btn:add'">新增</el-button>

<!-- 函数方式 -->
<el-button v-if="hasAuth('permission:btn:add')">新增</el-button>
```

---

## 五、数据流向

### 5.1 登录流程

```
用户输入账号密码
    ↓
login/index.vue → useUserStoreHook().loginByUsername()
    ↓
api/user.ts → getLogin() → http.post('/login')
    ↓
后端返回 token + userInfo
    ↓
setToken() 存储到 localStorage
    ↓
initRouter() 加载动态路由
    ↓
router.push() 跳转首页
```

### 5.2 HTTP 请求流程

```
组件调用 API 方法
    ↓
utils/http/index.ts → http.request()
    ↓
请求拦截器: 注入 Token、检查 Token 过期
    ↓
Axios 发起请求
    ↓
响应拦截器: 统一处理响应、错误
    ↓
返回数据到组件
```

### 5.3 权限控制流程

```
登录获取用户角色 roles
    ↓
路由守卫检查 meta.roles
    ↓
filterNoPermissionTree() 过滤无权限菜单
    ↓
生成侧边栏菜单
    ↓
页面加载检查按钮权限 (auths)
    ↓
v-auth 指令控制按钮显示
```

---

## 六、核心特性

### 6.1 动态路由

- 后端返回路由菜单，前端动态生成
- 路由缓存支持 (keep-alive)
- 路由层级自动处理 (三级及以上拍平)

### 6.2 权限控制

- 页面级别权限 (roles)
- 按钮级别权限 (auths)
- 三种权限使用方式: 组件/指令/函数

### 6.3 主题系统

- 支持亮色/暗色主题
- 主题色动态切换
- 侧边栏折叠/展开

### 6.4 多标签页

- 支持多标签页切换
- 标签页拖拽排序
- 标签页缓存 (keep-alive)

---

## 七、构建与部署

### 7.1 环境变量

| 文件 | 用途 |
|------|------|
| .env | 默认环境 |
| .env.development | 开发环境 |
| .env.staging | 测试环境 |
| .env.production | 生产环境 |

### 7.2 构建命令

```bash
pnpm dev          # 开发
pnpm build        # 生产构建
pnpm build:staging # 测试构建
pnpm preview      # 预览构建结果
```

---

## 八、API 接口规范

### 8.1 响应格式

```typescript
// 成功
{
  "success": true,
  "data": { ... }
}

// 失败
{
  "success": false,
  "message": "错误信息"
}
```

### 8.2 Token 机制

- `accessToken`: 接口访问令牌
- `refreshToken`: 刷新令牌
- 过期自动刷新机制

---

## 九、注意事项

1. **Vue 3 组合式 API**: 项目全面使用 `<script setup>` 语法
2. **TypeScript**: 强类型支持，需定义完整类型
3. **Pinia 状态管理**: 替代 Vuex，使用 defineStore
4. **Element Plus**: UI 组件库
5. **Tailwind CSS**: 原子化 CSS 框架 (v4)
6. **Node 版本**: 需要 Node 20+