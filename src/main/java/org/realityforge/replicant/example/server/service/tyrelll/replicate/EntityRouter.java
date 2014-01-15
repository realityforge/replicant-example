package org.realityforge.replicant.example.server.service.tyrelll.replicate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import tyrell.server.entity.tyrell.Building;
import tyrell.server.entity.tyrell.Room;
import tyrell.server.entity.tyrell.TyrellRouter;

public class EntityRouter
  implements TyrellRouter
{
  static final String BUILDING_KEY = "Tyrell.BuildingID";

  public EntityRouter()
  {
  }

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
