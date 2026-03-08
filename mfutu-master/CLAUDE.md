# mfutu - 个人股票持仓报价系统

## 项目概述

mfutu 是一个基于 Spring Boot 的个人股票持仓报价系统，定期抓取港股、美股价格，帮助用户随时思考投资策略。

## 技术栈

- **Java**: 11 (支持 8/17)
- **Spring Boot**: 2.4.1
- **MyBatis**: 1.3.2
- **MySQL**: 数据持久化
- **Thymeleaf**: 前端模板引擎
- **Futu OpenAPI SDK**: 港股行情数据源
- **Twelve Data API**: 美股行情数据源

## 项目结构

```
mfutu-master/
├── mfutu/                    # Maven 父项目配置
├── mfutu-app/                # Spring Boot 主应用
│   ├── src/main/java/com/ivan/mfutu/
│   │   ├── MFutuApplication.java      # 主入口
│   │   ├── controller/                # 控制器层
│   │   │   └── SubBasicQotController.java
│   │   ├── service/                   # 服务层
│   │   │   ├── FutuService.java
│   │   │   └── impl/FutuServiceImpl.java
│   │   ├── entity/                    # 实体类
│   │   │   ├── FutuData.java
│   │   │   ├── Category.java
│   │   │   └── SubBasicQot.java
│   │   ├── mapper/                    # MyBatis Mapper
│   │   └── util/                      # 工具类
│   │       ├── MyFutuUtil.java        # 富途 API 封装
│   │       └── MyTwelvedataUtil.java  # Twelve Data API 封装
│   └── src/main/resources/
│       ├── application.yml            # 主配置
│       ├── application-dev.yml        # 开发环境配置
│       ├── application-prd.yml        # 生产环境配置
│       ├── schema/                    # 数据库脚本
│       └── templates/                 # Thymeleaf 模板
└── mfutu-web/                # Web 模块
```

## 核心功能

### 数据源

| 市场 | 数据源 | Market Code |
|------|--------|-------------|
| 港股 | Futu OpenAPI | 1 |
| 美股 | Twelve Data API | 2 |
| A股 | Futu OpenAPI | 3 |

### 定时任务

- **港股更新**: 北京时间 9:00-16:00，每 5 分钟获取一次行情
- **美股更新**: 北京时间 21:00-05:00，每小时获取一次行情

配置项:
- `futu.subscribed.update.fixedDelay`: 更新间隔 (毫秒)
- `futu.subscribed.update.initialDelay`: 初始延迟 (毫秒)

## API 端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/` | GET | 首页 |
| `/home` | GET | 持仓数据展示 |
| `/manage` | GET | 数据管理页面 |
| `/subBasicQot` | GET | 订阅行情展示 (自动选择港股/美股) |
| `/subBasicQot/api/list` | GET | 行情 JSON API |
| `/sync?market=1\|2` | GET | 同步持仓数据 |
| `/getBasicQot?market=1\|2&code=XXX` | GET | 获取单只股票行情 |

## 数据库表

### sub_basic_qot (订阅行情)
| 字段 | 类型 | 描述 |
|------|------|------|
| code | varchar(100) | 股票代码 (PK) |
| market | int | 市场 (1:港股, 2:美股, 3:A股) |
| name | varchar(45) | 股票名称 |
| cur_price | float | 当前价格 |
| last_close_price | float | 昨收价 |
| increase_rate | float | 涨跌幅 (%) |
| update_time | datetime | 更新时间 |

### category (股票分类)
| 字段 | 类型 | 描述 |
|------|------|------|
| code | varchar(50) | 股票代码 (PK) |
| name | varchar(200) | 股票名称 |
| industry | varchar(100) | 行业 |
| market | varchar(50) | 市场 |
| level | varchar(20) | 级别 |
| currency | varchar(10) | 币种 |

### futu_data (持仓数据)
| 字段 | 类型 | 描述 |
|------|------|------|
| id | int | 自增主键 |
| code | varchar(20) | 股票代码 |
| name | varchar(100) | 股票名称 |
| price | float | 当前价格 |
| my_avg_price | float | 持仓成本 |
| quantity | int | 持仓数量 |
| pl_value | float | 盈亏金额 |
| pl_ratio | float | 盈亏比例 |
| pl_date | date | 记录日期 |

## 配置说明

### application-dev.yml 关键配置

```yaml
server:
  port: 8080

futu:
  opend: 192.168.3.53    # Futu OpenD 地址
  port: 11111            # Futu OpenD 端口

spring:
  datasource:
    url: jdbc:mysql://host:3306/mfutu
    username: root
    password: 123456
```

## 构建与运行

```bash
# 构建
cd mfutu-app
mvn clean package -DskipTests

# 运行 (开发环境)
java -jar target/mfutu-app-0.0.1-SNAPSHOT.jar

# 运行 (生产环境)
java -jar target/mfutu-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prd
```

## 依赖说明

- `futu-api`: 富途 OpenAPI Java SDK，用于港股/A股行情和交易
- `protobuf-java`: Protocol Buffers，Futu API 通信协议
- `fastjson`: JSON 解析，处理 Twelve Data API 响应

## 注意事项

1. 港股行情需要本地运行 Futu OpenD 网关
2. 美股行情使用 Twelve Data API，需要配置 API Key
3. 数据库使用 UTC 时区，展示时调整为 +8 时区
4. 最大订阅股票数量限制为 50 只 (`max_sub_basic_qot_num`)