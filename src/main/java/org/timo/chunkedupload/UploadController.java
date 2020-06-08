package org.timo.chunkedupload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.timo.chunkedupload.model.RangeData;

/**
 * @author timoteo
 * <p>
 * https://github.com/blueimp/jQuery-File-Upload/wiki/Chunked-file-uploads
 */
@RestController
public class UploadController {

    @RequestMapping("/hello")
    public String sayHi() {
        return "Hi at " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private String filePath() {
        if (System.getProperty("downloadDir") == null)
            return FilenameUtils.normalize(System.getProperty("user.home") + "/tmp4/", true);
        return FilenameUtils.normalize(System.getProperty("downloadDir") + "/",true);
    }

    @ResponseBody
    @RequestMapping(value = "/chunked-upload", method = RequestMethod.POST)
    public Map<String, String> chunkedUpload(final @RequestHeader HttpHeaders headers, final MultipartHttpServletRequest request) {
        info("Headers: " + headers);
        String fileName = extractFilename(headers.getFirst("Content-Disposition"));
        RangeData range = extractRangeData(headers.getFirst("Content-Range"));

        File file = new File(filePath() + fileName);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            throw new IllegalStateException("Can't create storage folder: " + file.getAbsolutePath());

        info("File: " + file.getAbsolutePath() + " range: " + range);

        if (range.getBegin() == 0L) {
            file.delete();
        } else if (file.length() >= range.getTotalSize()) {
            throw new IllegalStateException("File upload offset: " + range + ", current size: " + file.length());
        }

        request.getFileMap().forEach((name, partFile) -> {

            try (FileOutputStream fos = new FileOutputStream(file, true);
                 InputStream is = partFile.getInputStream()) {
                fos.write(IOUtils.toByteArray(is));
            } catch (IOException e) {
                throw new IllegalStateException("Error when processing file content", e);
            }
        });
        return Collections.singletonMap("status", "uploaded");
    }

    private static String extractFilename(String str) {
        if (str != null) {
            String key = "filename";
            int idx = str.indexOf("filename");
            if (idx >= 0) {
                String value = str.substring(idx + (key + "=\"").length(), str.length() - 1).trim();
                try {
                    return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException("Invalid Content-Disposition header content: " + str, e);
                }
            }
        }
        throw new IllegalArgumentException("Invalid Content-Disposition header: " + str);
    }

    private static RangeData extractRangeData(String str) {
        String[] parts = str.split("\\W+");
        if (parts.length > 3)
            return new RangeData(Long.parseLong(parts[1]), Long.parseLong(parts[2]), Long.parseLong(parts[3]));
        throw new IllegalArgumentException("Invalid Content-Range header: " + str);
    }

    private static void info(String string) {
        LoggerFactory.getLogger(UploadController.class).info(string);
    }

    public static void main(String[] args) {
        String str = "attachment; filename=\"presentation_20171005.sql.zip\"";
        info(str);
        info(" " + str.indexOf("filename"));
        info(" " + str.substring(str.indexOf("filename") + "filename=\"".length(), str.length() - 1));
        info(extractFilename(str));

        String range = "bytes 20000000-24999999/28369757";
        info(Arrays.toString(range.split("\\W+")));
        info(extractRangeData(range).toString());
    }

}
