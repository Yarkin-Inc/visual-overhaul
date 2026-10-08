package eu.midnightdust.visualoverhaul.fabric;
import eu.midnightdust.visualoverhaul.packet.UpdateItemsPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.Identifier;
import java.util.*;
/** One snapshot per block entity; newly tracking clients receive unchanged inventories too. */
public final class InventorySync {
    private List<ItemStack> previous = List.of();
    private Set<net.minecraft.server.level.ServerPlayer> recipients = Set.of();
    private long lastTick = Long.MIN_VALUE;
    public void tick(BlockEntity entity, Container inventory, Identifier type) {
        long now = entity.getLevel().getGameTime();
        if (lastTick != Long.MIN_VALUE && now - lastTick > 1) recipients = Set.of();
        lastTick = now;
        var current = new ArrayList<ItemStack>();
        for (int i = 0; i < UpdateItemsPacket.slots(type); i++) current.add(inventory.getItem(i));
        boolean changed = current.size() != previous.size();
        for (int i = 0; !changed && i < current.size(); i++) changed = !ItemStack.matches(current.get(i), previous.get(i));
        var watching = new HashSet<net.minecraft.server.level.ServerPlayer>();
        UpdateItemsPacket packet = null;
        for (var player : PlayerLookup.tracking(entity)) {
            if (!ServerPlayNetworking.canSend(player, UpdateItemsPacket.PACKET_ID)) continue;
            watching.add(player);
            if (changed || !recipients.contains(player)) {
                if (packet == null) packet = new UpdateItemsPacket(type, entity.getBlockPos(), current);
                ServerPlayNetworking.send(player, packet);
            }
        }
        if (changed) previous = current.stream().map(ItemStack::copy).toList();
        recipients = watching;
    }
}
