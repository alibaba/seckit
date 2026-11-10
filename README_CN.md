简体中文 | [English](README.md)
# 阿里巴巴安全SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba/seckit.svg)](https://search.maven.org/artifact/com.alibaba/seckit)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

`seckit`提供多种安全防护功能，帮助开发者防范常见的安全漏洞。该SDK通过`SecurityUtil`类提供统一的入口，支持JDBC连接串过滤、SSRF攻击防护、XXE攻击防护等核心安全功能。

## 功能特性

### [JDBC连接串安全过滤](#1-jdbc连接串过滤)
- 过滤JDBC连接串中的危险参数，移除不安全的参数如`allowLocalInfile`、`autoDeserialize`等
- 防止反序列化漏洞和SQL注入攻击
- 支持多种数据库类型：MySQL、PostgreSQL、Oracle、SQL Server、ClickHouse等

### [SSRF攻击防护](#2-ssrf攻击防护)
- 检测和阻止服务器端请求伪造攻击
- 支持多种HTTP客户端：Apache HttpClient 4.x/5.x、OkHttp 2/3
- 提供NetHook方式的线程级别SSRF检查

### [XXE攻击防护](#3-xxe攻击防护)
- 为XML解析器提供XXE攻击防护
- 支持多种XML解析框架：SAXReader、DocumentBuilderFactory、SAXParserFactory等
- 自动禁用外部实体引用和DTD处理
- 防止XML外部实体注入攻击

### [SQL转义](#4-sql转义)
- 用户输入转义与SQL字符裁剪

### [前端安全](#5-前端安全)
- HTML 转义
- JavaScript 转义

### [反序列化安全](#6-反序列化安全)
- readObject 白名单

### [命令参数裁剪](#7-命令参数裁剪)
- 按字符白名单裁剪，支持中文

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>seckit</artifactId>
    <version>0.1.1</version>
</dependency>
```

### Gradle依赖

```gradle
implementation 'com.alibaba:seckit:0.1.1'
```

## 使用指南

### 1. JDBC连接串过滤

#### 支持的数据库类型

| 数据库类型 | 连接串样式 |
| --------- | --------- |
| **MySQL** | `jdbc:mysql:`, `jdbc:mariadb:`, `jdbc:gbase:`, `jdbc:oceanbase:`, `jdbc:mysql2:`, `jdbc:mysql+srv:`, `mysqlx:`, `mysqlx+srv:` |
| **PostgreSQL** | `jdbc:postgresql:`, `jdbc:kingbase8:`, `jdbc:polardb:`, `jdbc:opengauss:` |
| **Oracle** | `jdbc:oracle:thin:`, `jdbc:oracle:oci:`, `jdbc:oracle:kprb:` |
| **SQL Server** | `jdbc:sqlserver:` |
| **ClickHouse** | `jdbc:clickhouse:`, `jdbc:ch:` |
| **MongoDB** | `jdbc:mongodb:`, `jdbc:mongodb+srv:`, `mongodb:`, `mongodb+srv:` |
| **Redis** | `jdbc:redis:` |
| **Hive** | `jdbc:hive:` |
| **Hive2** | `jdbc:hive2:` |
| **BigQuery** | `jdbc:bigquery:` |
| **Elasticsearch** | `jdbc:es:`, `jdbc:elasticsearch:` |
| **OpenSearch** | `jdbc:opensearch:` |
| **DB2** | `jdbc:db2:` |
| **Teradata** | `jdbc:teradata:` |
| **AS400** | `jdbc:as400:` |
| **ArrowFlightSQL** | `jdbc:arrow-flight-sql:`, `jdbc:arrow-flight:` |
| **TDEngine** | `jdbc:taos:`, `jdbc:TAOS:`, `jdbc:taos-rs:` |
| **Lindorm** | `jdbc:lindorm:table:`, `jdbc:lindorm:tsdb:` |
| **Redshift** | `jdbc:redshift:`, `jdbc:redshift:iam:` |
| **Presto** | `jdbc:presto:` |
| **Trino** | `jdbc:trino:` |
| **Greenplum** | `jdbc:pivotal:greenplum:` |
| **Sybase** | `jdbc:sybase:Tds:` |
| **Informix** | `jdbc:informix-sqli:` |
| **NetSuite** | `jdbc:ns:` |
| **OTS** | `jdbc:ots:http:`, `jdbc:ots:https:` |
| **ODPS** | `jdbc:odps:http:`, `jdbc:odps:https:` |
| **Phoenix** | `jdbc:phoenix:thin:` |
| **Impala** | `jdbc:impala:` |
| **Kylin** | `jdbc:kylin:` |
| **Snowflake** | `jdbc:snowflake:` |
| **Vertica** | `jdbc:vertica:` |
| **SAP** | `jdbc:sap:` |
| **DM** | `jdbc:dm:` |

使用`SecurityUtil.filterJdbcConnectionSource()`方法过滤JDBC连接串中的危险参数：

```java
import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.jdbc.JdbcURLException;

