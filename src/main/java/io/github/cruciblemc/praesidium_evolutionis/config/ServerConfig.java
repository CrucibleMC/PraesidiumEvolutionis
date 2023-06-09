package io.github.cruciblemc.praesidium_evolutionis.config;

import io.github.cruciblemc.omniconfig.api.annotation.AnnotationConfig;
import io.github.cruciblemc.omniconfig.api.annotation.properties.ConfigBoolean;
import io.github.cruciblemc.omniconfig.api.core.SidedConfigType;
import io.github.cruciblemc.omniconfig.api.core.VersioningPolicy;
import io.github.cruciblemc.praesidium_evolutionis.Tags;

@AnnotationConfig(name = Tags.MODID + "/server.cfg",
        policy = VersioningPolicy.NOBLE,
        sided = SidedConfigType.COMMON)
public class ServerConfig {

    @ConfigBoolean(name = "Use Modern SNBT", category = "Integration",
            comment = "When enabled, NBT serialization will attempt to use Crucible's SNBT when available.")
    public static boolean integration_useModernSNBT = true;
}
