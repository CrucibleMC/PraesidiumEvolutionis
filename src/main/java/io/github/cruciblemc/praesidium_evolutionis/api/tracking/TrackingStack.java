package io.github.cruciblemc.praesidium_evolutionis.api.tracking;

import cpw.mods.fml.common.ModContainer;
import io.github.cruciblemc.praesidium_evolutionis.PraesidiumEvolutionis;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Stack;

// This is still purely a draft
public final class TrackingStack {
    static final Stack<Frame> contextFrames = new Stack<>();

    private TrackingStack(){
        // Sealed class
    }

    public static CloseableContext pushContext(ModContainer mod, TileEntity tile, Entity entity, EntityPlayerMP player) {
        return new CloseableContext(new Frame(mod, tile, entity, player));
    }


    public static void tickEnd() {
        if (!contextFrames.isEmpty()) {
            PraesidiumEvolutionis.logger.error("TRACKING STACK CORRUPTION");
        }
    }


    @Getter
    @Nullable
    @AllArgsConstructor
    public static final class Frame {
        private ModContainer mod;
        private TileEntity tile;
        private Entity entity;
        private EntityPlayerMP player;
    }

    public final static class CloseableContext implements Closeable {
        private final Frame frame;

        CloseableContext(Frame frame) {
            this.frame = frame;
            contextFrames.push(frame);
        }

        @Override
        public void close() {
            if (contextFrames.pop() != frame) {
                PraesidiumEvolutionis.logger.error("TRACKING STACK CORRUPTION AT:", new Throwable());
            }
        }
    }
}
