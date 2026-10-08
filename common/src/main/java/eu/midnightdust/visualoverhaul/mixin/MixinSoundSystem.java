package eu.midnightdust.visualoverhaul.mixin;
import eu.midnightdust.visualoverhaul.util.SoundTest;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(SoundEngine.class)
public abstract class MixinSoundSystem {
    @Inject(method="play",at=@At("RETURN"))
    private void vo$play(SoundInstance sound,CallbackInfoReturnable<SoundEngine.PlayResult> ci) {
        if(sound.getSource()==SoundSource.RECORDS && ci.getReturnValue()!=SoundEngine.PlayResult.NOT_STARTED)
            SoundTest.soundPos.put(BlockPos.containing(sound.getX(),sound.getY(),sound.getZ()),sound.getIdentifier());
    }
    @Inject(method="stop(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",at=@At("HEAD"))
    private void vo$stop(SoundInstance sound,CallbackInfo ci) {
        if(sound!=null && sound.getSource()==SoundSource.RECORDS)
            SoundTest.soundPos.remove(BlockPos.containing(sound.getX(),sound.getY(),sound.getZ()),sound.getIdentifier());
    }
    @Inject(method="stopAll",at=@At("HEAD"))
    private void vo$clear(CallbackInfo ci) { SoundTest.soundPos.clear(); }
}
