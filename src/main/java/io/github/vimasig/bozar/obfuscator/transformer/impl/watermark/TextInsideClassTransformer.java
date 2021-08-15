package io.github.vimasig.bozar.obfuscator.transformer.impl.watermark;

import io.github.vimasig.bozar.obfuscator.Bozar;
import io.github.vimasig.bozar.obfuscator.transformer.ClassTransformer;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarCategory;
import io.github.vimasig.bozar.obfuscator.utils.model.BozarConfig;

public class TextInsideClassTransformer extends ClassTransformer {

    public TextInsideClassTransformer(Bozar bozar) {
        super(bozar, "Text inside class", BozarCategory.WATERMARK);
    }

    // Handled at Bozar.java
    // TODO: Handle it here

    @Override
    public BozarConfig.EnableType getEnableType() {
        return new BozarConfig.EnableType(() -> this.getBozar().getConfig().getOptions().getWatermarkOptions().isTextInsideClass(), "Bozar");
    }
}
