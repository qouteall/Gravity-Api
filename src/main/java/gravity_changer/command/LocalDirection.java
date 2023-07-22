package gravity_changer.command;

public enum LocalDirection {
    DOWN(-1, "down"),
    UP(-1, "up"),
    FORWARD(0, "forward"),
    BACKWARD(2, "backward"),
    LEFT(3, "left"),
    RIGHT(1, "right");
    
    private final int horizontalOffset;
    private final String name;
    
    LocalDirection(int horizontalOffset, String name) {
        this.horizontalOffset = horizontalOffset;
        this.name = name;
    }
    
    public int getHorizontalOffset() {
        return this.horizontalOffset;
    }
    
    public String getName() {
        return this.name;
    }
}
