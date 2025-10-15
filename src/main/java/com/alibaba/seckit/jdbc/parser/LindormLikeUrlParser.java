package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class LindormLikeUrlParser extends DefaultUrlParser {

    public LindormLikeUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    @Getter
    protected List<String> acceptedSchemes = Arrays.asList(
            "jdbc:lindorm:table:",
            "jdbc:lindorm:search:",
            "jdbc:lindorm:phoenix:",
            "jdbc:lindorm:analytics:",
            "jdbc:lindorm:tsdb:"
    );

    @Override
    protected String getParamSeparator() {
        return ";";
    }

    @Override
    public void parse() throws JdbcURLException {
        String urlSecondPart = null;
        for (String s :
                getAcceptedSchemes()) {
            if (initialUrl.startsWith(s)) {
                scheme = s;
                urlSecondPart = initialUrl.substring(s.length());
                break;
            }
        }
        if (urlSecondPart == null) {
            throw new JdbcURLUnsafeException("url not supported: " + initialUrl);
        }
        String[] kvs = urlSecondPart.split(";");
        for (String kv: kvs){
            String[] fields = kv.split("=");
            if (fields.length == 2) {
                properties.put(fields[0].trim(), fields[1].trim());
            }
        }
    }


    @Override
    public String toString() {
        return getScheme() + propertiesToString();
    }
}
