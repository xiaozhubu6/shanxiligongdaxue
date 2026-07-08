# 扫码点餐系统 (diancan-user)

## 项目简介
这是一个基于 uni-app 开发的扫码点餐系统，支持多端运行（H5、小程序、App等）。系统采用现代化的UI设计，提供流畅的点餐体验，并集成了即时通讯功能，实现订单实时提醒。

## 功能模块

### 1. 扫码点餐
- 路径：`pages/saoma/saoma`
- 功能：用户通过扫描餐桌二维码进入点餐系统
- 特点：快速便捷的点餐入口

### 2. 首页展示
- 路径：`pages/home-page/page`
- 功能：展示餐厅基本信息、特色菜品等
- 特点：美观的UI设计，突出餐厅特色

### 3. 人数选择
- 路径：`pages/index/index`
- 功能：用户选择就餐人数
- 特点：自定义导航栏设计

### 4. 订单详情
- 路径：`pages/order-details/details`
- 功能：展示订单详细信息
- 特点：清晰的订单状态展示

## 技术架构

### 前端框架
- uni-app：跨平台开发框架
- Vue.js：前端MVVM框架
- GoEasy：即时通讯服务

### 项目结构
```
├── pages/                # 页面文件
│   ├── saoma/           # 扫码点餐
│   ├── home-page/       # 首页
│   ├── index/           # 人数选择
│   └── order-details/   # 订单详情
├── static/              # 静态资源
├── style/               # 样式文件
├── utils/               # 工具函数
├── config/              # 配置文件
└── App.vue             # 应用入口组件
```

### 主要依赖
- Vue.js
- GoEasy（即时通讯）
- uni-app 框架

## 开发环境
- Node.js
- HBuilderX
- Vue.js

## 使用说明
1. 安装依赖：
```bash
npm install
```

2. 运行项目：
- 使用 HBuilderX 打开项目
- 选择运行到浏览器或模拟器

## 注意事项
1. 使用前需要配置 GoEasy 的 appkey
2. 确保网络环境正常，以保证即时通讯功能正常运行

## 后续优化计划
1. 添加用户评价功能
2. 优化订单状态更新机制
3. 增加更多支付方式
4. 优化页面加载速度

## 贡献指南
欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 许可证
MIT License 