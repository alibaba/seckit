package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;
import com.alibaba.seckit.jdbc.parser.oracle.NVFactory;
import com.alibaba.seckit.jdbc.parser.oracle.NVPair;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oracle JDBC URL parser.
 * <p>
 * e.g. <code>jdbc:oracle:thin:@tcps://mydbhost1:5521/mydbservice?wallet_location=/work/wallet</code>
 * documentation at <link>https://docs.oracle.com/en/database/oracle/oracle-database/21/jajdb/index.html</link>
 * refers to oracle.jdbc.driver.OracleDriver
 */
public class OracleUrlParser extends DefaultUrlParser {

    public OracleUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final Pattern HOST_INFO_PATTERN = Pattern.compile("(?<hostnames>(((\\[[A-z0-9:]+\\])|([A-z0-9][A-z0-9._-]+)),?)+)(:(?<port>\\d+))?");
    private static final Pattern EZ_URL_PATTERN = Pattern.compile("^((?<protocol>tcp|tcps):)?(//)?(?<hostinfo>(" + HOST_INFO_PATTERN.pattern() + ")+)(/(?<servicename>[A-z][A-z0-9,-.]+))?(:(?<servermode>dedicated|shared|pooled))?(/(?<instance>[A-z][A-z0-9]+))?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_PATTERN = Pattern.compile("jdbc:oracle:(thin|oci|oci8|kprb):\\w*/?\\w*@(//)?[A-z0-9-._]+(:\\d+)[:/][A-z0-9-._:]+");
    private static final Pattern OLD_URL_SPLITTER = Pattern.compile("(?i)jdbc:(oracle|default):(thin|oci[8]?|kprb|connection)(?-i)(:(((([\\w\\[\\]$#]*)|(\"[^\u0000\"]+\"))/(([\\w$#\\(\\)\\!]*)|(\"[^\u0000\"]+\")))?@(.*)?)?)?", Pattern.MULTILINE | Pattern.DOTALL);


    private String connectionString;

    @Getter
    @Setter
    private NVPair nvPair;

    @Override
    public void parse() throws JdbcURLException {
        if (!initialUrl.regionMatches(true, 0, "jdbc:oracle:thin:@", 0, 18)) {
            throw new JdbcURLUnsafeException("initialUrl not supported: " + initialUrl);
        }
        this.initialUrl = initialUrl;
        this.scheme = "jdbc:oracle:thin:";

        String originalUrl = initialUrl;
        initialUrl = initialUrl.substring(18);
        if (initialUrl.isEmpty()) {
            throw new JdbcURLUnsafeException("invalid initialUrl: " + originalUrl);
        }

        if (!URL_PATTERN.matcher(initialUrl).matches()) {
            if (initialUrl.startsWith("(")) {
                this.parseTNSURLFormat(initialUrl);
            } else {
                int paramIndex = initialUrl.indexOf("?");
                if (paramIndex > -1) {
                    this.parseProperties(initialUrl.substring(paramIndex + 1));
                    initialUrl = initialUrl.substring(0, paramIndex);
                }
                this.parseEzConnectFormat(initialUrl);
            }
        } else {
            this.connectionString = originalUrl;
        }
        this.parseConnectionString();
    }

    private void parseEzConnectFormat(String url) throws JdbcURLException {
        url = url.replaceAll("\\s+", "");
        this.connectionString = "jdbc:oracle:thin:@" + url;

    }

    private void parseConnectionString() throws JdbcURLException {
        if (this.connectionString == null || this.connectionString.equals("")) {
            return;
        }

        Matcher matcher = OLD_URL_SPLITTER.matcher(this.connectionString);
        if (!matcher.matches()) {
            throw new JdbcURLUnsafeException("invalid url format: " + this.connectionString);
        }
        String db = matcher.group(12);
        if (db.equals("")) {
            throw new JdbcURLUnsafeException("invalid url: " + this.connectionString);
        }

        // remove parameter, (should not happen, since question mark is remove by parseEzFormat)
        int questionMarkIndex = db.indexOf('?');
        if (questionMarkIndex >= 0) {
            db = db.substring(0, questionMarkIndex);
        }

        this.connectionString = this.connectionString.substring(0, matcher.start(12)) + db;

        // remove schema prefix
        int atIndex = this.connectionString.indexOf('@');
        this.connectionString = this.connectionString.substring(atIndex + 1);

    }

    private void parseTNSURLFormat(String url) throws JdbcURLException {
        url = url.startsWith("alias") ? url : "alias=" + url;
        url = url.charAt(0) == '(' ? url : "(" + url + ")";
        try {
            nvPair = (new NVFactory()).createNVPair(url);
        }
        catch (JdbcURLException ex) {
            // throw new JdbcURLUnsafeException("invalid url format: " + url);
            throw (ex);
        }
        if (nvPair == null || nvPair.getListSize() != 1) {
            throw new JdbcURLUnsafeException("invalid url format: " + url);
        }
    }

    @Override
    public String toString() {
        String url = this.scheme + "@";
        if (nvPair != null) {
            if (nvPair.getListSize() < 1) {
                return url;
            }
            NVPair subPair = nvPair.getListElement(0);
            if (subPair == null) {
                return url;
            }
            url += subPair.toString();
        } else {
            url += this.connectionString + (this.properties.isEmpty() ? "" : "?" + this.propertiesToString());
        }
        return url;
    }

}
