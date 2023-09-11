package com.tml.uep.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
@Slf4j
public class PdfTemplatingService {
    public File getPDFFromTemplate(
            String templateName, Map<String, String> placeholderMap, String fileName)
            throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String html =
                new Scanner(
                                Objects.requireNonNull(
                                        classLoader.getResourceAsStream(
                                                "templates/" + templateName)),
                                StandardCharsets.UTF_8.toString())
                        .useDelimiter("\\A")
                        .next();
        sanitizeDataMap(placeholderMap);
        StringSubstitutor sub = new StringSubstitutor(placeholderMap, "${", "}");
        String resolvedString = sub.replace(html);
        FileOutputStream pdfOutputStream = null;
        final File outputFile = File.createTempFile(fileName, ".pdf");
        pdfOutputStream = new FileOutputStream(outputFile);
        ITextRenderer it = new ITextRenderer();
        it.setDocumentFromString(resolvedString);
        it.layout();
        it.createPDF(pdfOutputStream);
        pdfOutputStream.close();
        return outputFile;
    }

    private void sanitizeDataMap(Map<String, String> mapToSanitize) {
        Set<String> keysOfMap = mapToSanitize.keySet();
        for (String key : keysOfMap) {
            String dataOfKey = mapToSanitize.get(key);
            if (dataOfKey.startsWith("data:")) continue;
            String sanitizedData = getSanitizedStringForHTML(dataOfKey);
            mapToSanitize.put(key, sanitizedData);
        }
    }

    private String getSanitizedStringForHTML(String inputStr) {
        Map<String, String> keyWordMap = new HashMap<>();
        keyWordMap.put("<", "<lt;");
        keyWordMap.put(">", ">gt;");
        keyWordMap.put("\"", "\"quot;");
        keyWordMap.put("&", "&amp;");
        for (String key : keyWordMap.keySet()) {
            inputStr = inputStr.replaceAll(key, keyWordMap.get(key));
        }
        return inputStr;
    }
}
