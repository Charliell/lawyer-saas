# Lawyer SaaS Platform

律师SaaS平台

## 技术栈

- **Java**: JDK 8 (LTS)
- **Spring Boot**: 2.7.18
- **Spring Framework**: 5.3.39
- **MyBatis Plus**: 3.5.7
- **Database**: MySQL 5.7/8.0+
- **Cache**: Redis 5.0/6.0/7.0

## 项目结构

```
lawyer-saas/
├── yudao-dependencies/     # Maven BOM - Central dependency management
├── yudao-framework/       # Core framework extensions and starters
├── yudao-server/          # Main application entry point
├── yudao-module-system/    # System management (users, roles, permissions)
├── yudao-module-infra/     # Infrastructure (files, config, jobs)
└── sql/                    # Database initialization scripts
```

## 快速开始

1. 克隆项目
2. 配置数据库连接
3. 运行数据库初始化脚本
4. 启动应用程序

## 开发指南

详细开发指南请参考项目内部文档。

## 许可证

MIT License