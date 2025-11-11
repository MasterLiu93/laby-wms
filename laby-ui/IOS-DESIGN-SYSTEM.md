# 🍎 iOS 风格设计系统

## 设计理念

本系统采用 **Apple iOS 设计语言**，核心理念：

- ✨ **极简主义** - 简洁纯粹的视觉设计
- 🎨 **优雅流畅** - 柔和的圆角和过渡
- 💎 **轻盈透明** - 轻量的阴影和毛玻璃效果
- 🎯 **清晰层次** - 明确的视觉层级
- 📱 **原生感受** - 接近原生 iOS 的体验

---

## 🎨 iOS 配色方案

### 主色调

```scss
// iOS 蓝
--el-color-primary: #007aff;

// iOS 绿
--el-color-success: #34c759;

// iOS 橙
--el-color-warning: #ff9500;

// iOS 红
--el-color-danger: #ff3b30;

// iOS 灰
--el-color-info: #8e8e93;
```

### 背景色

```scss
// 亮色模式
--el-bg-color-page: #f2f2f7;   // 页面背景
--el-bg-color: #ffffff;         // 卡片背景
--el-fill-color: #f7f7f7;       // 填充背景

// 暗色模式
--el-bg-color-page: #000000;    // 页面背景
--el-bg-color: #1c1c1e;         // 卡片背景
--el-fill-color: #2c2c2e;       // 填充背景
```

---

## 🎯 iOS 按钮

### 实心按钮（Primary）

最常用的主要操作按钮：

```vue
<el-button type="primary">确认</el-button>
<el-button type="success">保存</el-button>
<el-button type="warning">警告</el-button>
<el-button type="danger">删除</el-button>
```

**特点：**
- ✅ 圆角 12px
- ✅ 纯色背景
- ✅ 悬停 scale(1.02)
- ✅ 点击 scale(0.98)
- ✅ 轻量阴影

---

### 填充按钮（Default）

适合次要操作：

```vue
<el-button>导出</el-button>
<el-button>重置</el-button>
<el-button>取消</el-button>
```

**特点：**
- ✅ 浅灰背景 `#f2f2f7`
- ✅ 主色文字 `#007aff`
- ✅ 无边框设计
- ✅ 悬停变深

---

### 文字按钮（Text）

表格操作专用：

```vue
<el-button type="primary" text>编辑</el-button>
<el-button type="danger" text>删除</el-button>
<el-button type="info" text>查看</el-button>
```

**特点：**
- ✅ 透明背景
- ✅ 彩色文字
- ✅ 悬停半透明背景
- ✅ 轻量不突兀

---

### 幽灵按钮（Plain）

轻量边框设计：

```vue
<el-button type="primary" plain>新建</el-button>
<el-button type="success" plain>提交</el-button>
```

**特点：**
- ✅ 细边框
- ✅ 透明背景
- ✅ 半透明悬停

---

## 📋 iOS 表格

### 基础表格

```vue
<el-table :data="list">
  <el-table-column type="selection" width="55" />
  <el-table-column prop="name" label="名称" />
  <el-table-column label="操作" width="180">
    <template #default>
      <el-button type="primary" text>编辑</el-button>
      <el-button type="danger" text>删除</el-button>
    </template>
  </el-table-column>
</el-table>
```

**特点：**
- ✅ 16px 大圆角
- ✅ 轻量阴影
- ✅ 浅灰表头 `#fafafa`
- ✅ 细分隔线
- ✅ 圆角复选框（6px）

---

## 📝 iOS 表单

### 输入框

```vue
<el-input v-model="value" placeholder="请输入内容" />
```

**特点：**
- ✅ 10px 圆角
- ✅ 浅灰背景 `#f2f2f7`
- ✅ 无边框
- ✅ 聚焦时白色背景
- ✅ 聚焦时蓝色外发光

---

### 复选框

```vue
<el-checkbox v-model="checked">选项</el-checkbox>
```

**特点：**
- ✅ 20x20 尺寸
- ✅ 6px 圆角
- ✅ iOS 蓝勾选色
- ✅ 悬停蓝色边框

---

## 🃏 iOS 卡片

```vue
<el-card>
  <template #header>
    <h3>卡片标题</h3>
  </template>
  卡片内容
</el-card>
```

**特点：**
- ✅ 16px 大圆角
- ✅ 轻盈阴影 `0 2px 16px rgba(0,0,0,0.04)`
- ✅ 无边框
- ✅ 分隔线浅灰

---

## 🏷️ iOS 标签

