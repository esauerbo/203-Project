import java.util.List;
import processing.core.PImage;

final class Background
{
   public String id;
   public List<PImage> images;
   public int imageIndex;

   public Background(String id, List<PImage> images)
   {
      this.id = id;
      this.images = images;
   }

  /* public static Background getBackgroundCell(WorldModel world, Point pos)
   {
      return world.background[pos.y][pos.x];
   }

   public static void setBackgroundCell(WorldModel world, Point pos,
                                        Background background)
   {
      world.background[pos.y][pos.x] = background;
   }*/
}
