package io.github.cruciblemc.praesidium_evolutionis.config;

import io.github.cruciblemc.omniconfig.api.annotation.AnnotationConfig;
import io.github.cruciblemc.omniconfig.api.annotation.properties.ConfigBoolean;
import io.github.cruciblemc.omniconfig.api.core.SidedConfigType;
import io.github.cruciblemc.omniconfig.api.core.VersioningPolicy;
import io.github.cruciblemc.praesidium_evolutionis.Tags;

@AnnotationConfig(name = Tags.MODID + "/common.cfg",
        policy = VersioningPolicy.NOBLE,
        sided = SidedConfigType.COMMON)
public class CommonConfig {

    @ConfigBoolean(name = "Enabled", category = "Tracking",
            comment = """
                    When enabled, PE will hook into tiles and save who placed it to use the player object for permission checks.
                    On it's own this option does nothing, but allow other fixes to work better on a server environment with protection mods/plugins.
                    """)
    public static boolean tracking_enabled = true;

    @ConfigBoolean(name = "Proxy FakePlayer Messages", category = "Tracking",
            comment = "Determines whether BetterFakePlayers will proxy chat messages they receive to their owners.")
    public static boolean tracking_proxyFakePlayerMessages = true;

    @ConfigBoolean(name = "Enabled", category = "Debugging",
            comment = """
                    Enables all sorts of debugging stuff
                    """)
    public static boolean debugging_enabled = false;
}
