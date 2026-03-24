/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viarewind.legacysupport.util;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class FoliaUtil {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Runs a delayed task scoped to the given entity's region.
     * On Folia uses the entity scheduler; on standard Paper/Spigot uses the global scheduler.
     */
    public static void runEntityDelayed(final Plugin plugin, final Entity entity, final Runnable task, final long delay) {
        if (FOLIA) {
            try {
                final Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                final Method method = scheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
                method.invoke(scheduler, plugin, (Consumer<Object>) t -> task.run(), null, delay);
            } catch (Exception e) {
                Bukkit.getLogger().severe("[ViaRewind-Legacy-Support] Failed to schedule entity delayed task: " + e.getMessage());
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    /**
     * Runs a repeating task scoped to the given entity's region.
     * On Folia uses the entity scheduler; on standard Paper/Spigot uses the global scheduler.
     */
    public static void runEntityTimer(final Plugin plugin, final Entity entity, final Runnable task, final long delay, final long period) {
        if (FOLIA) {
            try {
                final Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                final Method method = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
                method.invoke(scheduler, plugin, (Consumer<Object>) t -> task.run(), null, delay, period);
            } catch (Exception e) {
                Bukkit.getLogger().severe("[ViaRewind-Legacy-Support] Failed to schedule entity timer task: " + e.getMessage());
            }
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }

    /**
     * Runs a repeating task on the global region scheduler.
     * On Folia uses the global region scheduler; on standard Paper/Spigot uses the global scheduler.
     */
    public static void runGlobalTimer(final Plugin plugin, final Runnable task, final long delay, final long period) {
        if (FOLIA) {
            try {
                final Object scheduler = Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
                final Method method = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
                method.invoke(scheduler, plugin, (Consumer<Object>) t -> task.run(), delay, period);
            } catch (Exception e) {
                Bukkit.getLogger().severe("[ViaRewind-Legacy-Support] Failed to schedule global timer task: " + e.getMessage());
            }
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }

    private FoliaUtil() {}
}
