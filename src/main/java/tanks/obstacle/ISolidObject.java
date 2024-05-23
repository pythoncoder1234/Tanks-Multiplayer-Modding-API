package tanks.obstacle;

public interface ISolidObject
{
	default boolean rayCollision() { return false; }

	/** Horizontal faces are top/bottom faces, as they span the X direction. */
	Face[] getHorizontalFaces();

	/** Vertical faces are left/right faces, as they span the Y direction. */
	Face[] getVerticalFaces();
}
