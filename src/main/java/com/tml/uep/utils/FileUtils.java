package com.tml.uep.utils;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

public class FileUtils {

    public static String getFileExtension(String mimeType) throws MimeTypeException {
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        MimeType mime = allTypes.forName(mimeType);
        return mime.getExtension();
    }

    public static String getMimeType(byte[] bytes) {
        Tika tika = new Tika();
        String mimeType = tika.detect(bytes);
        return mimeType;
    }
}
