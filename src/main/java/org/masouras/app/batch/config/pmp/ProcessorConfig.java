package org.masouras.app.batch.config.pmp;

import lombok.extern.slf4j.Slf4j;
import org.masouras.printing.sqlite.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ProcessorConfig {

    @Bean
    public ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor() {
        return record -> {
            if (log.isInfoEnabled()) log.info("Processing record: {}", record.getId());
            record.setProcessed(true);
            return record;
        };
    }
}
