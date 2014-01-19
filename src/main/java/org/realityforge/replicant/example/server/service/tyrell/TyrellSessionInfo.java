package org.realityforge.replicant.example.server.service.tyrell;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.replicant.example.server.service.tyrell.replicate.Packet;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.ReplicantClient;
import org.realityforge.ssf.SimpleSessionInfo;

public class TyrellSessionInfo
  extends SimpleSessionInfo
  implements ReplicantClient
{
  private final HashSet<Integer> _buildingsOfInterest = new HashSet<>();

  // sequence of last packet delivered
  private int _lastSequence;
  private final LinkedList<Packet> _packets = new LinkedList<>();


  public TyrellSessionInfo( @Nonnull final String sessionID,
                            @Nonnull final String username )
  {
    super( sessionID, username );
  }

  public void registerInterest( final int id )
  {
    _buildingsOfInterest.add( id );
  }

  public void deregisterInterest( final int id )
  {
    _buildingsOfInterest.remove( id );
  }

  public boolean isBuildingInteresting( final int id )
  {
    return _buildingsOfInterest.contains( id );
  }

  @Override
  public void addChangeSet( final List<EntityMessage> changeSet )
  {
    _packets.add( new Packet( newPacketSequence(), changeSet ) );
  }

  @Nullable
  public synchronized Packet poll( final int lastSequenceAcked )
  {
    updateAccessTime();
    final Iterator<Packet> iterator = _packets.iterator();
    while ( iterator.hasNext() )
    {
      final Packet packet = iterator.next();
      if ( packet.getSequence() <= lastSequenceAcked )
      {
        iterator.remove();
      }
      else
      {
        return packet;
      }
    }
    return null;
  }

  private int newPacketSequence()
  {
    return ++_lastSequence;
  }
}
