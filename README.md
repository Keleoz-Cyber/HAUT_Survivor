# HAUT Survivor

Spring Boot + MyBatis-Plus + MySQL 的 HAUT Survivor Demo 基础版。

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

## 验证

```powershell
.\mvnw.cmd test
```
