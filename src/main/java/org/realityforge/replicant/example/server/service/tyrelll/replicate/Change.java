package org.realityforge.replicant.example.server.service.tyrelll.replicate;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.replicant.server.EntityMessage;

/**
 * A representation of an entity change and any dependent values.
 */
public final class Change
{
  @Nonnull
  private final EntityMessage _entityMessage;
  @Nullable
  private final List<Change> _subscriptionPayload;

  public Change( @Nonnull final EntityMessage entityMessage, @Nullable final List<Change> subscriptionPayload )
  {
    _entityMessage = entityMessage;
    _subscriptionPayload = subscriptionPayload;
  }

  @Nonnull
  public EntityMessage getEntityMessage()
  {
    return _entityMessage;
  }

  @Nullable
  public List<Change> getSubscriptionPayload()
  {
    return _subscriptionPayload;
  }
}