```vue
<el-tag type="primary">标签</el-tag>
<el-tag type="success">成功</el-tag>
<el-tag type="warning">警告</el-tag>
<el-tag type="danger">错误</el-tag>
```

**特点：**
- ✅ 8px 圆角
- ✅ 半透明背景（12% 透明度）
- ✅ 无边框
- ✅ 彩色文字

**示例：**
```scss
// Primary 标签
background: rgba(0, 122, 255, 0.12);
color: #007aff;
```

---

## 💬 iOS 对话框

```vue
<el-dialog v-model="visible" title="对话框">
  <el-form>
    <!-- 表单内容 -->
  </el-form>
  <template #footer>
    <el-button>取消</el-button>
    <el-button type="primary">确认</el-button>
  </template>
</el-dialog>
```

**特点：**
- ✅ 20px 超大圆角
- ✅ 毛玻璃效果 `backdrop-filter: blur(20px)`
- ✅ 深阴影
- ✅ 分隔线设计

---

## 📄 iOS 分页

```vue
<el-pagination
  :current-page="currentPage"
  :page-size="pageSize"
  :total="total"
  @current-change="handlePageChange"
/>
```

**特点：**
- ✅ 8px 圆角按钮
- ✅ 透明背景
- ✅ 悬停浅灰背景
- ✅ 选中蓝色背景

---

## 🎨 设计规范

### 圆角规范

```scss
小圆角：8px   // 标签、分页
中圆角：10px  // 输入框
基础圆角：12px // 按钮
卡片圆角：16px // 卡片、表格
大圆角：20px   // 对话框
超大圆角：24px // Round 按钮
```

### 阴影规范

```scss
轻阴影：0 2px 16px rgba(0,0,0,0.04)   // 卡片、表格
中阴影：0 4px 16px rgba(0,0,0,0.08)   // 对话框
重阴影：0 8px 24px rgba(0,0,0,0.12)   // 消息提示
```

### 动画规范

```scss
过渡时间：0.2s
缓动函数：cubic-bezier(0.25, 0.46, 0.45, 0.94)
悬停缩放：scale(1.02)
点击缩放：scale(0.98)
```

### 字体规范

```scss
标题：18px / 600
正文：15px / 500
说明：14px / 500
辅助：13px / 400
```

---

## 📱 完整示例

### 工具栏

```vue
<template>
  <div class="toolbar">
    <el-button type="primary">新建</el-button>
    <el-button>导出</el-button>
    <el-button>刷新</el-button>
    <el-button type="primary">搜索</el-button>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}
</style>
```

---

### 表格页面

```vue
<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">搜索</el-button>
          <el-button>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <el-button type="primary">新建</el-button>
      <el-button>导出</el-button>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button type="primary" text>编辑</el-button>
            <el-button type="danger" text>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="10"
        :total="total"
        class="mt-4"
      />
    </el-card>
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
  background: #f2f2f7;
  min-height: 100vh;
}

.search-card,
.toolbar-card,
.table-card {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}
</style>
```

---

## 🌗 暗色模式

系统完整支持 iOS 风格暗色模式，颜色会自动切换：

- 🌑 **背景** - 纯黑 `#000000` / 深灰 `#1c1c1e`
- 🔵 **主色** - 亮蓝 `#0a84ff`
- ⚪ **文字** - 白色 `#ffffff` / 浅灰 `#f2f2f7`
- 🔳 **边框** - 深灰系列

---

## 💡 最佳实践

### ✅ 推荐做法

1. **工具栏按钮**
   ```vue
   <el-button type="primary">主操作</el-button>
   <el-button>次要操作</el-button>
   ```

2. **表格操作**
   ```vue
   <el-button type="primary" text>编辑</el-button>
   <el-button type="danger" text>删除</el-button>
   ```

3. **表单提交**
   ```vue
   <el-button type="primary">提交</el-button>
   <el-button>取消</el-button>
   ```

### ❌ 避免做法

1. ❌ 过多使用 Primary 按钮
2. ❌ 表格操作使用实心按钮
3. ❌ 混用多种按钮风格
4. ❌ 自定义圆角破坏统一性

---

## 🎉 总结

iOS 风格设计系统的优势：

✅ **极简优雅** - 简洁纯粹的视觉设计  
✅ **统一规范** - 统一的圆角、阴影、配色  
✅ **流畅动画** - 精心调优的过渡效果  
✅ **完整适配** - 亮色/暗色双主题  
✅ **原生体验** - 接近 Apple 原生 App 的感受

现在您的系统拥有了完整的 iOS 风格！🍎


