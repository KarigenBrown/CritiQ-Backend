package me.critiq.backend.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PathUtil {
    // 高并发下不要这么写,线程不安全有并发问题,每个请求直接new就好
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public static String dateUuidPath(String originalFilename) {
        var index = originalFilename.lastIndexOf(".");
        var fileType = originalFilename.substring(index);
        return formatter.format(LocalDate.now()) + "/" + UUID.randomUUID() + fileType;
    }
}