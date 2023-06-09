package io.github.cruciblemc.praesidium_evolutionis.scheduler;

import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
class ForgeAsyncDebugger {

    private final int expiry;
    private final Class<? extends Runnable> clazz;
    private ForgeAsyncDebugger next = null;

    ForgeAsyncDebugger(final int expiry, final Class<? extends Runnable> clazz) {
        this.expiry = expiry;
        this.clazz = clazz;
    }

    final ForgeAsyncDebugger getNextHead(final int time) {
        ForgeAsyncDebugger next, current = this;
        while (time > current.expiry && (next = current.next) != null) {
            current = next;
        }
        return current;
    }

    final ForgeAsyncDebugger setNext(final ForgeAsyncDebugger next) {
        return this.next = next;
    }

    void debugTo(final StringBuilder string) {
        for (ForgeAsyncDebugger next = this; next != null; next = next.next) {
            string.append(':').append(next.clazz.getName()).append('@').append(next.expiry).append(',');
        }
    }
}
