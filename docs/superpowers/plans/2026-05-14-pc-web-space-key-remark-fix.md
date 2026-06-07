# PC Web端备注输入框空格键Bug修复计划

**Goal:** 修复PC web端备注输入框中无法输入空格、按空格键会打开/收起常用备注选择框的bug。

**根因分析:** Element Plus的`el-popover(trigger="click")`内部input元素按空格键时，键盘事件会冒泡到reference元素，被当作click事件触发popover切换。阻止空格键事件冒泡即可修复。

**影响范围:**
1. 收支记录页面 - 新增/编辑弹窗中的备注输入框 (BillForm.vue)
2. 收支记录页面 - 查询条件中的备注输入框 (BillFilter.vue)
3. 账单统计报表 - 查询条件中的备注输入框 (ReportFilter.vue)
4. 分类统计报表 - 查询条件中的备注输入框 (ReportFilter.vue)

**修复方案:** 在相关的`el-input`上添加`@keydown.space.stop`阻止空格键事件冒泡。

**修改文件:**
- `bk-pc/src/views/bill/components/BillForm.vue`
- `bk-pc/src/views/bill/components/BillFilter.vue`
- `bk-pc/src/views/report/components/ReportFilter.vue`

---

## 执行步骤

### Step 1: 修改 BillForm.vue
在新增/编辑弹窗的备注输入框添加 `@keydown.space.stop`

### Step 2: 修改 BillFilter.vue
在查询条件备注输入框添加 `@keydown.space.stop`

### Step 3: 修改 ReportFilter.vue
在报表查询条件备注输入框添加 `@keydown.space.stop`

### Step 4: 验证修复
启动开发服务器，在以下场景测试：
- 收支记录页面新增/编辑弹窗的备注输入框可正常输入空格
- 收支记录页面查询条件的备注输入框可正常输入空格
- 账单统计报表查询条件的备注输入框可正常输入空格
- 分类统计报表查询条件的备注输入框可正常输入空格
- 空格键不再触发常用备注选择框的打开/收起

### Step 5: 提交并推送
```bash
git add bk-pc/src/views/bill/components/BillForm.vue bk-pc/src/views/bill/components/BillFilter.vue bk-pc/src/views/report/components/ReportFilter.vue
git commit -m "fix: 修复PC端备注输入框空格键触发popover切换的问题

在BillForm、BillFilter、ReportFilter的备注输入框上添加@keydown.space.stop，
阻止空格键事件冒泡到el-popover的reference元素，避免按空格时打开/收起常用备注选择框。"
git push
```
