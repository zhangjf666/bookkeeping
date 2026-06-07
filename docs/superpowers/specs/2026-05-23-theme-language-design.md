# 主题与语言配置功能设计文档

## 概述

为记账项目（bk-pc）增加用户级别的主题和语言配置功能，支持 PC 端和 H5 端自适应展示。

## 需求摘要

1. 用户注册时自动创建 `theme` 和 `language` 两个配置项
2. 登录页面默认语言跟随系统，主题默认为浅色
3. 登录后根据用户配置展示对应的主题和语言
4. PC 端常规设置页面增加主题和语言配置；右上角语言切换增加"跟随系统"选项；设置面板整体风格切换时保存配置
5. H5 端我的页面-常用设置中增加主题和语言配置（主题对 H5 无视觉效果，但保存到后端）

## 关键决策

- **语言存储方式**：i18n locale 值始终存储为 `zh` 或 `en`，`system` 仅作为初始设置来源。检测到系统语言为中文时设为 `zh`，其他语言设为 `en`。
- **存储优先级**：登录后用后端用户配置覆盖 localStorage，切换时同时更新后端和 localStorage，退出登录后保留 localStorage。
- **登录页切换按钮**：保留现有的语言切换和主题切换按钮，只是默认值改为跟随系统/浅色。
- **H5 主题保存**：H5 端切换主题也保存到后端，PC 端会生效。

## 后端设计

### 改动文件

- `bk-server/src/main/resources/user_default_config.json`

### 新增配置项

```json
{
  "name": "theme",
  "value": "system",
  "description": "主题设置(system:跟随系统,light:浅色,dark:深色)"
},
{
  "name": "language",
  "value": "system",
  "description": "语言设置(system:跟随系统,zh-CN:简体中文,en:英文)"
}
```

### 说明

无需修改 Java 代码。`BookkeepingUserServiceImpl.registerUser()` 已调用 `createUserConfig()` 读取此 JSON 文件创建用户配置。

## 前端核心设计

### 新增 Composable

**文件**：`bk-pc/src/composables/useUserConfig.ts`

职责：
- `loadUserConfig(userId: number)`：从后端加载用户配置
- `applyTheme(theme: string)`：应用主题（system/light/dark）
- `applyLanguage(language: string)`：应用语言（system/zh/en）
- `saveConfig(name: string, value: string)`：保存到后端和 localStorage
- `getSystemLanguage(): string`：获取系统语言（中文→zh，其他→en）

### 主题应用逻辑

- `system`：通过 `matchMedia('(prefers-color-scheme: dark)')` 检测系统主题
- `light`：强制浅色模式
- `dark`：强制深色模式

复用现有的 `useDataThemeChange()` composable 进行实际主题切换。

### 语言应用逻辑

- `system`：调用 `getSystemLanguage()` 获取实际语言后设置
- `zh`：设置 i18n locale 为 `zh`，更新 localStorage
- `en`：设置 i18n locale 为 `en`，更新 localStorage

## 登录页面改动

**文件**：`bk-pc/src/views/login/index.vue`

### 改动点

1. 语言下拉菜单增加"跟随系统"选项
2. 初始化时检测系统语言作为默认 locale
3. 主题切换默认使用浅色
4. 登录成功后调用 `loadUserConfig()` 并应用配置

## PC 端设置改动

### 常规设置页面

**文件**：`bk-pc/src/views/settings/common/index.vue`

在"常规配置"区域增加：
- 主题：单选组（跟随系统 / 浅色 / 深色）
- 语言：单选组（跟随系统 / 简体中文 / English）

切换时调用 `saveConfig()` 保存到后端和 localStorage。

### 导航栏语言切换

**文件**：`bk-pc/src/layout/components/lay-navbar/index.vue`

语言下拉菜单增加"跟随系统"选项，选中后保存 `language: "system"` 到后端。

### 设置面板整体风格

**文件**：`bk-pc/src/layout/components/lay-setting/index.vue`

整体风格切换（light/dark/system）时，额外调用 `saveConfig("theme", value)` 保存到后端。

## H5 端设置改动

### 常用设置页面

**文件**：`bk-pc/src/views/mobile/user/CommonSettings.vue`

在"常用设置"分组中增加：
- 主题：点击弹出 Action Sheet（跟随系统 / 浅色 / 深色）
- 语言：点击弹出 Action Sheet（跟随系统 / 简体中文 / English）

切换时调用 `saveConfig()` 保存到后端。

**注意**：H5 端主题切换只保存配置，不实际改变 H5 界面（H5 未开发主题功能）。语言切换立即生效。

## 数据流

```
用户注册
  → 后端创建 theme=system, language=system 配置

用户访问登录页
  → 默认语言：检测系统语言
  → 默认主题：浅色

用户登录成功
  → 调用 loadUserConfig()
  → 获取 theme 和 language
  → applyTheme(theme) → 更新 dataTheme + overallStyle
  → applyLanguage(language) → 更新 i18n locale + localStorage

用户切换设置（PC/H5）
  → 调用 saveConfig(name, value)
  → PUT /userConfig
  → 更新 localStorage
  → 重新 applyTheme/applyLanguage

用户退出登录
  → 保留 localStorage 中的设置
```

## 历史用户数据处理

历史用户（注册早于本次改动的用户）的 userConfig 表中缺少 `theme` 和 `language` 配置项。

**处理方案：数据库迁移脚本**

提供 SQL 脚本，为所有历史用户批量插入默认配置：

```sql
-- 为所有没有 theme 配置的用户插入默认值
INSERT INTO user_config (user_id, name, value, description, enable)
SELECT u.id, 'theme', 'system', '主题设置(system:跟随系统,light:浅色,dark:深色)', 'YES'
FROM bookkeeping_user u
WHERE NOT EXISTS (
    SELECT 1 FROM user_config uc WHERE uc.user_id = u.id AND uc.name = 'theme'
);

-- 为所有没有 language 配置的用户插入默认值
INSERT INTO user_config (user_id, name, value, description, enable)
SELECT u.id, 'language', 'system', '语言设置(system:跟随系统,zh-CN:简体中文,en:英文)', 'YES'
FROM bookkeeping_user u
WHERE NOT EXISTS (
    SELECT 1 FROM user_config uc WHERE uc.user_id = u.id AND uc.name = 'language'
);
```

**执行方式**：
- 部署时手动执行此 SQL 脚本
- 或添加到项目的迁移脚本目录中（如有 Flyway/Liquibase 等迁移工具）

此方案优点：
- 前端逻辑保持干净，无需处理缺失配置的情况
- 一次性解决问题，所有用户数据保持一致
- 新用户通过 `user_default_config.json` 自动创建，历史用户通过脚本补齐

## 依赖

- 复用现有 API：`bk-pc/src/api/userConfig.ts`（`getUserConfigList`, `updateUserConfig`）
- 复用现有 composable：`useDataThemeChange`, `useTranslationLang`
- 无需新增依赖包
