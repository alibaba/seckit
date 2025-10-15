package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ClickHouseFilter extends DefaultFilter {

    public ClickHouseFilter() {
        super();
        super.getPropertyPatterns().put("server_time_zone".toLowerCase(), Pattern.compile("^[a-zA-Z0-9_\\-\\[\\]/+]+$"));
        super.getPropertyPatterns().put("use_time_zone".toLowerCase(),  Pattern.compile("^[a-zA-Z0-9_\\-\\[\\]/+]+$"));
    }

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:clickhouse:",
            "jdbc:ch:",
            "jdbc:clickhouse:http:",
            "jdbc:clickhouse:https:",
            "jdbc:clickhouse:grpc:",
            "jdbc:ch:http:",
            "jdbc:ch:https:",
            "jdbc:ch:grpc:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "socket_timeout",
            "max_partitions_per_insert_block",
            "connect_timeout",
            "insert_deduplicate",
            "receive_timeout",
            "max_insert_threads",
            "send_timeout",
            "replication_alter_partitions_sync",
            "http_send_timeout",
            "http_receive_timeout",
            "max_execution_time",
            "autoCommit",
            "createDatabaseIfNotExist",
            "continueBatchOnError",
            "databaseTerm",
            "externalDatabase",
            "jdbcCompliant",
            "namedParameter",
            "nullAsDefault",
            "transactionSupport",
            "wrapperObject",

            "async",
            "auto_discovery",
            "client_name",
            "compress",
            "decompress",
            "compress_algorithm",
            "decompress_algorithm",
            "compress_level",
            "decompress_level",
            "database",
            "failover",
            "product_name",
            "retry",
            "server_time_zone",
            "session_timeout",
            "ssl",
            "sslmode",
            "transaction_timeout",
            "use_binary_string",
            "use_blocking_queue",
            "use_compilation",
            "use_server_time_zone",
            "use_server_time_zone_for_dates",
            "use_time_zone"
            ));

}