public class JdbcExample {
    public void filterJdbcUrl() {
        try {
            // 原始连接串包含危险参数
            String originalUrl = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&allowLocalInfile=true&autoDeserialize=true&sessionVariables=abc&foo=bar";

            // 过滤后的安全连接串
            String safeUrl = SecurityUtil.filterJdbcConnectionSource(originalUrl);
            System.out.println("安全连接串: " + safeUrl);
            // 输出: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&allowLocalInfile=false&allowLoadLocalInfile=false&sessionVariables=abc

        } catch (JdbcURLException e) {
            System.err.println("JDBC URL解析失败: " + e.getMessage());
        }
    }
}
```

### 2. SSRF攻击防护

> 1. 使用SSRF防护功能需要在程序启动时执行 `SecurityUtil.initialize()` 进行初始化。
>
> 2. 该功能在Windows上不生效。
>
> 3. 对于 JDK 16 及以上版本，由于JDK默认禁止了外部包通过反射访问 jdk 内部的 protected/private 成员，而安全工具的防护能力需要访问到这些成员，因此需要在虚拟机参数中添加`--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED`

#### 支持的HTTP客户端

| HTTP客户端 | 支持版本 | 适配类路径 |
|-----------|---------|-----------|
| **Apache HttpClient** | 4.x/5.x | `org.apache.http.impl.client.HttpClientBuilder` `org.apache.hc.client5.http.impl.classic.HttpClientBuilder` |
| **Apache HttpAsyncClient** | 4.x/5.x | `org.apache.http.impl.nio.client.HttpAsyncClientBuilder` `org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder` |
| **OkHttp 2** | 2.7.5+ | `com.squareup.okhttp.OkHttpClient` |
| **OkHttp 3** | 3.4.9+ | `okhttp3.OkHttpClient$Builder` |

#### 2.1 Apache HTTP客户端SSRF检查

为HTTP客户端添加SSRF检查功能：

```java
import com.alibaba.seckit.SecurityUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;

