package gravity_changer.mixin.debug;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class MixinEntity_Debug {
//    @Shadow private Vec3 position;
//
//    @Inject(
//        method = "setPosRaw", at = @At("HEAD")
//    )
//    private void debugOnSetPos(double x, double y, double z, CallbackInfo ci) {
//        Entity this_ = (Entity) (Object) this;
//        if (this_ instanceof ItemEntity) {
//            String str = "%s ItemEntity#setPosRaw(%s, %s, %s) grav %s %s".formatted(
//                this_.level().isClientSide() ? "client" : "server", x, y, z,
//                GravityChangerAPI.getGravityDirection(this_),
//                GravityChangerAPI.getGravityStrength(this_)
//            );
//            System.out.println(str);
//        }
//    }
}
