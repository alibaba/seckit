package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.Filter;
import com.alibaba.seckit.jdbc.FilterResult;
import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.DefaultUrlParser;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class DefaultFilter implements Filter {


    @Getter
    private final Set<String> acceptedSchemes = new HashSet<String>(Arrays.asList(
            "jdbc:not-exists:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>();

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "username",
            "pass", "password",
            "CustomProperties"
    ));

    @Getter
    @Setter
    private boolean ignoreCase = false;

    // case insensitive
    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();

    protected Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

    static {
        propertyPatterns.put("servertimezone", Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\]/+]+$"));
    }

    protected Pattern getPropertyValuePattern() {
        return PROPERTY_VALUE_PATTERN;
    }

    private static final Pattern PROPERTY_VALUE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\]]+$");



    @Override
    public void addAcceptedPropertyKey(String... keys) {
        if (this.getAcceptedPropertyKeys() == null) {
            this.setAcceptedPropertyKeys(new HashSet<String>());
        }
        this.getAcceptedPropertyKeys().addAll(Arrays.asList(keys));
    }

    @Override
    public void addPropertyKeyWhiteList(String... keys) {
        if (this.getPropertyKeyWhiteList() == null) {
            this.setPropertyKeyWhiteList(new HashSet<String>());
        }
        this.getPropertyKeyWhiteList().addAll(Arrays.asList(keys));
    }

    @Override
    public boolean acceptURL(String scheme) {
        return false;
    }

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new DefaultUrlParser(url);
    }

    @Override
    public String filterProperties(String url) throws JdbcURLException {
        FilterResult result = filterPropertiesWithResult(url);
        return result.getAfter();
    }

    public FilterResult filterPropertiesWithResult(String url) throws JdbcURLException {
        UrlParser parser = createUrlParser(url);
        parser.parse();
        FilterResult result = checkAndFilterProperties(parser);
        result.setAfter(parser.toString());
        return result;
    }

    public FilterResult checkAndFilterProperties(UrlParser parser) {
        FilterResult result = new FilterResult(parser.getInitialUrl());

        Map<String, String> properties = parser.getProperties();
//        Map<String, String> removedProperties = new HashMap<>();
        Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            if (!keyIn(key, getAcceptedPropertyKeys()) && !keyIn(key, getPropertyKeyWhiteList())) {
                iterator.remove();
                result.propertyDelete(entry.getKey(), entry.getValue());
                continue;
            }

            if (entry.getValue() != null && !entry.getValue().isEmpty() &&
                    !keyIn(key, getPropertyKeyWhiteList()) ) {
                Pattern pattern = getPropertyPatterns().get(key.toLowerCase());
                if (pattern == null) {
                    pattern = getPropertyValuePattern();
                }
                if (!pattern.matcher(entry.getValue()).matches()) {
                    iterator.remove();
                    result.propertyDelete(key, entry.getValue());
                }
            }
        }
        if (!result.getDeleted().isEmpty()) {
            log.info("some invalid property are removed by jdbc url filter: {}, original url: {}, after filter: {}",
                    result.getDeleted(), parser.getInitialUrl(), parser);
        }
        return result;
    }

    protected boolean keyIn(String key, Collection<String> keyList) {
        if (keyList == null || key == null) {
            return false;
        }
        if (!isIgnoreCase()) {
            return keyList.contains(key);
        }
        for (String k :
                keyList) {
            if (key.equalsIgnoreCase(k)) {
                return true;
            }
        }
        return false;
    }

}
