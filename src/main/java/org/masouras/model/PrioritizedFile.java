package org.masouras.model;

import lombok.Getter;

import java.io.File;

public class PrioritizedFile implements Comparable<PrioritizedFile> {
    @Getter
    private final File file;
    private final int priority;

    public PrioritizedFile(File file, int priority) {
        this.file = file;
        this.priority = priority;
    }

    @Override
    public int compareTo(PrioritizedFile other) {
        return Integer.compare(other.priority, this.priority);
    }
}

