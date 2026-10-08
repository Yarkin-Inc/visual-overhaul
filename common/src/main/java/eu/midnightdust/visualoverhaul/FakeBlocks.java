package eu.midnightdust.visualoverhaul;
import java.io.*;
import java.util.*;
import eu.midnightdust.visualoverhaul.block.renderer.DynamicModels;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
public final class FakeBlocks {
    private static final Map<Identifier, String> SOURCES = new HashMap<>();
    private static final Map<Identifier, ItemQuads> MODELS = new HashMap<>();
    public static void reload(ResourceManager manager) {
        SOURCES.clear(); MODELS.clear();
        manager.listResources("models/fakeblock", id -> id.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (var reader = resource.openAsReader()) {
                var out = new StringWriter(); reader.transferTo(out);
                SOURCES.put(id.withPath(p -> p.substring("models/fakeblock/".length(), p.length()-5)), out.toString());
            } catch (IOException e) { VisualOverhaulCommon.LOGGER.error("Cannot read fake block {}", id, e); }
        });
    }
    public static ItemQuads getModel(Identifier id) {
        return MODELS.computeIfAbsent(id, key -> {
            var source = SOURCES.get(key);
            if (source == null) return ItemQuads.EMPTY;
            try { return DynamicModels.bake(new StringReader(source), key, false); }
            catch (IOException | RuntimeException e) {
                VisualOverhaulCommon.LOGGER.error("Cannot bake fake block {}", key, e); return ItemQuads.EMPTY;
            }
        });
    }
}
