package tanks.gui;

public interface ITrigger
{
    void update();

    void draw();

    default void updateKeybind() {}

    void setPosition(double x, double y);
}
