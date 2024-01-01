package gravity_changer.mixin.client;


import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemPickupParticle.class)
public abstract class ItemPickupParticleMixin {
    @Shadow
    @Final
    private Entity target;
    
    @Shadow
    private double targetX;
    
    @Shadow
    private double targetY;
    
    @Shadow
    private double targetZ;
    
    /**
     * Make item absorption destination correct.
     * @author qouteall
     * @reason simpler than multiple injections
     */
    @Overwrite
    private void updatePosition() {
        Vec3 entityPos = target.position();
        Vec3 eyePos = target.getEyePosition();
        Vec3 mid = eyePos.add(entityPos).scale(0.5);
        
        this.targetX = mid.x();
        this.targetY = mid.y();
        this.targetZ = mid.z();
    }
}
