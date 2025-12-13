package org.masouras.app.batch.pmp.control.control.validator.control;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Chars;
import org.masouras.app.batch.pmp.control.control.validator.domain.FileValidatorResult;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileValidatorXML implements FileValidator {
    private static final char MAX_BMP_BEFORE_SURROGATE = '\uD7FF'; // 0xD7FF
    private static final char MIN_BMP_AFTER_SURROGATE  = '\uE000'; // 0xE000
    private static final char MAX_BMP                  = '\uFFFD'; // 0xFFFD

    @Override
    public FileExtensionType getFileExtensionType() {
        return FileExtensionType.XML;
    }

    @Override
    public FileValidatorResult gatValidatedResult(Object... params) {
        Preconditions.checkNotNull(params);
        Preconditions.checkArgument(params.length >= 1, "validate requires 1 or 2 parameters: base64Content and optionally xsdPath");

        String base64Content = (String) params[0];
        Preconditions.checkNotNull(base64Content, "base64Content can't be null");

        String xsdPath = (params.length > 1) ? (String) params[1] : null;

        return gatValidatedResultMain(base64Content, xsdPath);
    }
    private FileValidatorResult gatValidatedResultMain(String base64Content, String xsdPath) {
        try {
            String xmlContent = new String(Base64.getDecoder().decode(base64Content), StandardCharsets.UTF_8);

            xmlContent = sanitizeXml(xmlContent);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            if (StringUtils.isNotBlank(xsdPath)) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setSchema(schemaFactory.newSchema(new File(xsdPath)));
            }
            Document doc = factory.newDocumentBuilder().parse(inputStream);

            return FileValidatorResult.success(doc);
        } catch (IllegalArgumentException | ParserConfigurationException | SAXException | IOException e) {
            log.error("{} failed with message: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            return FileValidatorResult.error(e.getMessage());
        }
    }

    private String sanitizeXml(String input) {
        String cleaned = input.chars()
                .mapToObj(c -> {
                    char ch = (char) c;
                    return isValidXmlChar(ch) ? String.valueOf(ch) : StringUtils.SPACE;
                })
                .collect(Collectors.joining());
        return cleaned.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;)", "&amp;");
    }
    private boolean isValidXmlChar(char ch) {
        return (ch == Chars.TAB     // tab
                || ch == Chars.LF   // newline
                || ch == Chars.CR   // carriage return
                || (ch >= Chars.SPACE && ch <= MAX_BMP_BEFORE_SURROGATE)
                || (ch >= MIN_BMP_AFTER_SURROGATE && ch <= MAX_BMP));
    }
}
