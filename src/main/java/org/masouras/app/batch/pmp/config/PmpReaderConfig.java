package org.masouras.app.batch.pmp.config;

import lombok.RequiredArgsConstructor;
import org.masouras.squad.printing.mssql.schema.jpa.mapper.PrintingDataRowMapper;
import org.masouras.squad.printing.mssql.repo.PrintingDataRepo;
import org.masouras.squad.printing.mssql.repo.PrintingDataSQL;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class PmpReaderConfig {
    private final PrintingDataSQL printingDataSQL;

    @Bean
    @StepScope
    public JdbcCursorItemReader<PrintingDataEntity> pmpReader(DataSource dataSource) {
        JdbcCursorItemReader<PrintingDataEntity> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(printingDataSQL.getSQL(PrintingDataRepo.NameOfSQL.LIST_UNPROCESSED));
        reader.setRowMapper(new PrintingDataRowMapper());
        return reader;
    }
}

