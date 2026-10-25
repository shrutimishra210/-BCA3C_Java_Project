package com.uniswap.service;

import com.uniswap.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores product photos on disk. Every upload gets a brand-new random name,
 * so one product's image can never overwrite another's. The file TYPE is
 * decided from the file's real bytes (not the client-supplied name/type).
 */
@Service
public class FileStorageService {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final String URL_PREFIX = "/uploads/listings/";

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Pattern STORED_URL =
            Pattern.compile("^" + URL_PREFIX + "[0-9a-f\\-]{36}\\.(jpg|png|webp|gif)$");

    private final Path listingDir;

    public FileStorageService(@Value("${swapuni.upload-dir}") String uploadDir) {
        this.listingDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("listings");
        try {
            Files.createDirectories(listingDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload folder: " + listingDir, e);
        }
    }

    public Path getRootDir() {
        return listingDir.getParent();
    }

    /** Validates and saves the file; returns its public URL path. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Please choose an image to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw ApiException.badRequest("That image is too large. The maximum size is 5 MB.");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot + 1) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw ApiException.badRequest("Only JPG, JPEG, PNG, WEBP and GIF images are allowed.");
        }

        byte[] head = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            throw ApiException.badRequest("Couldn't read the uploaded image.");
        }
        String type = detectType(head, read);
        if (type == null) {
            throw ApiException.badRequest("That file isn't a valid JPG, PNG, WEBP or GIF image.");
        }

        String name = UUID.randomUUID() + "." + type;
        Path target = listingDir.resolve(name).normalize();
        if (!target.startsWith(listingDir)) {
            throw ApiException.badRequest("Invalid file name.");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            throw new ApiException("Couldn't save the image. Please try again.",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return URL_PREFIX + name;
    }

    /** Accepts only URLs this service produced, and only if the file really exists. */
    public String requireStored(String url) {
        if (!STORED_URL.matcher(url).matches()
                || !Files.isRegularFile(listingDir.resolve(url.substring(URL_PREFIX.length())))) {
            throw ApiException.badRequest("The uploaded image could not be found. Please upload it again.");
        }
        return url;
    }

    public void delete(String url) {
        if (url == null || !STORED_URL.matcher(url).matches()) return;
        try {
            Files.deleteIfExists(listingDir.resolve(url.substring(URL_PREFIX.length())));
        } catch (IOException ignored) {
            // A leftover file is harmless; never fail a listing edit over it.
        }
    }

    /** Magic-number sniffing. Returns canonical extension or null. */
    static String detectType(byte[] b, int n) {
        if (n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (n >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
        if (n >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') return "gif";
        if (n >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }
}
