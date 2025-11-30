package org.masouras.control.filter;

import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileLockedFilter implements FileListFilter<File> {

    @Override
    public boolean accept(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.tryLock()) {
            return lock != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<File> filterFiles(File[] files) {
        return Arrays.stream(files).filter(this::accept).collect(Collectors.toList());
    }

    @Override
    public boolean supportsSingleFileFiltering() {
        return true;
    }

    @Override
    public boolean isForRecursion() {
        return false;
    }
}


