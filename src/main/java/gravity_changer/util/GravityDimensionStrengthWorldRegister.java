package gravity_changer.util;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.resources.ResourceLocation;

public class GravityDimensionStrengthWorldRegister implements WorldComponentInitializer {

    public static final ComponentKey<GravityDimensionStrengthInterface> GRAVITY_DIMENSION_STRENGTH_MODIFIER =
            ComponentRegistry.getOrCreate(new ResourceLocation("gravityapi", "gravity_dimension_strength"), GravityDimensionStrengthInterface.class);

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(GRAVITY_DIMENSION_STRENGTH_MODIFIER, GravityDimensionStrengthComponent::new);
    }
}
