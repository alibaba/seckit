# 阿里巴巴安全SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba/seckit.svg)](https://search.maven.org/artifact/com.alibaba/seckit)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

`seckit`提供多种安全防护功能，帮助开发者防范常见的安全漏洞。该SDK通过`SecurityUtil`类提供统一的入口，支持JDBC连接串过滤、SSRF攻击防护、XXE攻击防护等核心安全功能。

## 功能特性

### 🔒 JDBC连接串安全过滤
- 自动过滤JDBC连接串中的危险参数，移除不安全的参数如`allowLocalInfile`、`autoDeserialize`等
- 防止反序列化漏洞和SQL注入攻击
- 支持多种数据库类型：MySQL、PostgreSQL、Oracle、SQL Server、ClickHouse等

### 🛡️ SSRF攻击防护
- 检测和阻止服务器端请求伪造攻击
- 支持多种HTTP客户端：Apache HttpClient 4.x/5.x、OkHttp 2/3
- 提供NetHook方式的线程级别SSRF检查

### 🚫 XXE攻击防护
- 为XML解析器提供XXE攻击防护
- 支持多种XML解析框架：SAXReader、DocumentBuilderFactory、SAXParserFactory等
- 自动禁用外部实体引用和DTD处理
- 防止XML外部实体注入攻击

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>seckit</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle依赖

```gradle
implementation 'com.alibaba:seckit:0.0.1'
```

> 注意:
> 1. 本工具需要 JDK 8 以上版本

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
import com.alibaba.seckit.JdbcURLException;

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

> ⚠️ 对于 JDK 16 及以上版本，由于JDK默认禁止了外部包通过反射访问 jdk 内部的 protected/private 成员，而安全工具的防护能力需要访问到这些成员，因此需要在虚拟机参数中添加`--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED`


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

| XML解析器 | 所属框架 | 适配的类路径                                                        |
|----------|---------|---------------------------------------------------------------|
| **SAXReader** | Dom4j | `org.dom4j.io.SAXReader`                                      |
| **DocumentBuilderFactory** | JAXP | `javax.xml.parsers.DocumentBuilderFactory`                    |
| **SAXParserFactory** | JAXP | `javax.xml.parsers.SAXParserFactory`                          |
| **XMLInputFactory** | StAX | `javax.xml.stream.XMLInputFactory`                            |
| **SAXBuilder** | JDOM | `org.jdom2.input.SAXBuilder`                                  |
| **Validator** | JAXP | `javax.xml.validation.Validator`                              |
| **XMLReader** | SAX | `org.xml.sax.XMLReader` `jdk.internal.org.xml.sax.XMLReader"` |
| **TransformerFactory** | JAXP | `javax.xml.transform.TransformerFactory`                      |
| **SchemaFactory** | JAXP | `javax.xml.validation.SchemaFactory`                          |

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


## 许可证

本项目采用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) 许可证。

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目。

## 联系方式

- 项目主页: [GitHub Repository](https://github.com/alibaba/seckit)
- 问题反馈: [Issues](https://github.com/alibaba/seckit/issues)
