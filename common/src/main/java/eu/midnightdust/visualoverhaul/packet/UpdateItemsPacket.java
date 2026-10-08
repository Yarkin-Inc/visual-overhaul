package eu.midnightdust.visualoverhaul.packet;
import static eu.midnightdust.visualoverhaul.VisualOverhaulCommon.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
public record UpdateItemsPacket(Identifier blockTypeID, BlockPos pos, List<ItemStack> inv) implements CustomPacketPayload {
    public static final Type<UpdateItemsPacket> PACKET_ID = new Type<>(UPDATE_ITEMS_PACKET);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateItemsPacket> codec = StreamCodec.ofMember(UpdateItemsPacket::write, UpdateItemsPacket::read);
    public UpdateItemsPacket {
        if (inv.size() != slots(blockTypeID)) throw new IllegalArgumentException("Invalid visual inventory size");
        pos = pos.immutable();
        inv = inv.stream().map(ItemStack::copy).toList();
    }
    public static int slots(Identifier type) {
        if (type.equals(UPDATE_TYPE_RECORD)) return 1;
        if (type.equals(UPDATE_TYPE_FURNACE_ITEMS)) return 3;
        if (type.equals(UPDATE_TYPE_POTION_BOTTLES)) return 5;
        throw new IllegalArgumentException("Unknown visual inventory type: " + type);
    }
    public static UpdateItemsPacket read(RegistryFriendlyByteBuf buf) {
        var type = buf.readIdentifier();
        var pos = buf.readBlockPos();
        int size = slots(type);
        var items = new ArrayList<ItemStack>(size);
        for (int i = 0; i < size; i++) items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        return new UpdateItemsPacket(type, pos, items);
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(blockTypeID); buf.writeBlockPos(pos);
        for (var stack : inv) ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
    }
    @Override public Type<UpdateItemsPacket> type() { return PACKET_ID; }
}
