# HAUT Survivor

Spring Boot + MyBatis-Plus + MySQL 的 HAUT Survivor 可玩 Demo。

## 运行

1. 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS haut_survivor
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

2. 确认 `src/main/resources/application.yml` 中的 MySQL 账号密码。当前默认：

```text
username: root
password: 123456
```

3. 启动：

```powershell
.\mvnw.cmd spring-boot:run
```

4. 打开：

```text
http://localhost:8080/login
```

Demo 账号：

```text
admin / admin123
student / student123
```

## Demo 演示路线

```text
登录 student/student123
  -> 创建或进入校园角色
  -> 打开校园地图
  -> 点击实验室触发剧情事件
  -> 选择事件处理方式并查看属性变化
  -> 进入“Java 课设：DDL 前夜”
  -> 在“需求风暴”阶段选择课设推进策略
  -> 在“数据库拼图”阶段勾选正确表关系，并受用时、技能值和前置选择影响结算
  -> 在“Bug 暴走”阶段选择收尾方案
  -> 查看副本阶段结算、过程标签和最终评价
```

当前 Java 课设副本已经不是简单任务清单。前置选择会写入 `scope_controlled`、`scope_sprawl` 等过程标签；数据库拼图会根据正确关系、错误关系、用时和角色属性生成 `schema_clear` 或 `schema_mist` 等后果；最终结局会综合总分和关键后果生成，例如“课设战神”或“答辩沉默现场”。

管理员可以使用 `admin/admin123` 进入事件管理页面，维护基础事件内容。

## 验证

```powershell
.\mvnw.cmd test
```
