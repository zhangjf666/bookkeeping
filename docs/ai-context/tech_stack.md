# 技术栈与依赖版本

## 1. 核心技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.22 | 前端框架 |
| TypeScript | 5.9.3 | 类型系统 |
| Vite | 7.1.12 | 构建工具 |
| Vue Router | 4.6.3 | 路由管理 |
| Pinia | 3.0.3 | 状态管理 |

---

## 2. UI 组件库

### 2.1 PC 端
| 依赖 | 版本 | 说明 |
|------|------|------|
| element-plus | 2.11.5 | PC 端 UI 组件库 |
| @element-plus/icons-vue | - | Element Plus 图标 |

### 2.2 移动端（新增）
| 依赖 | 版本 | 说明 |
|------|------|------|
| vant | ^4.9.0 | 移动端 UI 组件库 |
| @vant/auto-import-resolver | ^1.2.1 | Vant 自动导入解析器 |

---

## 3. 工具库

| 依赖 | 版本 | 说明 |
|------|------|------|
| axios | 1.12.2 | HTTP 请求库 |
| dayjs | 1.11.18 | 日期处理库 |
| qs | 6.14.0 | URL 参数序列化 |
| js-cookie | 3.0.5 | Cookie 管理 |
| mitt | 3.0.1 | 事件总线 |
| nprogress | 0.2.0 | 进度条 |
| localforage | 1.10.0 | 本地存储增强 |
| @vueuse/core | 14.0.0 | Vue 组合式工具库 |
| lodash-es | 4.17.21 | 工具函数库 |
| pinyin-pro | 3.27.0 | 拼音转换 |

---

## 4. 开发依赖

### 4.1 构建相关
| 依赖 | 版本 | 说明 |
|------|------|------|
| @vitejs/plugin-vue | 6.0.1 | Vite Vue 插件 |
| @vitejs/plugin-vue-jsx | 5.1.1 | Vite JSX 插件 |
| vite-plugin-compression | 0.5.1 | Gzip 压缩 |
| vite-svg-loader | 5.1.0 | SVG 加载器 |
| sass | 1.93.2 | CSS 预处理器 |
| tailwindcss | 4.1.16 | CSS 框架 |

### 4.2 代码质量
| 依赖 | 版本 | 说明 |
|------|------|------|
| eslint | 9.38.0 | 代码检查 |
| prettier | 3.6.2 | 代码格式化 |
| stylelint | 16.25.0 | 样式检查 |
| husky | 9.1.7 | Git Hooks |
| lint-staged | 16.2.6 | 暂存区检查 |

### 4.3 类型定义
| 依赖 | 版本 | 说明 |
|------|------|------|
| @types/node | 20.19.23 | Node.js 类型 |
| @types/js-cookie | 3.0.6 | js-cookie 类型 |
| @types/nprogress | 0.2.3 | nprogress 类型 |
| @types/qs | 6.14.0 | qs 类型 |

---

## 5. Vant 4 组件使用清单

### 5.1 基础组件
| 组件 | 用途 |
|------|------|
| van-button | 按钮 |
| van-cell | 单元格 |
| van-icon | 图标 |
| van-image | 图片 |
| van-popup | 弹出层 |
| van-toast | 轻提示 |
| van-loading | 加载 |

### 5.2 表单组件
| 组件 | 用途 |
|------|------|
| van-field | 输入框 |
| van-picker | 选择器 |
| van-picker-group | 选择器组 |
| van-date-picker | 日期选择 |
| van-number-keyboard | 数字键盘 |
| van-search | 搜索框 |
| van-switch | 开关 |
| van-radio | 单选框 |
| van-checkbox | 复选框 |
| van-tag | 标签 |

### 5.3 展示组件
| 组件 | 用途 |
|------|------|
| van-nav-bar | 导航栏 |
| van-tabbar | 标签栏 |
| van-tab | 标签页 |
| van-collapse | 折叠面板 |
| van-empty | 空状态 |
| van-skeleton | 骨架屏 |
| van-swipe-cell | 滑动单元格 |

### 5.4 导航组件
| 组件 | 用途 |
|------|------|
| van-tabbar | 底部导航 |
| van-tabbar-item | 导航项 |
| van-nav-bar | 顶部导航 |

### 5.5 反馈组件
| 组件 | 用途 |
|------|------|
| van-dialog | 弹窗 |
| van-action-sheet | 动作面板 |
| van-notify | 通知 |

---

## 6. 安装命令

### 6.1 安装 Vant 4
```bash
pnpm add vant@4 @vant/auto-import-resolver unplugin-vue-components -D
```

### 6.2 完整依赖安装
```bash
pnpm install
```

---

## 7. Vite 配置更新

### 7.1 添加 Vant 自动导入
修改 `build/plugins.ts`：

```typescript
import { VantResolver } from '@vant/auto-import-resolver';
import Components from 'unplugin-vue-components/vite';

export function getPluginsList() {
  return [
    // ... 其他插件
    Components({
      resolvers: [VantResolver()]
    })
  ];
}
```

### 7.2 移动端适配
添加 viewport 配置和 postcss-px-to-viewport（可选）：

```typescript
// vite.config.ts
export default {
  css: {
    postcss: {
      plugins: [
        // 可选：px 转 vw
        // postcssPxToViewport({ ... })
      ]
    }
  }
}
```

---

## 8. 浏览器兼容性

| 浏览器 | 最低版本 |
|--------|----------|
| Chrome | 最新 2 个版本 |
| Firefox | 最新 2 个版本 |
| Safari | 最新 2 个版本 |
| Edge | 最新 2 个版本 |
| iOS Safari | iOS 12+ |
| Android Chrome | Android 5+ |

---

## 9. Node.js 版本要求

```json
{
  "engines": {
    "node": "^20.19.0 || >=22.13.0",
    "pnpm": ">=9"
  }
}
```
