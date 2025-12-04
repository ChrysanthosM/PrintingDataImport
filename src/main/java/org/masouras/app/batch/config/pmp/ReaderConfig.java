package org.masouras.app.batch.config.pmp;

import org.masouras.app.batch.control.PrintingDataRowMapper;
import org.masouras.printing.mssql.repo.PrintingDataRepo;
import org.masouras.printing.mssql.repo.PrintingDataSQL;
import org.masouras.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ReaderConfig {
    private final PrintingDataSQL printingDataSQL;

    @Autowired
    public ReaderConfig(PrintingDataSQL printingDataSQL) {
        this.printingDataSQL = printingDataSQL;
    }

    @Bean
    public JdbcCursorItemReader<PrintingDataEntity> reader(DataSource dataSource) {
        JdbcCursorItemReader<PrintingDataEntity> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(printingDataSQL.getSQL(PrintingDataRepo.NameOfSQL.LIST_UNPROCESSED));
        reader.setRowMapper(new PrintingDataRowMapper());
        return reader;
    }
}

