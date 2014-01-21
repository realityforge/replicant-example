package org.realityforge.replicant.example.server.service.tyrell;

import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.replicant.server.transport.Packet;
import org.realityforge.replicant.server.transport.PacketQueue;
import org.realityforge.ssf.SimpleSessionInfo;

public class TyrellSessionInfo
  extends SimpleSessionInfo
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

  public final PacketQueue getQueue()
  {
    return _queue;
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
