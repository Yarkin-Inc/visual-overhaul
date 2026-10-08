package eu.midnightdust.visualoverhaul.packet;
import eu.midnightdust.visualoverhaul.VisualOverhaulCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;
public record HelloPacket(UUID uuid) implements CustomPacketPayload {
    public static final Type<HelloPacket> PACKET_ID = new Type<>(VisualOverhaulCommon.HELLO_PACKET);
    public static final StreamCodec<FriendlyByteBuf, HelloPacket> codec = StreamCodec.ofMember(HelloPacket::write, HelloPacket::read);
    public static HelloPacket read(FriendlyByteBuf buf) { return new HelloPacket(buf.readUUID()); }
    public void write(FriendlyByteBuf buf) { buf.writeUUID(uuid); }
    @Override public Type<HelloPacket> type() { return PACKET_ID; }
}
