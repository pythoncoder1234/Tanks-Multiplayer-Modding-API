package tanks.obstacle;

public interface ISolidObject
{
	default boolean rayCollision() { return false; }

	Face[] getHorizontalFaces();

	Face[] getVerticalFaces();
}
