package io.github.vimasig.bozar.obfuscator.utils.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.zip.ZipEntry;

@Getter
@AllArgsConstructor
public class ResourceWrapper {

    private final ZipEntry zipEntry;
    @Setter
    private byte[] bytes;
}
