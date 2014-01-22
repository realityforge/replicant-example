package org.realityforge.replicant.example.server.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import org.realityforge.replicant.example.server.entity.tyrell.Building;
import org.realityforge.replicant.example.server.entity.tyrell.Room;

@Dependent
public class TyrellRouterImpl
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
