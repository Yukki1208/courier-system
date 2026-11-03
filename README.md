# 快递管理系统（Java + MySQL）

本科《Java 程序设计 / 数据库》随堂作业：一个只用 **Java SE + JDBC + MySQL** 实现的控制台版快递管理系统，不依赖任何 Web 框架。

## 一、功能

| 序号 | 功能 | 说明 |
| --- | --- | --- |
| 1 | 录入快递 | 输入运单号、寄件人、收件人等信息，状态自动为“待取件” |
| 2 | 查看全部 | 按录入顺序倒序列出所有快递 |
| 3 | 按运单号查询 | 显示一条快递的完整信息 |
| 4 | 修改状态 | 待取件 → 运输中 → 已签收 |
| 5 | 删除快递 | 按运单号删除，删除前需二次确认 |
| 6 | 统计 | 按状态分组统计件数 |

## 二、运行环境

- JDK 8 及以上（本机已验证 JDK 26 可编译运行）
- MySQL 5.7 / 8.0
- 开发工具：IDEA（推荐）或任意编辑器 + 命令行

## 三、运行步骤

### 第 1 步：建库建表

在命令行执行（或把 `sql/courier.sql` 整个复制到 Navicat / MySQL Workbench 里执行）：

```bash
mysql -u root -p < sql/courier.sql
```

执行完会创建数据库 `courier_db`、表 `courier`，并插入 3 条测试数据。

### 第 2 步：修改数据库密码

打开 `src/main/java/com/example/courier/DBUtil.java`，把 `PASSWORD` 改成自己电脑上 MySQL 的密码：

```java
private static final String USER = "root";
private static final String PASSWORD = "123456";   // ← 改成你自己电脑上的 MySQL 密码
```

### 第 3 步：运行

**方式 A：用 IDEA（推荐）**

1. `File → Open` 选择本文件夹，等待 IDEA 自动下载 Maven 依赖（需要联网，只下载 MySQL 驱动）；
2. 打开 `Main.java`，点击 `main` 方法左边的绿色三角运行。

**方式 B：命令行（Maven）**

```bash
mvn compile
mvn exec:java
```

**方式 C：不用 Maven**

1. 下载 `mysql-connector-j-8.0.33.jar`（Maven 中央仓库可搜到），在项目根目录新建 `lib` 文件夹并放进去；
2. 编译运行：

```bash
javac -encoding UTF-8 -cp "lib/mysql-connector-j-8.0.33.jar" -d out src/main/java/com/example/courier/*.java
java -cp "out;lib/mysql-connector-j-8.0.33.jar" com.example.courier.Main
```

> Windows 下 classpath 分隔符是 `;`，Linux / macOS 是 `:`。

## 四、目录结构

```
courier-system/
├─ pom.xml                     Maven 配置（只依赖 MySQL 驱动）
├─ README.md                   本说明
├─ sql/courier.sql             建库建表 + 测试数据
└─ src/main/java/com/example/courier/
   ├─ DBUtil.java              数据库连接工具类
   ├─ Courier.java             快递实体类（对应 courier 表）
   ├─ CourierDao.java          数据访问层，所有 SQL 都在这里
   └─ Main.java                程序入口 + 控制台菜单
```

## 五、设计说明

- **分层**：`Main`（界面）→ `CourierDao`（SQL）→ `DBUtil`（连接）→ MySQL，职责清晰，共 4 个类、约 330 行代码。
- **防 SQL 注入**：所有带参数的 SQL 都用 `PreparedStatement` 的 `?` 占位符传值，没有拼接字符串。
- **资源释放**：统一使用 try-with-resources，`Connection / Statement / ResultSet` 用完自动关闭。
- **中文不乱码**：数据库用 `utf8mb4`，连接串带 `characterEncoding=utf8`，源文件统一 UTF-8 编码。

## 六、常见问题

| 现象 | 原因与解决 |
| --- | --- |
| `找不到 MySQL 驱动` | 缺少 `mysql-connector-j` 依赖，检查 pom.xml 或 lib 目录 |
| `Access denied for user 'root'` | `DBUtil` 里的密码不对，改成自己电脑的密码 |
| `Unknown database 'courier_db'` | 没有执行 `sql/courier.sql` 建库 |
| `The server time zone value ... is unrecognized` | 连接串少了 `serverTimezone=Asia/Shanghai`（本项目已加） |
| 控制台中文显示为乱码 | 用 IDEA 运行；或 `java -Dfile.encoding=UTF-8 -cp ... Main`；或 `chcp 65001` 后运行 |
| 运行后没有任何输出就结束 | 检查是否用 IDE 的运行按钮启动的是 `Main` 类 |

## 七、实验报告

同目录下的 `快递管理系统-实验报告.docx`，可直接填写姓名学号后提交，其中“运行结果”一节的截图请换成本机运行截图。
