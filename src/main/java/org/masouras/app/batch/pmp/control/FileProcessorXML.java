package org.masouras.app.batch.pmp.control;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.masouras.app.batch.pmp.domain.FileProcessorResult;
import org.masouras.squad.printing.mssql.schema.jpa.control.ActivityType;
import org.masouras.squad.printing.mssql.schema.jpa.control.ContentType;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class FileProcessorXML implements FileProcessor {

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
        ByteArrayInputStream xmlStream = getByteArrayInputStream(validatedBase64Content);

        return FileProcessorResult.success("");
    }
    private @NonNull ByteArrayInputStream getByteArrayInputStream(String validatedBase64Content) {
        String xmlContent = new String(Base64.getDecoder().decode(validatedBase64Content), StandardCharsets.UTF_8);
        return new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
    }
}
