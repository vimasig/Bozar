package io.github.vimasig.bozar.obfuscator.utils.model;

import java.util.zip.ZipEntry;

public class ResourceWrapper {

    private final ZipEntry zipEntry;
    private byte[] bytes;

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

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
