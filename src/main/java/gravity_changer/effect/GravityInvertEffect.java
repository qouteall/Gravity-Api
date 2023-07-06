package gravity_changer.effect;

import gravity_changer.GravityComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class GravityInvertEffect extends MobEffect {
    
    public static final int COLOR = 0x98D982;
    
    public static final ResourceLocation PHASE = new ResourceLocation("gravity_changer:invert_effect_phase");
    
    public static final GravityInvertEffect INSTANCE = new GravityInvertEffect();
    
    private GravityInvertEffect() {
        super(MobEffectCategory.NEUTRAL, COLOR);
    }
    
    public static void init() {
        GravityComponent.GRAVITY_DIR_MODIFIER_EVENT.register(
            PHASE, (component, direction) -> {
                Entity entity = component.entity;
        
                if (entity instanceof LivingEntity livingEntity) {
                    if (livingEntity.hasEffect(INSTANCE)) {
                        return direction.getOpposite();
                    }
                }
        
                return direction;
            }
        );
        
        // apply invert after gravity effect
        GravityComponent.GRAVITY_STRENGTH_MODIFIER_EVENT.addPhaseOrdering(
            GravityDirectionEffect.PHASE, GravityInvertEffect.PHASE
        );
    }
    
    
}