public class SSRFExample {
    public void httpClientWithSSRF() throws Exception {
        // 为HttpClient添加SSRF检查
        HttpClient client = SecurityUtil.withSSRFChecking(HttpClients.custom()).build();

        // 安全的外部请求
        HttpResponse response = client.execute(new HttpGet("https://www.example.com"));
        System.out.println("状态码: " + response.getStatusLine().getStatusCode());

        // 危险的内网请求会被阻止
        try {
            client.execute(new HttpGet("http://127.0.0.1:8080"));
        } catch (SecurityException e) {
            System.out.println("SSRF攻击被阻止: " + e.getMessage());
        }
    }
}
```

#### 2.2 OkHttp客户端SSRF检查

```java
import com.alibaba.seckit.SecurityUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpSSRFExample {
    public void okHttpWithSSRF() throws Exception {
        // 为OkHttp客户端添加SSRF检查
        OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient.Builder()).build();

        // 安全请求
        Request request = new Request.Builder()
                .url("https://www.example.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("响应码: " + response.code());
        }
    }
}
```

### 3. XXE攻击防护

#### 支持的XML解析器

| XML解析器 | 所属框架 | 适配的类路径 |
|----------|---------|---------------------------------------------------------------|
| **SAXReader** | Dom4j | `org.dom4j.io.SAXReader` |
| **DocumentBuilderFactory** | JAXP | `javax.xml.parsers.DocumentBuilderFactory` |
| **SAXParserFactory** | JAXP | `javax.xml.parsers.SAXParserFactory` |
| **XMLInputFactory** | StAX | `javax.xml.stream.XMLInputFactory` |
| **SAXBuilder** | JDOM | `org.jdom2.input.SAXBuilder` |
| **Validator** | JAXP | `javax.xml.validation.Validator` |
| **XMLReader** | SAX | `org.xml.sax.XMLReader` `jdk.internal.org.xml.sax.XMLReader` |
| **TransformerFactory** | JAXP | `javax.xml.transform.TransformerFactory` |
| **SchemaFactory** | JAXP | `javax.xml.validation.SchemaFactory` |

为XML解析器添加XXE防护：

```java
import com.alibaba.seckit.SecurityUtil;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;

import java.io.InputStream;

public class XXEExample {
    public void parseXmlSafely() throws Exception {
        // 为SAXReader添加XXE防护
        SAXReader reader = SecurityUtil.withXxeProtection(new SAXReader());

        // 安全解析XML
        InputStream xmlStream = getClass().getResourceAsStream("/safe.xml");
        Document document = reader.read(xmlStream);

        System.out.println("XML解析成功: " + document.getRootElement().getName());
    }
}
```

### 4. SQL转义

提供对用户输入的安全过滤，适用于直接拼接 SQL 的历史代码场景（优先推荐使用预编译 PreparedStatement）。

```java
import com.alibaba.seckit.SecurityUtil;

// 将用户输入安全地转义后拼接到 SQL 字符串
String keyword = SecurityUtil.escapeSql(userInput);
String sql = "SELECT * FROM products WHERE name LIKE '%" + keyword + "%'";

// 严格裁剪仅允许安全字符集（适用于列名、order by等场景）
String orderBy = SecurityUtil.trimSql4OrderBy(userProvidedColumn);
String sql2 = "SELECT * FROM products ORDER BY " + orderBy + " DESC";
```

### 5. 前端安全

#### 5.1 HTML、Javascript 转义
对输出到 HTML、JavaScript 上下文的用户数据进行转义，避免跨站脚本注入。

```java
import com.alibaba.seckit.SecurityUtil;

String htmlText = SecurityUtil.escapeHtml(userInput);
String javascriptText = SecurityUtil.escapeJavascript(userInput);
```

#### 5.2 重定向检查

允许 `/login` 这类站内根路径，或域名匹配白名单的 HTTP(S) 地址。`//example.com/path` 这类省略协议的地址也必须匹配域名白名单。非法 URL、反斜杠、未编码的空白/控制字符和 userinfo 会被拒绝；排除列表优先。

```java
import com.alibaba.seckit.SecurityUtil;

boolean allowed = SecurityUtil.isRedirectUrlAllowed(redirectUrl, "*.example.com");

// 支持排除域名
boolean allowed2 = SecurityUtil.isRedirectUrlAllowed(redirectUrl,
        new String[]{"*.example.com"},
        new String[]{"evil.example.com"});
```

### 6. 反序列化安全

使用带白名单的 `ObjectInputStream`，限制可反序列化的类，降低反序列化攻击风险。按 Class 加白时，仅递归父类和非 static、非 transient 的实例字段类型。自定义 `writeObject`/`readObject` 或 `Externalizable` 可能写入额外类型，需要显式加白。

