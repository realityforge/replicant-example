package org.realityforge.replicant.example.server.service.tyrell;

import java.util.HashSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.replicant.example.server.service.tyrell.replicate.Packet;
import org.realityforge.replicant.example.server.service.tyrell.replicate.PacketQueue;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.ReplicantClient;
import org.realityforge.ssf.SimpleSessionInfo;

public class TyrellSessionInfo
  extends SimpleSessionInfo
  implements ReplicantClient
{
  private final HashSet<Integer> _buildingsOfInterest = new HashSet<>();

  private final PacketQueue _queue = new PacketQueue();
  private boolean _interestedInAllBuildings;

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
    return _interestedInAllBuildings || _buildingsOfInterest.contains( id );
  }

  @Override
  public void addChangeSet( final List<EntityMessage> changeSet )
  {
    _queue.addPacket( changeSet );
  }

  @Nullable
  public synchronized Packet poll( final int ack )
  {
    updateAccessTime();
    _queue.ack( ack );
    return _queue.nextPacketToProcess();
  }

  public void registerInterestInAll()
  {
    _interestedInAllBuildings = true;
  }
}
