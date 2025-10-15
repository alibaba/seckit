package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.ColonSeparatedUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class ColonSeparatedUrlFilter extends DefaultFilter{

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>();

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new ColonSeparatedUrlParser(url);
    }

}
