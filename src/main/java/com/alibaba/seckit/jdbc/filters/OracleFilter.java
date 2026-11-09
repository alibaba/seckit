package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.FilterResult;
import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.OracleUrlParser;
import com.alibaba.seckit.jdbc.parser.oracle.NVPair;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Slf4j
public class OracleFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>();

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "oracle.net.CONNECT_TIMEOUT",
            "oracle.jdbc.ReadTimeout",
            "oracle.net.networkCompression",
            "oracle.net.networkCompressionThreshold"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPairNames = new HashSet<>(Arrays.asList(
            "DESCRIPTION", "DESCRIPTION_LIST", "ADDRESS", "ADDRESS_LIST", "ALIAS",
            // ADDRESS
            "PROTOCOL", "HOST", "PORT",
            // ADDRESS_LIST / DESCRIPTION_LIST
            "SOURCE_ROUTE", "LOAD_BALANCE", "FAILOVER",
            // DESCRIPTION
            "CONNECT_DATA",
            // CONNECT_DATA
            "SID", "SERVER", "SERVICE_NAME", "INSTANCE_NAME", "INSTANCE_ROLE", "CONNECTION_ID_PREFIX"
    ));


    @Override
    public boolean acceptURL(String url) {
        return url != null && url.regionMatches(true, 0, "jdbc:oracle:thin:@", 0, 18);
    }

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new OracleUrlParser(url);
    }


    public FilterResult checkAndFilterProperties(UrlParser parser) {
        FilterResult result = new FilterResult(parser.getInitialUrl());
        if (!(parser instanceof OracleUrlParser)) {
            // should never happen
            result.setSafe(false);
            return result;
        }

        result = super.checkAndFilterProperties(parser);

        OracleUrlParser oracleParser = (OracleUrlParser) parser;
        NVPair pair = oracleParser.getNvPair();
        if (pair == null) {
            return result;
        }
        if (!isAcceptedPair(pair)) {
            oracleParser.setNvPair(null);
            result.propertyDelete(pair.getName(), "");
        } else {
            filterNVPairRecursive(pair, result);
        }
        if (!result.isSafe()) {
            log.info("invalid property are removed by jdbc url filter, original url: {}, after filter: {}",
                    parser.getInitialUrl(), parser.toString());
        }
        return result;
    }

    public void filterNVPairRecursive(NVPair pair, FilterResult result) {
        for (int index = pair.getListSize() - 1; index >= 0; index--) {
            NVPair child = pair.getListElement(index);
            if (!isAcceptedPair(child)) {
                result.propertyDelete(child.getName(), "");
                pair.removeListElement(index);
            } else {
                filterNVPairRecursive(child, result);
            }
        }
    }

    private boolean isAcceptedPair(NVPair pair) {
        if (!acceptedPairNames.contains(pair.getName().toUpperCase(Locale.ROOT))) {
            return false;
        }
        // List-valued and empty protocols are not TCP/TCPS.
        return !pair.getName().equalsIgnoreCase("PROTOCOL")
                || (pair.getRHSType() == NVPair.RHS_ATOM
                && ("TCP".equalsIgnoreCase(pair.getAtom()) || "TCPS".equalsIgnoreCase(pair.getAtom())));
    }
}
