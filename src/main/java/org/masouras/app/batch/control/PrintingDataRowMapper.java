package org.masouras.app.batch.control;

import org.masouras.printing.sqlite.schema.jpa.control.ContentType;
import org.masouras.printing.sqlite.schema.jpa.entity.ActivityEntity;
import org.masouras.printing.sqlite.schema.jpa.entity.PrintingDataEntity;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class PrintingDataRowMapper implements RowMapper<PrintingDataEntity> {

    @Override
    public PrintingDataEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        PrintingDataEntity entity = new PrintingDataEntity();

        entity.setId(rs.getLong("id"));
        entity.setProcessed(rs.getBoolean("PROCESSED"));
        entity.setCurrentTimestamp(rs.getTimestamp("CURRENT_TIMESTAMP").toLocalDateTime());

        ActivityEntity activity = new ActivityEntity();
        activity.setId(rs.getLong("ACTIVITY_ID"));
        entity.setActivity(activity);

        entity.setContentType(Objects.requireNonNull(ContentType.getFromCode(rs.getString("CONTENT_TYPE"))));

        entity.setContentBase64(rs.getString("CONTENT_BASE64"));

        return entity;
    }
}
