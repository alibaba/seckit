package com.alibaba.seckit.jdbc;

import com.alibaba.seckit.jdbc.filters.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class JdbcToolImpl implements JdbcTool {

    private static final AtomicBoolean isInit = new AtomicBoolean(false);
    private static final FilterManager filterManager = new FilterManager();

    static {
        init();
    }

    private static void init() {
        if (isInit.compareAndSet(false, true)) {
            filterManager.registerFilter(new BigQueryFilter());
            filterManager.registerFilter(new PhoenixThinFilter());
            filterManager.registerFilter(new KylinFilter());
            filterManager.registerFilter(new ElasticSearchFilter());
            filterManager.registerFilter(new OTSFilter());
            filterManager.registerFilter(new TDEngineFilter());
            filterManager.registerFilter(new ArrowFlightSqlFilter());
            filterManager.registerFilter(new SnowFlakeFilter());
            filterManager.registerFilter(new As400Filter());
            filterManager.registerFilter(new SybaseFilter());
            filterManager.registerFilter(new GreenplumFilter());
            filterManager.registerFilter(new VerticaFilter());
            filterManager.registerFilter(new ImpalaFilter());
            filterManager.registerFilter(new PrestoFilter());
            filterManager.registerFilter(new TrinoFilter());
            filterManager.registerFilter(new RedshiftFilter());
            filterManager.registerFilter(new Hive2Filter());
            filterManager.registerFilter(new HiveFilter());
            filterManager.registerFilter(new LindormFilter());
            filterManager.registerFilter(new RedisFilter());
            filterManager.registerFilter(new TeradataFilter());
            filterManager.registerFilter(new MongoDBFilter());
            filterManager.registerFilter(new OracleFilter());
            filterManager.registerFilter(new MySQLFilter());
            filterManager.registerFilter(new PostgresFilter());
            filterManager.registerFilter(new SQLServerFilter());
            filterManager.registerFilter(new ClickHouseFilter());
            filterManager.registerFilter(new OdpsFilter());
            filterManager.registerFilter(new InformixSqliFilter());
            filterManager.registerFilter(new SAPFilter());
            filterManager.registerFilter(new NSFilter());
            filterManager.registerFilter(new DB2Filter());
            filterManager.registerFilter(new SemicolonSeparatedUrlFilter());
            filterManager.registerFilter(new ColonSeparatedUrlFilter());
            filterManager.registerFilter(new DMFilter());
            filterManager.registerFilter(new OpenSearchFilter());
            filterManager.registerFilter(new DefaultFilter());
        }
    }

    public void registerFilter(Filter filter) {
        filterManager.registerFilter(filter);
    }

    @Override
    public String filterConnectionSource(String str) throws JdbcURLException {
        Filter filter = filterManager.selectFilter(str);
        return filter.filterProperties(str);
    }

    public FilterResult filterConnectionSourceWithResult(String str) throws JdbcURLException {
        Filter filter = filterManager.selectFilter(str);
        return filter.filterPropertiesWithResult(str);
    }
}
