package org.masouras.app.batch.config.pmp;

import org.masouras.squad.printing.mssql.repo.PrintingDataRepo;
import org.masouras.squad.printing.mssql.repo.PrintingDataSQL;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class WriterConfig {
    private final PrintingDataSQL printingDataSQL;

    @Autowired
    public WriterConfig(PrintingDataSQL printingDataSQL) {
        this.printingDataSQL = printingDataSQL;
    }

    @Bean
    public ItemWriter<PrintingDataEntity> pmpWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PrintingDataEntity>()
                .dataSource(dataSource)
                .sql(printingDataSQL.getSQL(PrintingDataRepo.NameOfSQL.UPDATE_SET_PROCESSED))
                .itemPreparedStatementSetter((entity, ps) -> ps.setLong(1, entity.getId()))
                .build();
    }
}
