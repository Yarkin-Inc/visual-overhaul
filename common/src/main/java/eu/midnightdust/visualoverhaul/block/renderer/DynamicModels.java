package eu.midnightdust.visualoverhaul.block.renderer;

import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.cuboid.*;
import net.minecraft.client.resources.model.geometry.*;
import net.minecraft.client.resources.model.sprite.*;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Bakes standalone cuboid models against the currently uploaded block/item atlases. */
public final class DynamicModels {
    private static final ModelBaker BAKER = new ModelBaker() {
        private final Interner interner = new Interner() {
            public Vector3fc vector(Vector3fc value) { return new Vector3f(value); }
            public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo value) { return value; }
        };
        public Interner interner() { return interner; }
        public <T> T compute(SharedOperationKey<T> key) { return key.compute(this); }
        public ResolvedModel getModel(Identifier id) { throw new UnsupportedOperationException("Parents resolved before baking"); }
        public BlockStateModelPart missingBlockModelPart() { throw new UnsupportedOperationException(); }
        public MaterialBaker materials() { throw new UnsupportedOperationException("Uses uploaded atlases"); }
    };
    public static ItemQuads bake(Reader reader, Identifier name, boolean disc) throws IOException {
        CuboidModel model = CuboidModel.fromStream(reader);
        var slots = new TextureSlots.Resolver();
        var geometry = model.geometry();
        var seen = new HashSet<Identifier>();
        while (model != null) {
            slots.addLast(model.textureSlots());
            if (geometry == null) geometry = model.geometry();
            if (model.parent() == null) break;
            if (!seen.add(model.parent())) throw new IOException("Cyclic model parent: " + name);
            var file = model.parent().withPath(p -> "models/" + p + ".json");
            try (var parent = Minecraft.getInstance().getResourceManager().getResourceOrThrow(file).openAsReader()) {
                model = CuboidModel.fromStream(parent);
            }
        }
        if (!(geometry instanceof UnbakedCuboidGeometry cuboids)) return ItemQuads.EMPTY;
        var textures = slots.resolve(name::toString);
        var atlases = Minecraft.getInstance().getAtlasManager();
        var blocks = atlases.getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS);
        var items = atlases.getAtlasOrThrow(net.minecraft.data.AtlasIds.ITEMS);
        var quads = new ArrayList<BakedQuad>();
        for (var element : cuboids.elements()) {
            for (var entry : element.faces().entrySet()) {
                var face = entry.getValue();
                var material = textures.getMaterial(face.texture());
                var sprite = blocks.missingSprite();
                if (material != null) {
                    sprite = blocks.getSprite(material.sprite());
                    if (sprite == blocks.missingSprite()) sprite = items.getSprite(material.sprite());
                }
                if (disc && (sprite == blocks.missingSprite() || sprite == items.missingSprite()))
                    sprite = items.getSprite(Identifier.fromNamespaceAndPath("visualoverhaul", "item/music_disc_missing"));
                quads.add(FaceBakery.bakeQuad(BAKER, element.from(), element.to(), face,
                    new Material.Baked(sprite, material != null && material.forceTranslucent()), entry.getKey(),
                    BlockModelRotation.IDENTITY, element.rotation(), element.shadeDirectionOverride(), element.lightEmission()));
            }
        }
        return ItemQuads.split(quads);
    }
}
