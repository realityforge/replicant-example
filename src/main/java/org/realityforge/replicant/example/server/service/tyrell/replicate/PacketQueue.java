package org.realityforge.replicant.example.server.service.tyrell.replicate;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.realityforge.replicant.server.EntityMessage;

/**
 * A queue of packets for session.
 */
public class PacketQueue
{
  /**
   * List of packets associated with session.
   */
  private final LinkedList<Packet> _packets = new LinkedList<>();
  /**
   * Sequence of last packet removed from queue.
   */
  private int _lastPacketAcked;
  /**
   * Sequence of next packet to be added to the queue.
   */
  private int _nextPacketSeq = 1;

  /**
   * @return the number of packets in queue.
   */
  public int size()
  {
    return _packets.size();
  }

  /**
   * Acknowledge that the remote side has received packet with specified sequence.
   *
   * @param sequence the sequence.
   */
  public void ack( final int sequence )
  {
    removePacketsLessThanOrEqual( sequence );
    _lastPacketAcked = sequence;
  }

  public Packet nextPacketToProcess()
  {
    if ( 0 == _packets.size() )
    {
      return null;
    }
    else
    {
      final Packet packet = _packets.getFirst();
      if ( packet.isPrevious( _lastPacketAcked ) )
      {
        return packet;
      }
      else
      {
        return null;
      }
    }
  }

  public int getLastPacketAcked()
  {
    return _lastPacketAcked;
  }

  /**
   * Add packet to queue.
   *
   * @param changes the changes to create packet from.
   */
  public void addPacket( @Nonnull final List<EntityMessage> changes )
  {
    final Packet packet = new Packet( _nextPacketSeq++, changes );
    if ( !_packets.contains( packet ) )
    {
      _packets.add( packet );
      Collections.sort( _packets );
    }
  }

  /**
   * Remove packets with a sequence less than or equal to specified sequence.
   *
   * @param sequence the sequence
   */
  final void removePacketsLessThanOrEqual( final int sequence )
  {
    final Iterator<Packet> iterator = _packets.iterator();
    while ( iterator.hasNext() )
    {
      final Packet packet = iterator.next();
      final int seq = packet.getSequence();

      if ( packet.isLessThanOrEqual( sequence ) )
      {
        iterator.remove();
        if ( seq == sequence )
        {
          break;
        }
      }
    }
  }

  /**
   * Return the packet with specified sequence.
   *
   * @param sequence the sequence.
   * @return the packet with sequence or null if no such packet.
   */
  public Packet getPacket( final int sequence )
  {
    for ( final Packet packet : _packets )
    {
      final int seq = packet.getSequence();
      if ( seq == sequence )
      {
        return packet;
      }
    }
    return null;
  }

  public String toString()
  {
    return "PacketQueue[" + _packets + "]";
  }
}
