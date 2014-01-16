package org.realityforge.replicant.example.server.service.tyrell;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.replicant.example.server.entity.tyrell.Building;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageGenerator;

/**
 * Utility class that encodes entities as sets of messages.
 */
public class Encoder
{
  private final EntityMessageGenerator _messageGenerator;

  @Inject
  public Encoder( final EntityMessageGenerator messageGenerator )
  {
    _messageGenerator = messageGenerator;

  }

  void encodeBuilding( @Nonnull final LinkedList<EntityMessage> messages, @Nonnull final Building building )
  {
    encodeObject( messages, building );
    encodeObjects( messages, building.getRooms() );
  }

  void encodeObjects( @Nonnull final LinkedList<EntityMessage> messages, @Nonnull final List objects )
  {
    for ( final Object object : objects )
    {
      encodeObject( messages, object );
    }
  }

  void encodeObject( @Nonnull final LinkedList<EntityMessage> messages, @Nullable final Object object )
  {
    if ( null != object )
    {
      final EntityMessage message = _messageGenerator.convertToEntityMessage( object, true );
      if ( null != message )
      {
        messages.add( message );
      }
    }
  }
}
