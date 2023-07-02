package gravity_changer.mixin;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04))
    private double multiplyGravity(double constant) {
        return constant * GravityChangerAPI.getGravityStrength(this);
    }
}
