package me.forward.dts;

import com.google.common.base.Joiner;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.forward.dts.config.DtsProperties;
import me.forward.dts.model.Record;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/6
 */
@Slf4j
@Component
public class MysqlSinkSplitRunner implements SinkSplitRunner {

    public static final String KEYWORD_ESCAPE = "`";

    @Autowired
    private DtsProperties dtsProperties;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void sink(List<Record> records) {
        Connection connection = null;
        try {
            List<String> columns = getColumnNames(dtsProperties.getSink().getTable());
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT IGNORE INTO ");
            sb.append(KEYWORD_ESCAPE);
            sb.append(dtsProperties.getSink().getTable());
            sb.append(KEYWORD_ESCAPE);
            sb.append(" (");
            sb.append(KEYWORD_ESCAPE);
            sb.append(Joiner.on(KEYWORD_ESCAPE + ',' + KEYWORD_ESCAPE).join(columns));
            sb.append(KEYWORD_ESCAPE);
            sb.append(") VALUES (");
            String[] placeholders = new String[columns.size()];
            Arrays.fill(placeholders, "?");
            sb.append(Joiner.on(", ").join(Arrays.asList(placeholders)));
            sb.append(")");

            Object[][] params = new Object[records.size()][];
            for (int i = 0; i < records.size(); i++) {
                Record record = records.get(i);

                Object[] param = new Object[record.size()];
                for (int j = 0; j < record.size(); j++) {
                    param[j] = record.get(j).getValue();
                    params[i] = param;
                }
            }
            connection = jdbcTemplate.getDataSource().getConnection();
            connection.setAutoCommit(false);
            QueryRunner runner = new QueryRunner(jdbcTemplate.getDataSource());
            runner.batch(connection, sb.toString(), params);
            connection.commit();
        } catch (SQLException e) {
            log.error("fail sink split", e);
        } finally {
            DbUtils.closeQuietly(connection);
        }

    }

    private List<String> getColumnNames(String targetTable) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(KEYWORD_ESCAPE);
        sb.append(targetTable);
        sb.append(KEYWORD_ESCAPE);
        sb.append(" WHERE 1=2 limit 1");

        QueryRunner runner = new QueryRunner(jdbcTemplate.getDataSource());
        return runner.query(sb.toString(), rs -> {
            List<String> names = new ArrayList<>();
            int count = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= count; i++) {
                names.add(rs.getMetaData().getColumnName(i));
            }
            return names;
        });
    }
}
