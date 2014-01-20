package org.realityforge.replicant.example.server.service.tyrell.replicate;

import java.util.List;
import javax.annotation.Nonnull;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.json.JsonEncoder;

public final class Packet
{
  private final int _sequence;
  @Nonnull
  private final List<EntityMessage> _changes;

  public Packet( final int sequence, @Nonnull final List<EntityMessage> changes )
  {
    _sequence = sequence;
    _changes = changes;
  }

  public int getSequence()
  {
    return _sequence;
  }

  @Nonnull
  public List<EntityMessage> getChanges()
  {
    return _changes;
  }

  public String toJson()
  {
    return JsonEncoder.encodeChangeSetFromEntityMessages( getSequence(), getChanges() );
  }
}
