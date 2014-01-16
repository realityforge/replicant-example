package org.realityforge.replicant.example.server.service.tyrell.replicate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.realityforge.replicant.example.server.entity.tyrell.Building;
import org.realityforge.replicant.example.server.entity.tyrell.Room;
import org.realityforge.replicant.example.server.entity.tyrell.TyrellRouter;

public class EntityRouter
  implements TyrellRouter
{
  public static final String BUILDING_KEY = "Tyrell.BuildingID";

  @Nonnull
  @Override
  public Map<String, Serializable> routeBuilding( @Nonnull final Building entity )
  {
    final HashMap<String, Serializable> map = new HashMap<>();
    map.put( BUILDING_KEY, entity.getID() );
    return map;
  }

  @Nonnull
  @Override
  public Map<String, Serializable> routeRoom( @Nonnull final Room entity )
  {
    return routeBuilding( entity.getBuilding() );
  }
}
