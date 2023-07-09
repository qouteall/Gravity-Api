package gravity_changer.mob_effect;

import gravity_changer.GravityComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class GravityStrengthMobEffect extends MobEffect {
    
    public final double base;
    public final int signum;
    
    public static final GravityStrengthMobEffect INCREASE =
        new GravityStrengthMobEffect(0x98D982, 1.2, 1);
    public static final GravityStrengthMobEffect DECREASE =
        new GravityStrengthMobEffect(0x98D982, 0.7, 1);
    
    // it turns gravity into levitation but does not change player orientation
    public static final GravityStrengthMobEffect REVERSE =
        new GravityStrengthMobEffect(0x98D982, 1.0, -1);
    
    protected GravityStrengthMobEffect(int color, double base, int signum) {
        super(MobEffectCategory.NEUTRAL, color);
        this.base = base;
        this.signum = signum;
    }
    
    public double getGravityStrengthMultiplier(int level) {
        return Math.pow(base, (double) level) * signum;
    }
    
    private double apply(LivingEntity entity, double strength) {
        MobEffectInstance effectInstance = entity.getEffect(this);
        
        if (effectInstance == null) {
            return strength;
        }
        
        int level = effectInstance.getAmplifier() + 1;
        return strength * getGravityStrengthMultiplier(level);
    }
    
    public static void init() {
        GravityComponent.GRAVITY_STRENGTH_MODIFIER_EVENT.register(
            (component, strength) -> {
                double result = strength;
                
                if (component.entity instanceof LivingEntity livingEntity) {
                    result = INCREASE.apply(livingEntity, result);
                    result = DECREASE.apply(livingEntity, result);
                    result = REVERSE.apply(livingEntity, result);
                }
                
                return result;
            }
        );
    
        Registry.register(
            BuiltInRegistries.MOB_EFFECT,
            new ResourceLocation("gravity_changer:strength_increase"),
            INCREASE
        );
        
        Registry.register(
            BuiltInRegistries.MOB_EFFECT,
            new ResourceLocation("gravity_changer:strength_decrease"),
            DECREASE
        );
        
        Registry.register(
            BuiltInRegistries.MOB_EFFECT,
            new ResourceLocation("gravity_changer:strength_reverse"),
            REVERSE
        );
    }
}
