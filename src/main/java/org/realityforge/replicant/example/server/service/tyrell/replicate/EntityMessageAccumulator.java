package org.realityforge.replicant.example.server.service.tyrell.replicate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.realityforge.replicant.server.EntityMessage;

public final class EntityMessageAccumulator<T extends ReplicantClient>
{
  private final Map<T, LinkedList<EntityMessage>> _changeSets = new HashMap<>();

  public void addEntityMessage( final T client, final EntityMessage message )
  {
    getChangeSet( client ).add( message );
  }

  public void complete()
  {
    for ( final Entry<T, LinkedList<EntityMessage>> entry : _changeSets.entrySet() )
    {
      entry.getKey().addPacket( entry.getValue() );
    }
  }

  private LinkedList<EntityMessage> getChangeSet( final T info )
  {
    LinkedList<EntityMessage> clientChangeSet = _changeSets.get( info );
    if ( null == clientChangeSet )
    {
      clientChangeSet = new LinkedList<>();
      _changeSets.put( info, clientChangeSet );
    }
    return clientChangeSet;
  }
}
