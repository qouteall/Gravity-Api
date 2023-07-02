package gravity_changer.mixin;


import gravity_changer.api.GravityChangerAPI;
import gravity_changer.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Piglin.class)
public abstract class PiglinEntityMixin implements CrossbowAttackMob {
    @Redirect(
        method = "Lnet/minecraft/world/entity/monster/piglin/Piglin;shootCrossbowProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/Projectile;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/piglin/Piglin;shootCrossbowProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;FF)V",
            ordinal = 0
        )
    )
    private void redirect_shoot_shoot_0(Piglin piglinEntity, LivingEntity entity, LivingEntity target, Projectile projectile, float multishotSpray, float speed) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            this.shootCrossbowProjectile(entity, target, projectile, multishotSpray, speed);
            return;
        }
        
        Vec3 targetPos = target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getBbHeight() * 0.3333333333333333D, 0.0D, gravityDirection));
        
        double d = targetPos.x - entity.getX();
        double e = targetPos.z - entity.getZ();
        double f = Math.sqrt(Math.sqrt(d * d + e * e));
        double g = targetPos.y - projectile.getY() + f * 0.20000000298023224D;
        Vector3f vec3f = this.getProjectileShotVector(entity, new Vec3(d, g, e), multishotSpray);
        projectile.shoot((double) vec3f.x(), (double) vec3f.y(), (double) vec3f.z(), speed, (float) (14 - entity.level().getDifficulty().getId() * 4));
        entity.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
