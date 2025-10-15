package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * @author mingyi
 * @date 2024/4/11
 */
public class PhoenixThinUrlParser extends LindormLikeUrlParser {

    public PhoenixThinUrlParser(String url) throws JdbcURLException {
        super(url);
    }
    @Getter
    private List<String> acceptedSchemes = Collections.singletonList(
            "jdbc:phoenix:thin:"
    );
}
