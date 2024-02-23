package io.github.vimasig.bozar.obfuscator.transformer.impl.watermark;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;

import java.util.jar.JarOutputStream;

public class ZipCommentTransformer extends ClassTransformer {

    public ZipCommentTransformer(Bozar bozar) {
        super(bozar, "Zip comment", BozarCategory.WATERMARK);
    }

    @Override
    public void transformOutput(JarOutputStream jarOutputStream) {
        jarOutputStream.setComment(this.getBozar().getConfig().getOptions().getWatermarkOptions().zipCommentText());
    }

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().getWatermarkOptions().zipComment(), "Obfuscation provided by\nhttps://github.com/vimasig/Bozar");
    }
}
