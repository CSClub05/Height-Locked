package org.gradle.wrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A small, source-included Gradle bootstrap used in place of the opaque binary
 * wrapper JAR. It downloads, verifies, extracts, and launches the configured
 * Gradle distribution.
 */
public final class GradleWrapperMain {
    private GradleWrapperMain() {
    }

    public static void main(String[] args) throws Exception {
        Path projectDir = locateProjectDir();
        Path propertiesPath = projectDir.resolve("gradle/wrapper/gradle-wrapper.properties");
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(propertiesPath)) {
            properties.load(input);
        }

        URI distributionUri = URI.create(required(properties, "distributionUrl"));
        String expectedSha = required(properties, "distributionSha256Sum").toLowerCase();
        String zipName = Path.of(distributionUri.getPath()).getFileName().toString();
        String distributionName = zipName.endsWith(".zip")
                ? zipName.substring(0, zipName.length() - 4)
                : zipName;

        Path cacheDir = Path.of(System.getProperty("user.home"), ".gradle", "wrapper", "dists", distributionName);
        Path zipPath = cacheDir.resolve(zipName);
        Path unpackDir = cacheDir.resolve("unpacked");
        Files.createDirectories(cacheDir);

        if (!Files.isRegularFile(zipPath) || !sha256(zipPath).equals(expectedSha)) {
            download(distributionUri, zipPath);
        }
        verify(zipPath, expectedSha);

        Path gradleHome = findGradleHome(unpackDir);
        if (gradleHome == null) {
            deleteRecursively(unpackDir);
            Files.createDirectories(unpackDir);
            unzip(zipPath, unpackDir);
            gradleHome = findGradleHome(unpackDir);
        }
        if (gradleHome == null) {
            throw new IOException("Could not locate the Gradle home after extracting " + zipPath);
        }

        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        Path executable = gradleHome.resolve("bin").resolve(windows ? "gradle.bat" : "gradle");
        if (!windows) {
            executable.toFile().setExecutable(true);
        }

        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        command.addAll(List.of(args));

        Process process = new ProcessBuilder(command)
                .directory(projectDir.toFile())
                .inheritIO()
                .start();
        System.exit(process.waitFor());
    }

    private static Path locateProjectDir() throws Exception {
        Path jar = Path.of(GradleWrapperMain.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toAbsolutePath();
        // <project>/gradle/wrapper/gradle-wrapper.jar
        return jar.getParent().getParent().getParent();
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + key + " in gradle-wrapper.properties");
        }
        return value;
    }

    private static void download(URI uri, Path destination) throws Exception {
        Path temporary = destination.resolveSibling(destination.getFileName() + ".part");
        Files.deleteIfExists(temporary);
        System.out.println("Downloading " + uri);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<Path> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofFile(temporary)
        );
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Files.deleteIfExists(temporary);
            throw new IOException("Gradle download failed with HTTP " + response.statusCode());
        }
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void verify(Path file, String expectedSha) throws Exception {
        String actualSha = sha256(file);
        if (!actualSha.equals(expectedSha)) {
            Files.deleteIfExists(file);
            throw new IOException("Gradle distribution checksum mismatch. Expected "
                    + expectedSha + " but got " + actualSha);
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new BufferedInputStream(Files.newInputStream(file))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void unzip(Path zip, Path destination) throws IOException {
        try (ZipInputStream input = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zip)))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                Path output = destination.resolve(entry.getName()).normalize();
                if (!output.startsWith(destination)) {
                    throw new IOException("Unsafe ZIP entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Files.createDirectories(output.getParent());
                    try (BufferedOutputStream stream = new BufferedOutputStream(Files.newOutputStream(output))) {
                        input.transferTo(stream);
                    }
                }
                input.closeEntry();
            }
        }
    }

    private static Path findGradleHome(Path unpackDir) throws IOException {
        if (!Files.isDirectory(unpackDir)) {
            return null;
        }
        try (var children = Files.list(unpackDir)) {
            return children
                    .filter(Files::isDirectory)
                    .filter(path -> Files.isDirectory(path.resolve("bin")))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var stream = Files.walk(path)) {
            stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(item -> {
                        try {
                            Files.deleteIfExists(item);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            throw e;
        }
    }
}
