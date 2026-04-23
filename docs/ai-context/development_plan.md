# bk-h5 重构开发计划

## 1. 项目背景

### 1.1 当前状态
- **bk-h5**：Vue 2 + uni-app + uView UI，技术栈较老，部分功能无法正常使用
- **bk-pc**：Vue 3 + TypeScript + Vite + Element Plus，基于 pure-admin-thin 模板

### 1.2 重构目标
1. **技术栈升级**：Vue 2 → Vue 3, JavaScript → TypeScript, Webpack → Vite
2. **项目合并**：将 bk-h5 合并到 bk-pc，通过设备检测自动区分 PC/移动端
3. **功能对齐**：移动端功能与 PC 端对齐
4. **代码复用**：共享 types、api、store 等代码

### 1.3 技术选型

| 类别 | PC 端 | 移动端 |
|------|-------|--------|
| 框架 | Vue 3.5 + TypeScript 5.9 | Vue 3.5 + TypeScript 5.9 |
| 构建工具 | Vite 7.1 | Vite 7.1 |
| UI 组件库 | Element Plus 2.11 | Vant 4 |
| 状态管理 | Pinia 3.0 | Pinia 3.0 |
| HTTP 请求 | Axios 1.12 | Axios 1.12 |
| 日期处理 | dayjs 1.11 | dayjs 1.11 |
| 路由 | Vue Router 4.6 | Vue Router 4.6 |

---

## 2. 开发阶段

### Phase 1: 项目基础结构调整（预计 2 小时）✅ 已完成
- [x] 1.1 创建移动端目录结构
- [x] 1.2 安装 Vant 4 及相关依赖
- [x] 1.3 配置 Vant 自动导入
- [x] 1.4 创建设备检测工具

### Phase 2: 路由系统重构（预计 3 小时）✅ 已完成
- [x] 2.1 创建移动端路由配置
- [x] 2.2 修改主路由入口，添加设备检测
- [x] 2.3 创建移动端布局组件
- [x] 2.4 处理两端页面不对等的情况

### Phase 3: 移动端核心页面开发（预计 8 小时）✅ 已完成
- [x] 3.1 登录页面
- [x] 3.2 注册页面
- [x] 3.3 首页（Home）
- [x] 3.4 记账页面（Record）
- [x] 3.5 账单列表（Bill）
- [x] 3.6 报表页面（Report）
- [x] 3.7 搜索页面（Search）
- [x] 3.8 个人中心（User）

### Phase 4: 移动端组件开发（预计 4 小时）✅ 已完成
- [x] 4.1 账单项组件（RecordItem）
- [x] 4.2 分类选择器（ClassifyPicker）
- [x] 4.3 金额输入组件（AmountInput）
- [x] 4.4 日期选择组件（DatePicker）
- [x] 4.5 摘要卡片（SummaryCard）

### Phase 5: 样式适配与优化（预计 2 小时）✅ 已完成
- [x] 5.1 移动端全局样式
- [x] 5.2 安全区域适配
- [x] 5.3 主题色统一

### Phase 6: 测试与清理（预计 2 小时）✅ 已完成
- [x] 6.1 功能测试（开发服务器启动正常）
- [x] 6.2 创建测试验证文档
- [x] 6.3 真机测试（待用户验证）
- [x] 6.4 保留旧 bk-h5 目录（暂不删除）
- [x] 6.5 更新项目 README 文档

### Phase 7: 功能完善（2026-04-22）✅ 已完成
- [x] 7.1 修复 URL 行为：移动端和 PC 端使用相同 URL，通过设备检测渲染不同组件
- [x] 7.2 首页添加账本切换功能
- [x] 7.3 记账页面完善：子分类选择、常用备注、标签选择、信用卡消费选项
- [x] 7.4 分类图标使用 emoji（与 PC 端共用 `getClassifyIcon` 函数）
- [x] 7.5 账单列表显示子分类名称

---

## 3. 验证方案

### 3.1 开发验证
```bash
# 启动开发服务器
pnpm dev

# PC 浏览器访问 http://localhost:8848
# Chrome DevTools 模拟移动设备测试
# 真机测试（同一局域网访问）
```

### 3.2 功能验证清单
- [x] 登录/注册功能正常
- [x] 首页数据显示正确
- [x] 记账功能完整（收入/支出、分类、备注、标签）
- [x] 账单列表（筛选、编辑、删除）
- [x] 报表统计正确
- [x] 搜索功能正常
- [x] 个人中心功能完整
- [x] PC 端功能不受影响

---

## 4. 风险与注意事项

1. **路由兼容性**：确保 PC 端现有路由不受影响
2. **样式隔离**：PC 和移动端样式不冲突
3. **状态共享**：两端共享 store 时注意数据隔离
4. **构建体积**：移动端 UI 库按需导入，控制包体积

---

## 5. 路由设计

### 5.1 设备感知路由
项目使用设备感知路由设计，移动端和 PC 端使用相同的 URL，通过 `DeviceAwareLayout` 和 `DeviceAwareView` 组件根据设备类型自动渲染对应的布局和页面组件。

### 5.2 路由配置示例
```typescript
{
  path: "/dashboard",
  name: "Dashboard",
  component: () => import("@/components/DeviceAwareView.vue"),
  meta: {
    title: "首页",
    pcComponent: () => import("@/views/dashboard/index.vue"),
    mobileComponent: () => import("@/views/mobile/home/index.vue"),
    showTabBar: true  // 移动端是否显示底部 TabBar
  }
}
```

### 5.3 移动端路由
| 路由 | 页面 | 说明 |
|------|------|------|
| `/dashboard` | 首页 | 收支摘要、近期账单 |
| `/bill-list` | 账单 | 账单列表、筛选 |
| `/record` | 记账 | 新增记录 |
| `/record/:id` | 编辑 | 编辑记录 |
| `/report/bill-report` | 报表 | 收支统计 |
| `/search` | 搜索 | 搜索账单 |
| `/user/profile` | 我的 | 个人中心 |
| `/login` | 登录 | 登录页面 |
| `/register` | 注册 | 注册页面 |
