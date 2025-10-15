package com.alibaba.seckit.jdbc;

import com.alibaba.seckit.SecurityUtil;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class FilterTest {

    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    @Test
    public void testMySQLFilter() throws MalformedURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3306/test?useUnicode=true" +
                "&characterEncoding=UTF-8&foo=bar&allowLocalInfile=true&rewriteBatchedStatements=1&sessionVariables=abc&ConnectTimeout=1&socketTimeout=2&serverTimezone=Asia/Shanghai");
        assertThat(str, startsWith("jdbc:mysql://localhost:3306/test?"));
        assertThat(str, containsString("useUnicode=true"));
        assertThat(str, containsString("characterEncoding=UTF-8"));
        assertThat(str, containsString("allowLocalInfile=false"));
        assertThat(str, containsString("allowLoadLocalInfile=false"));
        assertThat(str, containsString("rewriteBatchedStatements=1"));
        assertThat(str, containsString("sessionVariables=abc"));
        assertThat(str, containsString("socketTimeout=2"));
        assertThat(str, containsString("serverTimezone=Asia/Shanghai"));
        // test case-sensitive
        assertThat(str, not(containsString("ConnectTimeout=2")));
        assertThat(str, not(containsString("foo=bar")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:MySQL://localhost:3306/test?useUnicode=true" +
                "&characterEncoding=UTF-8&foo=bar&allowLocalInfile=true&rewriteBatchedStatements=1&sessionVariables=abc");
        assertThat(str, startsWith("jdbc:MySQL://localhost:3306/test?"));
        assertThat(str, containsString("sessionVariables=abc"));
        assertThat(str, not(containsString("foo=bar")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mariadb://localhost:3306/test?useUnicode=true" +
                "&characterEncoding=UTF-8&foo=bar&allowLocalInfile=true");
        assertThat(str, startsWith("jdbc:mariadb://localhost:3306/test?"));
        assertThat(str, containsString("useUnicode=true"));
        assertThat(str, containsString("characterEncoding=UTF-8"));
        assertThat(str, containsString("allowLocalInfile=false"));
        assertThat(str, containsString("allowLoadLocalInfile=false"));
        assertThat(str, not(containsString("foo=bar")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3306/test?serverTimezone=GMT+8");
        assertThat(str, containsString("serverTimezone=GMT+8"));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3306/test?serverTimezone=GMT%2B8");
        assertThat(str, containsString("serverTimezone=GMT%2B8"));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3306/test?serverTimezone=GMT;8");
        assertFalse(str.contains("serverTimezone"));
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3306/test?cachePrepStmts=true&useServerPrepStmts=true");
        assertTrue(str.contains("cachePrepStmts=true"));
        assertTrue(str.contains("useServerPrepStmts=true"));

        // test space in property key or property value
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3306/test? user = zhangsan");
        assertTrue(str.contains("?user=zhangsan") || str.contains("&user=zhangsan"));

        // test failover
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3306,example-domain.com:1234/test?user=zhangsan");
        assertTrue(str.contains("jdbc:mysql://localhost:3306,example-domain.com:1234/test"));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql:replication://localhost:3306,example-domain.com:1234/test?user=zhangsan");
        assertTrue(str.contains("jdbc:mysql:replication://localhost:3306,example-domain.com:1234/test"));
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql:loadbalance://localhost:3306,example-domain.com:1234/test?user=zhangsan");
        assertTrue(str.contains("jdbc:mysql:loadbalance://localhost:3306,example-domain.com:1234/test"));
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql2://localhost:3306,example-domain.com:1234/test?user=zhangsan");
        assertTrue(str.contains("jdbc:mysql2://localhost:3306,example-domain.com:1234/test"));

        // test special protocol
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysqlxxx://localhost:3306/db");
        assertTrue(str.contains("jdbc:mysqlxxx://localhost:3306"));
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql+srv://localhost:3306/db");
        assertTrue(str.contains("jdbc:mysql+srv://localhost:3306/db"));
        str = SecurityUtil.filterJdbcConnectionSource("mysqlx+srv://localhost:3306/db");
        assertTrue(str.contains("mysqlx+srv://localhost:3306/db"));

        str = SecurityUtil.filterJdbcConnectionSource("mysqlx://localhost:3306/db?enabledTLSProtocols=TLSv1,TLSv1.1,TLSv1.2");
        assertTrue(str.contains("enabledTLSProtocols=TLSv1,TLSv1.1,TLSv1.2"));
        str = SecurityUtil.filterJdbcConnectionSource("mysqlx://localhost:3306/db?enabledTLSProtocols=foo:bar");
        assertFalse(str.contains("enabledTLSProtocols=foo:bar"));

        // test $ and Chinese character in database name
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3307/中文$db?user=zhangsan&foo=bar");
        assertTrue(str.startsWith("jdbc:mysql://localhost:3307/中文$db"));
        assertTrue(str.contains("user=zhangsan"));
        assertFalse(str.contains("foo=bar"));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3307/this=is db#name");
        assertTrue(str.startsWith("jdbc:mysql://localhost:3307/this=is db#name"));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:mysql://localhost:3307/ this=is db#name ?foo=bar");
        assertTrue(str.startsWith("jdbc:mysql://localhost:3307/ this=is db#name "));
    }

    // https://clickhouse.com/docs/en/integrations/java
    @Test
    public void testClickHouseFilter() throws MalformedURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:clickhouse://localhost:8123/test?" +
                "useUnicode=true&socket_timeout=-1&foo=bar");
        assertThat(str, startsWith("jdbc:clickhouse://localhost:8123/test?"));
        assertThat(str, not(containsString("useUnicode=true")));
        assertThat(str, containsString("socket_timeout=-1"));
        assertThat(str, not(containsString("foo=bar")));
        assertThat(str, not(containsString("allowLocalInfile=false")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:ch:http://server1.domain,server2.domain,server3.domain");
        assertEquals("jdbc:ch:http://server1.domain,server2.domain,server3.domain", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:ch:http://endpoint1,server2.domain,server3.domain/db");
        assertEquals("jdbc:ch:http://endpoint1,server2.domain,server3.domain/db", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:ch:http://endpoint1,server2.domain,server3.domain/db?server_time_zone=Asia/Shanghai");
        assertEquals("jdbc:ch:http://endpoint1,server2.domain,server3.domain/db?server_time_zone=Asia/Shanghai", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:ch:http://endpoint1,server2.domain,server3.domain/db?server_time_zone=%26foo=bar");
        assertEquals("jdbc:ch:http://endpoint1,server2.domain,server3.domain/db", str);

    }

    // https://jdbc.postgresql.org/documentation/use/
    @Test
    public void testPostgresFilter() throws MalformedURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:postgresql://localhost:5432/test?" +
                "useUnicode=true&stringtype=abc&socket_timeout=-1&foo=bar&user=postgres&password=postgres&socketTimeout=123");
        assertThat(str, startsWith("jdbc:postgresql://localhost:5432/test?"));
        // assertThat(str, containsString("useUnicode=true"));
        assertThat(str, containsString("user=postgres"));
        assertThat(str, containsString("stringtype=abc"));
        assertThat(str, containsString("socketTimeout=123"));
        assertThat(str, not(containsString("socket_timeout=-1")));
        assertThat(str, not(containsString("foo=bar")));
        assertThat(str, not(containsString("allowLocalInfile=false")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:polardb://localhost:5432/test?" +
                "useUnicode=true&stringtype=abc&socket_timeout=-1&foo=bar&user=postgres&password=postgres&socketTimeout=123");
        assertThat(str, startsWith("jdbc:polardb://localhost:5432/test?"));
        assertThat(str, containsString("user=postgres"));
        assertThat(str, containsString("stringtype=abc"));
        assertThat(str, containsString("socketTimeout=123"));
        assertThat(str, not(containsString("socket_timeout=-1")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:opengauss://localhost:5432/test?" +
                "useUnicode=true&stringtype=abc&socket_timeout=-1&foo=bar&user=postgres&password=postgres&socketTimeout=123");
        assertThat(str, startsWith("jdbc:opengauss://localhost:5432/test?"));
        assertThat(str, containsString("user=postgres"));
        assertThat(str, containsString("stringtype=abc"));
        assertThat(str, containsString("socketTimeout=123"));
        assertThat(str, not(containsString("socket_timeout=-1")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:postgresql://[::1]:5432,localhost:5432/db");
        assertEquals("jdbc:postgresql://[::1]:5432,localhost:5432/db", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:postgresql://localhost:5432/test@aabbccdd?" +
                "useUnicode=true&stringtype=abc&socket_timeout=-1&foo=bar&user=postgres&password=postgres&socketTimeout=123");
        assertThat(str, startsWith("jdbc:postgresql://localhost:5432/test@aabbccdd?"));
        // assertThat(str, containsString("useUnicode=true"));
        assertThat(str, containsString("user=postgres"));
        assertThat(str, containsString("stringtype=abc"));
        assertThat(str, containsString("socketTimeout=123"));
        assertThat(str, not(containsString("socket_timeout=-1")));
        assertThat(str, not(containsString("foo=bar")));
        assertThat(str, not(containsString("allowLocalInfile=false")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:postgresql://hgpostcn-xxxxxx.hologres.aliyuncs.com:80/dbname?preferQueryMode=simple&conf:stsToken=" +
                "ZGNmdn%2BNnYmhAIy/QlYmFhZ3lobmptaw==&foo=bar&sslmode=true");
        assertThat(str, containsString("conf:stsToken=ZGNmdn%2BNnYmhAIy%2FQlYmFhZ3lobmptaw%3D%3D"));
        assertThat(str, containsString("sslmode=true"));
        assertThat(str, not(containsString("foo=bar")));
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:postgresql://hgpostcn-xxxxxx.hologres.aliyuncs.com:80/dbname?preferQueryMode=simple&conf:stsToken=" +
                "ZGNmdn%2BNnYmhAIy/QlYmFhZ3lobmptaw==&sslmode=true");
        assertThat(str, containsString("conf:stsToken=ZGNmdn%2BNnYmhAIy%2FQlYmFhZ3lobmptaw%3D%3D"));
        assertThat(str, containsString("sslmode=true"));

    }

    @Test
    public void testOceanbaseFilter() throws MalformedURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:oceanbase://localhost:13000/test?foo=bar&netTimeoutForStreamingResults=abc");
        // assertThat(str, containsString("netTimeoutForStreamingResults=abc"));
        assertThat(str, not(containsString("foo=bar")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:oceanbase://localhost:13000/test?user=aaa&foo=bar&sessionVariables=foo=bar&allowLocalInfile=true");
        assertEquals("jdbc:oceanbase://localhost:13000/test?sessionVariables=foo=bar&allowUrlInLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&user=aaa&allowLoadLocalInfile=false", str);
    }

    @Test
    public void testGbase() throws MalformedURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:gbase://localhost:13000/test?user=aaa&foo=bar&sessionVariables=foo=bar&allowLocalInfile=true");
        assertThat(str, not(containsString("allowLocalInfile")));
        assertEquals("jdbc:gbase://localhost:13000/test?sessionVariables=foo=bar&allowUrlInLocalInfile=false&autoDeserialize=false&user=aaa&allowLoadLocalInfile=false", str);
    }
    @Test
    public void testSAPFilter() throws MalformedURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:sap://localhost:3200/test?user=foo&reconnect=true");
        assertThat(str, containsString("reconnect=true"));
        assertThat(str, not(containsString("user=foo")));

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sap://localhost:3200;failover:1234/test?user=foo&reconnect=true");
        assertEquals( "jdbc:sap://localhost:3200;failover:1234/test?reconnect=true", str);
    }

    // https://www.ibm.com/docs/en/db2-for-zos/11?topic=cdsudidsdjs-url-format-data-server-driver-jdbc-sqlj-type-4-connectivity
    @Test
    public void testDB2Filter() throws MalformedURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:db2://localhost:50000/test:user=foo;reconnect=true;");
        assertEquals(str, "jdbc:db2://localhost:50000/test");

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:db2://localhost:50000/test:currentSchema=abc;reconnect=true;");
        assertEquals("jdbc:db2://localhost:50000/test:currentSchema=abc;", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:db2://1.2.3.4:1234/SAMPLE:currentSchema=TEST;");
        assertEquals("jdbc:db2://1.2.3.4:1234/SAMPLE:currentSchema=TEST;", str);
    }

    // https://www.ibm.com/docs/en/informix-servers/12.10?topic=method-format-database-urls
    @Test
    public void testInformixSqliFilter() throws MalformedURLException {
        JdbcTool tool = new JdbcToolImpl();
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:informix-sqli://localhost:9088/test:INFORMIXSERVER=abc;user=foo");
        assertEquals("jdbc:informix-sqli://localhost:9088/test:INFORMIXSERVER=abc", str);
    }

    // https://docs.oracle.com/en/cloud/saas/netsuite/ns-online-help/section_4425615742.html#Specifying-Connection-Properties
    @Test
    public void testNSFilter() throws MalformedURLException {
        JdbcTool tool = new JdbcToolImpl();
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:ns://localhost:9088/test;Encrypted=true;foo=bar");
        assertEquals("jdbc:ns://localhost:9088/test;Encrypted=true", str);
    }

    // https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver16
    @Test
    public void testSQLServerFilter() throws MalformedURLException {
        JdbcTool tool = new JdbcToolImpl();
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;database=test;foo=bar");
        assertEquals("jdbc:sqlserver://localhost:1433;database=test", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost\\instance;database=test;foo=bar");
        assertEquals("jdbc:sqlserver://localhost\\instance;database=test", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost\\instance:1433;database=test;foo=bar");
        assertEquals("jdbc:sqlserver://localhost\\instance:1433;database=test", str);

        // test ignore case
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;DATABASENAME=test;foo=bar");
        assertEquals("jdbc:sqlserver://localhost:1433;DATABASENAME=test", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName=test;foo=bar");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName=test", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName=test;failOverPartner=1.1.1.1");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName=test;failOverPartner=1.1.1.1", str);

        // test white space in value
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName = test ;foo=bar");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName=test", str);

        // test chinese database name
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName=中文数据库測試;database=中文;");
        assertEquals("jdbc:sqlserver://localhost:1433;database=中文;databaseName=中文数据库測試", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName=月明かり昇る刻");
        assertEquals("jdbc:sqlserver://localhost:1433", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName=中文$eng@foo;database=中文@foo$eng;");
        assertEquals("jdbc:sqlserver://localhost:1433;database=中文@foo$eng;databaseName=中文$eng@foo", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName=[test space];");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName=[test space]", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName= {abc foobar };");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName={abc foobar }", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName={aaa}}b};");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName={aaa}}b}", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName={abc}}};");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName={abc}}}", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName={abc{}}};");
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName={abc{}}}", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost\\instanceName:1433;foo=bar");
        assertEquals("jdbc:sqlserver://localhost\\instanceName:1433", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1234;encrypt=false;language=us_english;loginTimeout=3600;socketTimeout=3600000;databaseName=BS3000+_000_2014");
        assertEquals("jdbc:sqlserver://localhost:1234;databaseName=BS3000+_000_2014;encrypt=false;socketTimeout=3600000;language=us_english;loginTimeout=3600", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://ip:1433;databaseName=dataphin(POC)");
        assertEquals("jdbc:sqlserver://ip:1433;databaseName=dataphin(POC)", str);

        try {
            str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://xxxx abvc\\instanceName:1433;foo=bar");
            fail();
        } catch (JdbcURLUnsafeException ignored){}
        try {
            str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost\\xxx abc:1433;foo=bar");
            fail();
        } catch (JdbcURLUnsafeException ignored){}

        try {
            str = SecurityUtil.filterJdbcConnectionSource("jdbc:sqlserver://localhost:1433;databaseName=abc{foo};");
            fail();
        } catch (JdbcURLUnsafeException ignored) {}
    }

    @Test
    public void testOracleFilter() throws MalformedURLException {
        JdbcTool tool = new JdbcToolImpl();
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:oracle:thin:@tcp://mydbhost1,mydbhost2:1521/mydbservice?wallet_location=/work/wallet&ssl_server_cert_dn=\"Server DN\"");
        assertEquals("jdbc:oracle:thin:@tcp://mydbhost1,mydbhost2:1521/mydbservice", str);

        String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)  (HOST=mydbhost)(PORT=1521)) (CONNECT_DATA=(SERVICE_NAME=mydbservice)))";
        str = SecurityUtil.filterJdbcConnectionSource(url);
        assertEquals(url.replace(" ", ""), str);

        url = "jdbc:oracle:thin:@(DESCRIPTION= (LOAD_BALANCE=on) (FOO=bar) (ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=host1) (PORT=1521)) (ADDRESS=(PROTOCOL=TCPS)(HOST=host2)(PORT=5221)) (ADDRESS=(PROTOCOL=LDAP)(HOST=host3)(PORT=5221))) (CONNECT_DATA=(SERVICE_NAME=orcl) (FOO=bar)))";
        str = SecurityUtil.filterJdbcConnectionSource(url);
        // System.out.println(str);
        assertEquals("jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=host1)(PORT=1521))(ADDRESS=(PROTOCOL=TCPS)(HOST=host2)(PORT=5221))(ADDRESS=(HOST=host3)(PORT=5221)))(CONNECT_DATA=(SERVICE_NAME=orcl)))", str);

        url = "jdbc:oracle:thin:@(FOO=(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)  (HOST=mydbhost)(PORT=1521)) (CONNECT_DATA=(SERVICE_NAME=mydbservice))))";
        str = SecurityUtil.filterJdbcConnectionSource(url);
        assertEquals("jdbc:oracle:thin:@", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:oracle:thin:@127.0.0.1:1231:db_name-test");
        assertEquals("jdbc:oracle:thin:@127.0.0.1:1231:db_name-test", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:oracle:thin:@127.0.0.1:1231:db_name-test?foo=bar");
        assertEquals("jdbc:oracle:thin:@127.0.0.1:1231:db_name-test", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:Oracle:thin:@127.0.0.1:1231:db_name-test?oracle.net.CONNECT_TIMEOUT=123&foo=bar");
        assertEquals("jdbc:oracle:thin:@127.0.0.1:1231:db_name-test?oracle.net.CONNECT_TIMEOUT=123", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=111.222.123.123))(connect_data=(service_name=test.adb.oraclecloud.com))(security=(ssl_server_dn_match=no)))");
        assertEquals("jdbc:oracle:thin:@(description=(address=(protocol=tcps)(port=1521)(host=111.222.123.123))(connect_data=(service_name=test.adb.oraclecloud.com)))", str);

    }

    // https://github.com/DataGrip/redis-jdbc-driver
    @Test
    public void testRedisFilter() throws MalformedURLException {
        String url = SecurityUtil.filterJdbcConnectionSource("jdbc:redis://localhost:7890?user=aaa&pass=bbb&password=ccc");
        assertEquals("jdbc:redis://localhost:7890?password=ccc&user=aaa", url);
        url = SecurityUtil.filterJdbcConnectionSource("jdbc:redis://foo:bar@localhost?aaa=bbb&user=root");
        assertEquals("jdbc:redis://foo:bar@localhost?user=root", url);
    }

    // https://www.mongodb.com/docs/manual/reference/connection-string/
    @Test
    public void testMongodbFilter() throws MalformedURLException {
        String url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb://localhost?ssl=true;foo=bar&tls=true");
        assertEquals("jdbc:mongodb://localhost/?tls=true&ssl=true", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb+srv://localhost?ssl=true;foo=bar&tls=true");
        assertEquals("jdbc:mongodb+srv://localhost/?tls=true&ssl=true", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb+srv://admin:admin@localhost?ssl=true;foo=bar&tls=true");
        assertEquals("jdbc:mongodb+srv://admin:admin@localhost/?tls=true&ssl=true", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb://localhost/db?ssl=true;foo=bar&tls=true");
        assertEquals("jdbc:mongodb://localhost/db?tls=true&ssl=true", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb://localhost/db?ssl=true&authMechanismProperties=foo:bar");
        assertEquals("jdbc:mongodb://localhost/db?authMechanismProperties=foo:bar&ssl=true", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb://localhost,1.1.1.1:2377/db");
        assertEquals("jdbc:mongodb://localhost,1.1.1.1:2377/db", url);


        url = SecurityUtil.filterJdbcConnectionSource("mongodb://localhost?ssl=true;foo=bar&tls=true");
        assertEquals("mongodb://localhost/?tls=true&ssl=true", url);
        url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb+srv://localhost?ssl=true;foo=bar&tls=true");
        assertEquals("jdbc:mongodb+srv://localhost/?tls=true&ssl=true", url);
        url = SecurityUtil.filterJdbcConnectionSource("jdbc:mongodb+srv://admin:admin@localhost?ssl=true;foo=bar&tls=true");
        assertEquals("jdbc:mongodb+srv://admin:admin@localhost/?tls=true&ssl=true", url);
        url = SecurityUtil.filterJdbcConnectionSource("mongodb://localhost/db?ssl=true;foo=bar&tls=true");
        assertEquals("mongodb://localhost/db?tls=true&ssl=true", url);
        url = SecurityUtil.filterJdbcConnectionSource("mongodb://localhost/db?ssl=true&authMechanismProperties=foo:bar");
        assertEquals("mongodb://localhost/db?authMechanismProperties=foo:bar&ssl=true", url);
        url = SecurityUtil.filterJdbcConnectionSource("mongodb://localhost,1.1.1.1:2377/db");
        assertEquals("mongodb://localhost,1.1.1.1:2377/db", url);

        url = SecurityUtil.filterJdbcConnectionSource("mongodb://root:P%4033w04d@localhost,1.1.1.1:2377/db?ssl=true");
        assertEquals("mongodb://root:P%4033w04d@localhost,1.1.1.1:2377/db?ssl=true", url);
        url = SecurityUtil.filterJdbcConnectionSource("mongodb://root:p,[w<>],d@localhost,1.1.1.1:2377/db?ssl=true");
        assertEquals("mongodb://root:p,[w<>],d@localhost,1.1.1.1:2377/db?ssl=true", url);
        url = SecurityUtil.filterJdbcConnectionSource("mongodb+srv://localhost,1.1.1.1:2377/db");
        assertEquals("mongodb+srv://localhost,1.1.1.1:2377/db", url);

        url = SecurityUtil.filterJdbcConnectionSource("mongodb://root:foo#bar@localhost,1.1.1.1:2377/db");
        assertEquals("mongodb://root:foo#bar@localhost,1.1.1.1:2377/db", url);

        url = SecurityUtil.filterJdbcConnectionSource("mongodb://localhost,1.1.1.1:2377/?ssl=true");
        assertEquals("mongodb://localhost,1.1.1.1:2377/?ssl=true", url);

        try {
            url = SecurityUtil.filterJdbcConnectionSource("mongodb+srv://root:foobar?foo=bar@1.1.1.1/db");
            fail();
        } catch (JdbcURLUnsafeException ignored) {}

        try {
            url = SecurityUtil.filterJdbcConnectionSource("mongodb+srv://root:foobar:foo=bar@1.1.1.1/db");
            fail();
        } catch (JdbcURLUnsafeException ignored) {}
    }

    // https://teradata-docs.s3.amazonaws.com/doc/connectivity/jdbc/reference/current/jdbcug_chapter_2.html
    @Test
    public void testTeradataFilter() throws MalformedURLException {
        String url = SecurityUtil.filterJdbcConnectionSource("jdbc:teradata://localhost/USER=zhangsan,BROWSER=xxxx,ACCOUNT=xxxx");
        assertEquals("jdbc:teradata://localhost/ACCOUNT=xxxx,USER=zhangsan", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:teradata://localhost/PASSWORD=dsfm^&()(,LOGDATA=sdfasd&^(,ACCOUNT=xxxx");
        assertEquals("jdbc:teradata://localhost/ACCOUNT=xxxx,PASSWORD=dsfm^&()(", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:teradata://localhost/PASSWORD=dsfm^&()(,LOGDATA=sdfasd&^(,ACCOUNT=xxxx");
        assertEquals("jdbc:teradata://localhost/ACCOUNT=xxxx,PASSWORD=dsfm^&()(", url);
    }

    // https://help.aliyun.com/zh/maxcompute/user-guide/usage-notes-2
    @Test
    public void testOdpsFilter() throws MalformedURLException {
        String url = SecurityUtil.filterJdbcConnectionSource("jdbc:odps:http://localhost:8080?charset=UTF-8&foobar=abc");
        assertEquals("jdbc:odps:http://localhost:8080?charset=UTF-8", url);
        url = SecurityUtil.filterJdbcConnectionSource("jdbc:odps:https://localhost:8080?charset=UTF-8&foobar=abc");
        assertEquals("jdbc:odps:https://localhost:8080?charset=UTF-8", url);
        url = SecurityUtil.filterJdbcConnectionSource("jdbc:odps:https://localhost:8080?aaa=xxxx&foobar=abc&dsfasf=sdfasfd");
        assertEquals("jdbc:odps:https://localhost:8080", url);
    }

    // https://help.aliyun.com/document_detail/476583.html?spm=a2c4g.216788.0.i0
    @Test
    public void testLindormFilter() throws MalformedURLException {
        String url = SecurityUtil.filterJdbcConnectionSource("jdbc:lindorm:table:url=https://www.aliyun.com/foo/bar;timeZone=Asia/Shanghai;serialization=protobuf");
        assertEquals("jdbc:lindorm:table:serialization=protobuf;timeZone=Asia/Shanghai;url=https://www.aliyun.com/foo/bar", url);
        url = SecurityUtil.filterJdbcConnectionSource("jdbc:lindorm:table:url=111;httpclient_factory=aaa;factory=bbb;httpclient_impl=xx;principal=xxx;foo=bar");
        assertEquals("jdbc:lindorm:table:url=111", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:lindorm:tsdb:url=http://localhost:8080;user=$%^&*;password=xxxx;database=db_name;lindorm.tsdb.driver.socket.timeout=5000");
        assertEquals("jdbc:lindorm:tsdb:password=xxxx;database=db_name;lindorm.tsdb.driver.socket.timeout=5000;user=$%^&*;url=http://localhost:8080", url);

    }

    @Test
    public void testCheckProperties() throws MalformedURLException {
        JdbcToolImpl jdbcTool = new JdbcToolImpl();
        String url = "jdbc:mysql://localhost:8080/?foo=bar";
        FilterResult result = SecurityUtil.filterJdbcConnectionSourceWithResult(url);
        assertFalse(result.isSafe());
    }

    @Test
    public void testHive() throws MalformedURLException {
        String url = SecurityUtil.filterJdbcConnectionSource("jdbc:hive://localhost:8080/db?user=zhangsan");
        assertEquals("jdbc:hive://localhost:8080/db", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:hive://localhost/db?user=zhangsan");
        assertEquals("jdbc:hive://localhost/db", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:hive://localhost:8080?user=zhangsan");
        assertEquals("jdbc:hive://localhost:8080", url);

        url = SecurityUtil.filterJdbcConnectionSource("jdbc:hive://localhost:8080/?user=zhangsan");
        assertEquals("jdbc:hive://localhost:8080", url);
    }

    // https://cwiki.apache.org/confluence/display/hive/hiveserver2+clients
    @Test
    public void testHive2() throws MalformedURLException {
        try {
            SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db*name;foo=bar");
            fail();
        } catch (JdbcURLUnsafeException ignored) {}

        assertEquals("jdbc:hive2://localhost:123/db$name",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db$name;foo=bar")
        );

        assertEquals("jdbc:hive2://localhost:123/db;retries=2",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db;foo=bar;retries=2?foo=bar#f=b")
        );

        assertEquals("jdbc:hive2://localhost:123/db",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db;foo=bar?foo=bar#f=b")
        );
        assertEquals("jdbc:hive2://localhost:123/db",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db;initFile=aaa&retries=/xxx/xxx?foo=bar#f=b")
        );
        assertEquals("jdbc:hive2://localhost:123/db",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db;initFile=aaa&retries=/xxx/xxx?foo=bar#f=b")
        );
        assertEquals("jdbc:hive2://localhost:123/db;password=pass$word;user=user",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db;user=user;password=pass$word?foo=bar#f=b")
        );

        assertEquals("jdbc:hive2://localhost:123/db;password=pass$word;user=user",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db;user=user;password=pass$word?hive.reloadable.aux.jars.path=xxxx")
        );
        assertEquals("jdbc:hive2://localhost:123/db;user=user1?user=user2#user=user3",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123/db;user=user1?user=user2#user=user3")
        );

        assertEquals("jdbc:hive2://localhost:123,1.1.1.1/db",
                SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://localhost:123,1.1.1.1/db")
        );

        // lindorm 计算引擎
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://123.234.XX.XX:10009/;?token=bisdfjis-f7dc-fdsa-9qwe-dasdfhhv8abcd;spark.dynamicAllocation.minExecutors=3;spark.sql.adaptive.enabled=false;compute-group=default;foo=bar");
        assertTrue(str.contains("jdbc:hive2://123.234.XX.XX:10009/;?"));
        assertFalse(str.contains("foo=bar"));
        assertTrue(str.contains("token=bisdfjis-f7dc-"));
        assertTrue(str.contains("compute-group=default"));
        assertTrue(str.contains("spark.dynamicAllocation.minExecutors=3"));

        // httpPath
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:hive2://abcd.aliuyuncs.com/;transportMode=http;httpPath=http/path/foobar;http.cookie.sessionId=abcabcabc;hive.server2.thrift.http.path=/http/path/foobar;httppath=abc");
        assertTrue(str.contains("httpPath=http/path/foobar"));
        assertTrue(str.contains("transportMode=http"));
        assertTrue(str.contains("http.cookie.sessionId=abcabcabc"));
        assertTrue(str.contains("hive.server2.thrift.http.path=/http/path/foobar"));
        assertFalse(str.contains("httppath=abc"));

    }

    // https://www.vertica.com/docs/11.1.x/HTML/Content/Authoring/ConnectingToVertica/ClientJDBC/JDBCConnectionProperties.htm
    @Test
    public void testVertica() throws JdbcURLException {
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:vertica://localhost/test?ssl=true&user=zhangsan&password=123$$$");
        assertEquals("jdbc:vertica://localhost/test?password=123$$$&DisableCopyLocal=true&ssl=true&user=zhangsan", str);
    }

    // https://docs.cloudera.com/documentation/other/connectors/impala-jdbc/latest/Cloudera-JDBC-Driver-for-Impala-Install-Guide.pdf
    @Test
    public void testImpala() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:impala://node1.example.com:18000/default2;AuthMech=3;UID=cloudera$user;PWD=cloudera");
        assertEquals("jdbc:impala://node1.example.com:18000/default2;AuthMech=3;UID=cloudera$user;PWD=cloudera", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:impala://node1.example.com:18000/default2;httpPath=/foo/bar;UID=user");
        assertEquals("jdbc:impala://node1.example.com:18000/default2;UID=user;httpPath=/foo/bar", str);
    }

    // https://prestodb.io/docs/current/installation/jdbc.html
    @Test
    public void testPresto() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:presto://example.net:8080/hive/sales?user=test&password=secret&SSL=true");
        assertEquals("jdbc:presto://example.net:8080/hive/sales?password=secret&user=test&SSL=true", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:presto://example.net:8080/hive/sales?timeZoneId=Asia/ShangHai&customHeaders=foo:bar;aaa:bbb");
        assertEquals("jdbc:presto://example.net:8080/hive/sales?timeZoneId=Asia/ShangHai", str);
    }

    // https://prestodb.io/docs/current/installation/jdbc.html
    @Test
    public void testTrino() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:trino://example.net:8443/hive/sales?user=test&password=secret&SSL=true");
        assertEquals("jdbc:trino://example.net:8443/hive/sales?password=secret&user=test&SSL=true", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:trino://example.net:8443/hive/sales?clientTags=abc,xyz&foo=bar&sessionProperties=abc:xyz;example.foo:bar&accessToken=foo+dsf=");
        assertEquals("jdbc:trino://example.net:8443/hive/sales?clientTags=abc,xyz&accessToken=foo+dsf=&sessionProperties=abc:xyz;example.foo:bar", str);
    }

    // https://docs.aws.amazon.com/redshift/latest/mgmt/jdbc20-build-connection-url.html
    @Test
    public void testRedshift() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:redshift://example.net:8443/dev?user=test&password=secret$#&SSL=true");
        assertEquals("jdbc:redshift://example.net:8443/dev;password=secret%24%23;user=test;SSL=true", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:redshift://example.net:8443/dev?sslFactory=com.example.Test");
        assertEquals("jdbc:redshift://example.net:8443/dev", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:redshift:iam://example.net:8443/http/path;sslFactory=com.example.Test;App_Name=abc");
        assertEquals("jdbc:redshift:iam://example.net:8443/http/path;App_Name=abc", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:redshift:iam://example.net:8443/http/path;ssl=abc$");
        assertEquals("jdbc:redshift:iam://example.net:8443/http/path", str);
    }

    @Test
    public void testGreenplum() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:pivotal:greenplum://127.0.0.1:5432;DatabaseName=abc;InitializationString=(command1;command2);LoginTimeout=2");
        assertEquals("jdbc:pivotal:greenplum://127.0.0.1:5432;LoginTimeout=2;DatabaseName=abc", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:pivotal:greenplum://server1:5432;DatabaseName=greenplumDB;AuthenticationMethod=kerberos;ServicePrincipalName=postgres/myserver.example.com@EXAMPLE.COM;");
        assertEquals("jdbc:pivotal:greenplum://server1:5432;ServicePrincipalName=postgres/myserver.example.com@EXAMPLE.COM;DatabaseName=greenplumDB;AuthenticationMethod=kerberos", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:pivotal:greenplum://127.0.0.1:5432");
        assertEquals("jdbc:pivotal:greenplum://127.0.0.1:5432", str);
    }

    @Test
    public void testSybase() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sybase:Tds:myserver:1234/mydatabase?LITERAL_PARAMS=true&PACKETSIZE=512&HOSTNAME=myhost&password=P$w$d");
        assertEquals("jdbc:sybase:Tds:myserver:1234/mydatabase?password=P$w$d&PACKETSIZE=512&HOSTNAME=myhost&LITERAL_PARAMS=true", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:sybase:Tds:myserver:1234/mydatabase?SYBSOCKET_ FACTORY=com.example.Soket");
        assertEquals("jdbc:sybase:Tds:myserver:1234/mydatabase", str);
    }

    @Test
    public void testAs400() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:as400://localhost:1234/db;foo=bar");
        assertEquals("jdbc:as400://localhost:1234/db", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:as400://localhost:1234/db;naming=xxx;errors=xxx;foo=bar");
        assertEquals("jdbc:as400://localhost:1234/db;naming=xxx;errors=xxx", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:as400://localhost:1234/db;block size=abc;date format=123;foo=bar");
        assertEquals("jdbc:as400://localhost:1234/db;block size=abc;date format=123", str);
    }

    @Test
    public void testArrowFlightSql() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:arrow-flight-sql://localhost:1111/dd?threadPoolSize=11&trustStore=dddasf/&token=asdfl2.12390x.adsf");
        assertEquals("jdbc:arrow-flight-sql://localhost:1111/dd?threadPoolSize=11&token=asdfl2.12390x.adsf", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:arrow-flight://localhost:1111/dd?threadPoolSize=11&trustStore=dddasf/&token=asdfl2.12390x.adsf");
        assertEquals("jdbc:arrow-flight://localhost:1111/dd?threadPoolSize=11&token=asdfl2.12390x.adsf", str);
    }

    @Test
    public void testTDEngine() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:taos://localhost:1111/dd?user=dddca_123!!&cfgdir=ddcs&charset=UTF-8");
        assertEquals("jdbc:taos://localhost:1111/dd?charset=UTF-8&user=dddca_123!!", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:TAOS://localhost:1111/dd?user=dddca_123!!&cfgdir=ddcs&charset=UTF-8");
        assertEquals("jdbc:TAOS://localhost:1111/dd?charset=UTF-8&user=dddca_123!!", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:taos-rs://localhost:1111/dd?user=dddca_123!!&cfgdir=ddcs&charset=UTF-8");
        assertEquals("jdbc:taos-rs://localhost:1111/dd?charset=UTF-8&user=dddca_123!!", str);
    }

    @Test
    public void testOTSEngine() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:ots:http://myinstance.cn-hangzhou.ots.aliyuncs.com/myinstance?enableRequestCompression=true&dd=false&user=LTAI...");
        assertEquals("jdbc:ots:http://myinstance.cn-hangzhou.ots.aliyuncs.com/myinstance?enableRequestCompression=true&user=LTAI...", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:ots:https://myinstance.cn-hangzhou.ots.aliyuncs.com/myinstance?enableRequestCompression=true&dd=false&user=LTAI...");
        assertEquals("jdbc:ots:https://myinstance.cn-hangzhou.ots.aliyuncs.com/myinstance?enableRequestCompression=true&user=LTAI...", str);
    }

    @Test
    public void testUnsafeScheme() throws JdbcURLException {
        try {
            SecurityUtil.filterJdbcConnectionSource("jdbc:mysql:Fabric://localhost/");
            fail();
        } catch (JdbcURLUnsafeException ignored){}

        try {
            SecurityUtil.filterJdbcConnectionSource("jdbc:jcr:jndi:a?foo=bar");
            fail();
        } catch (JdbcURLUnsafeException ignored){}

        try {
            SecurityUtil.filterJdbcConnectionSource("jdbc:jcr:jndI:a?foo=bar");
            fail();
        } catch (JdbcURLUnsafeException ignored){}

    }

    // https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-jdbc.html#jdbc-cfg
    @Test
    public void testEsFilter() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:es://http://server:3456/?timezone=UTC&page.size=250&debug.output=err.log");
        assertEquals("jdbc:es://http://server:3456/?timezone=UTC&page.size=250", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:elasticsearch://server:3456/?timezone=UTC&page.size=250&proxy.http=127.0.0.1:18808");
        assertEquals("jdbc:elasticsearch://server:3456/?timezone=UTC&page.size=250", str);
    }

    // https://kylin.apache.org/docs24/howto/howto_jdbc.html
    @Test
    public void testKylinFilter() throws JdbcURLException {
        String str;
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:kylin://localhost:7070/kylin_project_name?ssl=true;username=aa;something=xxx");
        assertEquals("jdbc:kylin://localhost:7070/kylin_project_name?ssl=true;username=aa", str);
    }

    @Test
    public void testDMFilter() throws MalformedURLException {
        JdbcTool tool = new JdbcToolImpl();
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:dm://localhost?schema=asdf&i134=asdfa");
        assertEquals("jdbc:dm://localhost?schema=asdf", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:dm://localhost/asdfasdf?schema=asdf&i134=asdfa");
        assertEquals("jdbc:dm://localhost/asdfasdf?schema=asdf", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:dm://localhost/asdfasdf?schema=asdf&i134=asdfa&appName=abc");
        assertEquals("jdbc:dm://localhost/asdfasdf?schema=asdf&appName=abc", str);

        str = SecurityUtil.filterJdbcConnectionSource("jdbc:dm://logDir?logDir=(../../)&SCHEMA=asdf&LOGIN_MODE=(4&i134=asdfa)");
        assertEquals("jdbc:dm://logDir?SCHEMA=asdf", str);
    }

    // https://phoenix.apache.org/faq.html
    // https://calcite.apache.org/avatica/docs/client_reference.html
    @Test
    public void testPhoenixThinFilter() throws JdbcURLException {
        JdbcTool tool = new JdbcToolImpl();
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:phoenix:thin:url=http://localhost:1234/db;serialization=PROTOBUF;foo=bar");
        assertEquals("jdbc:phoenix:thin:serialization=PROTOBUF;url=http://localhost:1234/db", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:phoenix:thin:url=http://localhost:1234/db;serialization=PROTOBUF;foo=bar");
        assertEquals("jdbc:phoenix:thin:serialization=PROTOBUF;url=http://localhost:1234/db", str);
    }

    @Test
    public void testBigQueryFilter() throws  JdbcURLException {
        String pvtKey = "{  \"type\": \"service_account\",  \"project_id\": \"translation-123123123123\",  \"private_key_id\": \"0123456789abcdef0124dfdf\",  " +
                "\"private_key\": \"-----BEGIN RSA PRIVATE KEY-----\\n" +
                "xxxxxxxxx\\n" +
                "-----END RSA PRIVATE KEY-----\",  \"client_email\": \"foobar@translation-123123123123.iam.gserviceaccount.com\",  " +
                "\"client_id\": \"1231231231231231231\",  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",  " +
                "\"token_uri\": \"https://oauth2.googleapis.com/token\",  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\", " +
                " \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/foobar%40translation-123123123123.iam.gserviceaccount.com\",  " +
                "\"universe_domain\": \"googleapis.com\"}";
        assertEquals("jdbc:bigquery://localhost",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://localhost;foo=bar"));
        try {
            SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://;foo=bar");
            fail();
        } catch (JdbcURLException ignored) {}
        try {
            SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://localhost:123456;foo=bar");
            fail();
        } catch (JdbcURLException ignored) {}
        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=1;ProjectId=MyBigQueryProject;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=MyBigQueryProject;OAuthType=1;LogPath=passwd"));
        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=1;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=$$MyBigQueryProject;OAuthType=1;"));
        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=0;ProjectId=MyBigQueryProject;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=MyBigQueryProject;OAuthType=0;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;OAuthPvtKeyPath=C:\\SecureFiles\\ServiceKeyFile.p12;"));
        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=0;ProjectId=MyBigQueryProject;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=MyBigQueryProject;OAuthType=0;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;OAuthPvtKey=/etc/passwd;"));
        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=0;ProjectId=MyBigQueryProject;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=MyBigQueryProject;OAuthType=0;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;OAuthPvtKey=foo/../../etc/passwd;"));

        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthPvtKey=" + pvtKey + ";OAuthType=0;ProjectId=MyBigQueryProject;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=MyBigQueryProject;OAuthType=0;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;OAuthPvtKey=" + pvtKey));

        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthPvtKey=" + pvtKey + ";OAuthType=0;ProjectId=MyBigQueryProject;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=MyBigQueryProject;OAuthType=0;OAuthServiceAcctEmail=bqtest1@data-driver-testing.iam.gserviceaccount.com;OAuthPvtKey=" + pvtKey));

        assertEquals("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthClientSecret=bCD+E1f2Gxhi3J4klmN;OAuthType=2;OAuthRefreshToken=1jt9Pcyq8pr3lvu143pfl4r86;ProjectId=MyBigQueryProject;OAuthAccessToken=a25c7cfd36214f94a79d;OAuthClientId=11b5516f132211e6;",
                SecurityUtil.filterJdbcConnectionSource("jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;OAuthType=2;ProjectId=MyBigQueryProject;OAuthAccessToken=a25c7cfd36214f94a79d;OAuthRefreshToken=1jt9Pcyq8pr3lvu143pfl4r86;OAuthClientId=11b5516f132211e6;OAuthClientSecret=bCD+E1f2Gxhi3J4klmN;"));

    }

    @Test
    public void testOpenSearchFilter() throws JdbcURLException {
        JdbcTool tool = new JdbcToolImpl();
        String str = SecurityUtil.filterJdbcConnectionSource("jdbc:opensearch://localhost:9200/x1231/%aax?user=addfo!!!__&password=P@ssw0rd&useSSL=false&a=bc");
        assertEquals("jdbc:opensearch://localhost:9200/x1231/%aax?password=P@ssw0rd&user=addfo!!!__&useSSL=false", str);
        str = SecurityUtil.filterJdbcConnectionSource("jdbc:opensearch://http://localhost:9200/x1231?user=addfo!!!__&password=P@ssw0rd&useSSL=false&a=bc");
        assertEquals("jdbc:opensearch://http://localhost:9200/x1231?password=P@ssw0rd&user=addfo!!!__&useSSL=false", str);
    }

}