```java
import com.alibaba.seckit.SecurityUtil;
import java.io.ObjectInputStream;
import java.io.InputStream;

// 仅允许基础JDK类（默认白名单）
ObjectInputStream ois1 = SecurityUtil.newObjectInputStream(in);

// 通过字符串模式白名单
ObjectInputStream ois2 = SecurityUtil.newObjectInputStream(in,
        "com.myapp.safe.**", "org.example.model.*");

// 通过Class白名单
ObjectInputStream ois3 = SecurityUtil.newObjectInputStream(in,
        com.myapp.safe.Model.class, com.myapp.safe.Event.class);
```

### 7. 命令参数裁剪

`SecurityUtil.trimCmd` 保留英文字母、数字、`_`、`-`、`,`、`.`，以及 `U+4E00–U+9FBB` 范围内的中文字符。空白、Shell 元字符等其他字符会被删除。输入为 null 时返回 null，空字符串返回空字符串。

```java
String argument = SecurityUtil.trimCmd("中文 文件-01.txt"); // 中文文件-01.txt
String commaArgument = SecurityUtil.trimCmd("日志, 报表"); // 日志,报表
```

此方法仅裁剪单个参数，不是 Shell 转义，也不能校验完整命令。使用 `ProcessBuilder` 时应分别传入参数，并单独校验可执行文件和选项；开头的连字符仍会保留。

## 高级用法

### NetHook方式SSRF检查

使用NetHook方式对所有TCP连接进行SSRF检查：

```java
import com.alibaba.seckit.SecurityUtil;
import exception.ssrf.com.alibaba.seckit.SSRFUnsafeConnectionException;

import java.net.URL;

public class NetHookSSRFExample {
    public void netHookSSRF() {
        try {
            // 启动SSRF检查
            SecurityUtil.startSSRFNetHookChecking();

            // 执行网络请求
            URL url = new URL("https://www.example.com");
            url.openConnection().getInputStream();

        } catch (SSRFUnsafeConnectionException e) {
            System.err.println("检测到SSRF攻击: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
        } finally {
            // 停止SSRF检查
            SecurityUtil.stopSSRFNetHookChecking();
        }
    }
}
```

## 从源码构建

需要 Maven 3.6.3+、JDK 8u121 或更新的 JDK 8，以及 JDK 11+。**推荐在 `~/.m2/toolchains.xml` 中一次性配置两个 JDK**，配置示例见[工具链指南](CONTRIBUTING.md#configure-toolchains-once-recommended)，然后在仓库根目录执行：

```sh
mvn clean package
```

该命令会运行测试，生成同时包含 Java 8 和 Java 9+ 实现的多版本 JAR。工具链配置完成后，无需每次传入 JDK 路径 `-D` 参数。

```sh
mvn clean test
mvn -Dtest=SqlToolTest,UrlToolTest,DeserializeToolTest test
mvn -DskipTests package
```

`JAVA_HOME` 决定运行 Maven 和测试的 JDK，与编译器工具链相互独立。修改多版本代码后，应分别在 JDK 8 和较新的受支持 JDK 下运行测试。一次性构建或没有工具链配置的 CI 仍可用 `-Djdk8.home`、`-Djdk9plus.home` [显式覆盖路径](CONTRIBUTING.md#explicit-jdk-paths-override--ci)。完整配置、测试 JDK 矩阵和测试夹具注意事项见[贡献指南](CONTRIBUTING.md)。

## 许可证

本项目采用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) 许可证。

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目。请参阅[贡献指南](CONTRIBUTING.md)和[变更记录](CHANGELOG.md)。

安全问题请按[安全政策](SECURITY.md)报告，不要在公开Issue中发布漏洞利用细节。

## 联系方式

- 项目主页: [GitHub Repository](https://github.com/alibaba/seckit)
- 问题反馈: [Issues](https://github.com/alibaba/seckit/issues)
