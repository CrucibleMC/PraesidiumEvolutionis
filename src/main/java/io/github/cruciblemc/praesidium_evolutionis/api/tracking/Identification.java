package io.github.cruciblemc.praesidium_evolutionis.api.tracking;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@ToString
@EqualsAndHashCode
@Getter
public final class Identification {
    static final Identification nobody = new Identification(new UUID(0L, 0L), "[nobody]");
    @NotNull
    private final UUID uuid;
    @NotNull
    private final String playerName;

    Identification(@NotNull UUID uuid, @NotNull String playerName) {
        this.uuid = Objects.requireNonNull(uuid);
        this.playerName = playerName;
    }

    Identification(@NotNull UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid);
        this.playerName = "[" + uuid + "]";
    }

    public void saveToNbt(@NotNull NBTTagCompound nbt, String prefix) {
        if (!this.equals(nobody)) {
            nbt.setString(prefix + "uuid", uuid.toString());
        }
        nbt.setString(prefix + "name", playerName);
    }
}
