package org.masouras.app.batch.pmp.control.mapper;

import org.masouras.squad.printing.mssql.schema.jpa.control.ContentType;
import org.masouras.squad.printing.mssql.schema.jpa.entity.ActivityEntity;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.masouras.squad.printing.mssql.schema.qb.structure.DbField;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class PrintingDataRowMapper implements RowMapper<PrintingDataEntity> {

    @Override
    public PrintingDataEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        PrintingDataEntity entity = new PrintingDataEntity();

        entity.setId(rs.getLong(DbField.REC_ID.systemName()));
        entity.setProcessed(rs.getBoolean(DbField.PROCESSED.getName()));
        entity.setModifiedAt(rs.getTimestamp(DbField.MODIFIED_AT.systemName()).toLocalDateTime());

        ActivityEntity activity = new ActivityEntity();
        activity.setId(rs.getLong(DbField.ACTIVITY_ID.systemName()));
        entity.setActivity(activity);

        entity.setContentType(Objects.requireNonNull(ContentType.getFromCode(rs.getString(DbField.CONTENT_TYPE.systemName()))));

        entity.setContentBase64(rs.getString(DbField.CONTENT_BASE64.systemName()));

        return entity;
    }
}
