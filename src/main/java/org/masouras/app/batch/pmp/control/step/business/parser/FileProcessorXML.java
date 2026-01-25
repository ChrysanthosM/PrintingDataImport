package org.masouras.app.batch.pmp.control.step.business.parser;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.masouras.app.batch.pmp.domain.FileProcessorResult;
import org.masouras.data.control.render.PdfRendererService;
import org.masouras.data.control.service.PrintingLetterSetUpService;
import org.masouras.data.control.service.XslTemplateService;
import org.masouras.model.mssql.schema.jpa.control.entity.adapter.projection.PrintingLetterSetUpProjectionImplementor;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ActivityType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ContentType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.FileExtensionType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ValidFlag;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileProcessorXML implements FileProcessor {
    private final PrintingLetterSetUpService printingLetterSetUpService;
    private final PdfRendererService pdfRendererService;
    private final XslTemplateService xslTemplateService;

    @Override
    public FileExtensionType getFileExtensionType() {
        return FileExtensionType.XML;
    }

    @Override
    public FileProcessorResult getFileProcessorResult(Object... params) {
        Preconditions.checkNotNull(params);
        Preconditions.checkArgument(params.length == 3, "processor requires 3 parameters: ActivityType, ContentType and validatedBase64Content");

        ActivityType activityType = (ActivityType) params[0];
        Preconditions.checkNotNull(activityType, "activityType must not be null");
        ContentType contentType = (ContentType) params[1];
        Preconditions.checkNotNull(contentType, "contentType must not be null");
        byte[] validatedContent = (byte[]) params[2];
        if (ArrayUtils.isEmpty(validatedContent)) {
            throw new IllegalArgumentException("validatedContent can't be empty");
        }

        return getFileProcessorResultMain(activityType, contentType, validatedContent);
    }
    private FileProcessorResult getFileProcessorResultMain(ActivityType activityType, ContentType contentType, byte[] validatedContent) {
        List<PrintingLetterSetUpProjectionImplementor> implementorList = printingLetterSetUpService.getPrintingLetterLookUpMap()
                .getOrDefault(activityType.getCode(), Map.of())
                .getOrDefault(contentType.getCode(), List.of());
        if (CollectionUtils.isEmpty(implementorList)) return FileProcessorResult.error("PrintingLetterSetUp not found for ActivityType: " + activityType + " and ContentType: " + contentType);

        List<byte[]> pdfResultList = implementorList.parallelStream()
                .filter(implementor -> implementor.getValidFlag() == ValidFlag.ENABLED)
                .map(implementor -> new AbstractMap.SimpleEntry<>(implementor, xslTemplateService.getTemplate(implementor.getXslType())))
                .filter(entry -> ArrayUtils.isNotEmpty(entry.getValue()))
                .map(entry ->
                        pdfRendererService.generatePdf(
                                entry.getKey().getRendererType(),
                                validatedContent,
                                entry.getValue()
                        )
                )
                .toList();

        return FileProcessorResult.success(pdfResultList);
    }
}
