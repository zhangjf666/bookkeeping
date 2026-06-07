<h1>vue-pure-admin精简版（国际化版本）</h1>

[![license](https://img.shields.io/github/license/pure-admin/vue-pure-admin.svg)](LICENSE)

**中文** | [English](./README.en-US.md)

## 介绍

精简版是基于 [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin) 提炼出的架子，包含主体功能，更适合实际项目开发，打包后的大小在全局引入 [element-plus](https://element-plus.org) 的情况下仍然低于 `2.3MB`，并且会永久同步完整版的代码。开启 `brotli` 压缩和 `cdn` 替换本地库模式后，打包大小低于 `350kb`

## 移动端支持

本项目已集成移动端 H5 页面，支持 PC/移动端自动适配：

- **PC 端**: 访问 `/` 自动显示 PC 端页面（Element Plus）
- **移动端**: 访问 `/` 自动重定向到 `/m/home`（Vant 4）
- **设备检测**: 自动识别访问设备类型，无需用户手动选择

### 移动端路由
| 路由 | 页面 | 说明 |
|------|------|------|
| `/m/home` | 首页 | 收支摘要、近期账单 |
| `/m/bill` | 账单 | 账单列表、筛选 |
| `/m/record` | 记账 | 新增/编辑记录 |
| `/m/report` | 报表 | 收支统计 |
| `/m/search` | 搜索 | 搜索账单 |
| `/m/user` | 我的 | 个人中心 |
| `/m/login` | 登录 | 登录页面 |
| `/m/register` | 注册 | 注册页面 |

### 技术栈
- **PC 端**: Vue 3 + TypeScript + Element Plus + Pinia
- **移动端**: Vue 3 + TypeScript + Vant 4 + Pinia
- **共享**: API、类型定义、状态管理

## 版本选择

当前是国际化版本，如果您需要非国际化版本 [请点击](https://github.com/pure-admin/pure-admin-thin)

## 配套视频

[点我查看 UI 设计](https://www.bilibili.com/video/BV17g411T7rq)  
[点我查看快速开发教程](https://www.bilibili.com/video/BV1kg411v7QT)

## 配套保姆级文档

[点我查看 vue-pure-admin 文档](https://pure-admin.cn/)  
[点我查看 @pureadmin/utils 文档](https://pure-admin-utils.netlify.app)

## 高级服务

[点我查看详情](https://pure-admin.cn/pages/service/)

## 预览

[查看预览](https://pure-admin-thin.netlify.app/#/login)

## 维护者

[xiaoxian521](https://github.com/xiaoxian521)

## ⚠️ 注意

精简版不接受任何 `issues` 和 `pr`，如果有问题请到完整版 [issues](https://github.com/pure-admin/vue-pure-admin/issues/new/choose) 去提，谢谢！

## 许可证

[MIT © 2020-present, pure-admin](./LICENSE)
