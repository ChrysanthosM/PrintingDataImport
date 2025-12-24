package org.masouras.app.batch.pmp.control;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.masouras.app.batch.pmp.domain.FileProcessorResult;
import org.masouras.data.control.render.PdfRendererService;
import org.masouras.squad.printing.mssql.schema.jpa.control.RendererType;
import org.masouras.data.control.service.PrintingLetterSetUpService;
import org.masouras.data.control.service.XslTemplateService;
import org.masouras.squad.printing.mssql.schema.jpa.control.ActivityType;
import org.masouras.squad.printing.mssql.schema.jpa.control.ContentType;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;
import org.masouras.squad.printing.mssql.schema.jpa.projection.PrintingLetterSetUpProjectionImplementor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Base64;
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
        String validatedBase64Content = (String) params[2];
        Preconditions.checkNotNull(validatedBase64Content, "validatedBase64Content must not be null");

        return getFileProcessorResultMain(activityType, contentType, validatedBase64Content);
    }
    private FileProcessorResult getFileProcessorResultMain(ActivityType activityType, ContentType contentType, String validatedBase64Content) {
        List<PrintingLetterSetUpProjectionImplementor> implementorList = printingLetterSetUpService.getPrintingLetterLookUpMap()
                .getOrDefault(activityType.getCode(), Map.of())
                .getOrDefault(contentType.getCode(), List.of());
        if (CollectionUtils.isEmpty(implementorList)) return FileProcessorResult.error("PrintingLetterSetUp not found");

        final byte[] base64ContentDecoded = Base64.getDecoder().decode(validatedBase64Content);
        List<String> pdfResultList = implementorList.parallelStream()
                .map(implementor -> new AbstractMap.SimpleEntry<>(implementor, xslTemplateService.getTemplate(implementor.getXslType())))
                .filter(entry -> ArrayUtils.isNotEmpty(entry.getValue()))
                .map(entry -> Base64.getEncoder().encodeToString(
                        pdfRendererService.generatePdf(
                                entry.getKey().getRendererType(),
                                base64ContentDecoded,
                                entry.getValue()
                        )
                ))
                .toList();

        return FileProcessorResult.success(pdfResultList);
    }
    private @NonNull ByteArrayInputStream getByteArrayInputStream(String validatedBase64Content) {
        String xmlContent = new String(Base64.getDecoder().decode(validatedBase64Content), StandardCharsets.UTF_8);
        return new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
    }
}
