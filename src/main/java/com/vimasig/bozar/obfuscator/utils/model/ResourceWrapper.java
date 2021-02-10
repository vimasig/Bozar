package com.vimasig.bozar.obfuscator.utils.model;

import java.util.zip.ZipEntry;

public class ResourceWrapper {

    private final ZipEntry zipEntry;
    private final byte[] bytes;

    public ResourceWrapper(ZipEntry zipEntry, byte[] bytes) {
        this.zipEntry = zipEntry;
        this.bytes = bytes;
    }

    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
