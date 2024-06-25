package com.example.filecopier;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Named
@RequestScoped
public class FileCopierBean {
    private String sourceDirectory;
    private String targetDirectory;
    private String targetFolderName;
    private StringBuilder output = new StringBuilder();

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public String getTargetFolderName() {
        return targetFolderName;
    }

    public void setTargetFolderName(String targetFolderName) {
        this.targetFolderName = targetFolderName;
    }

    public String getOutput() {
        return output.toString();
    }

    public void copyFiles() {
        String finalTargetFolderName = targetFolderName == null || targetFolderName.isEmpty()
                ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : targetFolderName;
        Path targetDir = Paths.get(targetDirectory, finalTargetFolderName);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
        } catch (IOException e) {
            output.append("Failed to create target directory: ").append(e.getMessage()).append("\n");
            return;
        }

        try {
            Files.walkFileTree(Paths.get(sourceDirectory), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = targetDir.resolve(Paths.get(sourceDirectory).relativize(file));
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    output.append("Copied: ").append(file).append(" to ").append(targetFile).append("\n");
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetSubDir = targetDir.resolve(Paths.get(sourceDirectory).relativize(dir));
                    if (Files.notExists(targetSubDir)) {
                        Files.createDirectories(targetSubDir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            output.append("Failed to copy files: ").append(e.getMessage()).append("\n");
        }
    }
}
