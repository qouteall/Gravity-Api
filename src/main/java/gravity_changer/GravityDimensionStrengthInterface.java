package gravity_changer;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface GravityDimensionStrengthInterface extends Component, AutoSyncedComponent {
    
    
    double getDimensionGravityStrength();
    
    void setDimensionGravityStrength(double strength);
}
