package gravity_changer.mob_effect;

import gravity_changer.GravityComponent;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class GravityDirectionMobEffect extends MobEffect {
    public static final int COLOR = 0x98D982;
    
    public static final ResourceLocation PHASE = new ResourceLocation("gravity_changer:dir_mob_effect_phase");
    
    public final Direction gravityDirection;
    
    public GravityDirectionMobEffect(Direction gravityDirection) {
        super(MobEffectCategory.NEUTRAL, COLOR);
        this.gravityDirection = gravityDirection;
    }
    
    public static final EnumMap<Direction, GravityDirectionMobEffect> EFFECT_MAP =
        new EnumMap<>(Direction.class);
    
    public static ResourceLocation getEffectId(Direction direction) {
        return switch (direction) {
            case DOWN -> new ResourceLocation("gravity_changer:down");
            case UP -> new ResourceLocation("gravity_changer:up");
            case NORTH -> new ResourceLocation("gravity_changer:north");
            case SOUTH -> new ResourceLocation("gravity_changer:south");
            case WEST -> new ResourceLocation("gravity_changer:west");
            case EAST -> new ResourceLocation("gravity_changer:east");
        };
    }
    
    public static void init() {
        for (Direction dir : Direction.values()) {
            GravityDirectionMobEffect effect = new GravityDirectionMobEffect(dir);
            EFFECT_MAP.put(dir, effect);
    
            Registry.register(
                BuiltInRegistries.MOB_EFFECT, getEffectId(dir), effect
            );
        }
    
        GravityComponent.GRAVITY_DIR_MODIFIER_EVENT.register(
            PHASE, (component, direction) -> {
                Entity entity = component.entity;

                if (!(entity instanceof LivingEntity livingEntity)) {
                    return direction;
                }
                
                Direction curr = direction;
                int maxAmplifier = 0;
                for (GravityDirectionMobEffect dirEffect : GravityDirectionMobEffect.EFFECT_MAP.values()) {
                    MobEffectInstance effectInstance = livingEntity.getEffect(dirEffect);
                    if (effectInstance != null) {
                        int amplifier = effectInstance.getAmplifier();
                        if (amplifier > maxAmplifier) {
                            maxAmplifier = amplifier;
                            curr = dirEffect.gravityDirection;
                        }
                    }
                }
                return curr;
            }
        );
        
    }
}
