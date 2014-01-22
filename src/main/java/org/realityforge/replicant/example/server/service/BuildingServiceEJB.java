package org.realityforge.replicant.example.server.service;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.realityforge.replicant.example.server.entity.Building;
import org.realityforge.replicant.example.server.entity.Room;
import org.realityforge.replicant.example.server.entity.dao.BuildingRepository;
import org.realityforge.replicant.example.server.entity.dao.RoomRepository;

@Stateless
public class BuildingServiceEJB
  implements BuildingService
{
  @EJB
  private BuildingRepository _buildingRepository;

  @EJB
  private RoomRepository _roomRepository;

  @Nonnull
  @Override
  public Building createBuilding( @Nonnull final String name )
  {
    final Building building = new Building();
    building.setName( name );
    _buildingRepository.persist( building );
    return building;
  }

  @Override
  public void removeBuilding( @Nonnull final Building building )
  {
    final ArrayList<Room> rooms = new ArrayList<>( building.getRooms() );
    for ( final Room room : rooms )
    {
      removeRoom( room );
    }
    _buildingRepository.remove( building );
  }

  @Override
  public void setBuildingName( @Nonnull final Building building, @Nonnull final String name )
  {
    building.setName( name );
  }

  @Nonnull
  @Override
  public Room createRoom( @Nonnull final Building building,
                          final int floor,
                          final int localNumber,
                          @Nonnull final String name,
                          final boolean active )
  {
    final Room room = new Room( building, floor );
    room.setLocalNumber( localNumber );
    room.setName( name );
    room.setActive( active );
    _roomRepository.persist( room );
    return room;
  }

  @Override
  public void removeRoom( @Nonnull final Room room )
  {
    _roomRepository.remove( room );
  }

  @Override
  public void setRoomName( @Nonnull final Room room, @Nonnull final String name )
  {
    room.setName( name );
  }

  @Override
  public void setRoomLocalNumber( @Nonnull final Room room, final int localNumber )
  {
    room.setLocalNumber( localNumber );
  }

  @Override
  public void setRoomActivity( @Nonnull final Room room, final boolean active )
  {
    room.setActive( active );
  }
}
