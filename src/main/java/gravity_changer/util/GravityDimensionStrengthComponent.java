package gravity_changer.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class GravityDimensionStrengthComponent implements GravityDimensionStrengthInterface {
    double gravityStrength = 1;
    
    private final Level currentWorld;
    
    public GravityDimensionStrengthComponent(Level world) {
        this.currentWorld = world;
    }
    
    @Override
    public double getDimensionGravityStrength() {
        return gravityStrength;
    }
    
    @Override
    public void setDimensionGravityStrength(double strength) {
        if (!currentWorld.isClientSide) {
            gravityStrength = strength;
            GravityDimensionStrengthWorldRegister.GRAVITY_DIMENSION_STRENGTH_MODIFIER.sync(currentWorld);
        }
    }
    
    @Override
    public void readFromNbt(CompoundTag tag) {
        gravityStrength = tag.getDouble("DimensionGravityStrength");
    }
    
    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putDouble("DimensionGravityStrength", gravityStrength);
    }
}
