package org.masouras.app.batch.pmp.config;

import lombok.RequiredArgsConstructor;
import org.masouras.model.mssql.j2sql.control.PrintingDataRepo;
import org.masouras.model.mssql.j2sql.control.PrintingDataSQL;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class PmpWriterConfig {
    private final PrintingDataSQL printingDataSQL;

    @Bean
    public ItemWriter<PrintingDataEntity> pmpWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PrintingDataEntity>()
                .dataSource(dataSource)
                .sql(printingDataSQL.getSQL(PrintingDataRepo.NameOfSQL.UPDATE_SET_PROCESSED))
                .itemPreparedStatementSetter((entity, ps) -> ps.setLong(1, entity.getId()))
                .build();
    }
}
