package org.realityforge.replicant.example.server.service.tyrell;

import java.util.Collection;
import java.util.LinkedList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import org.realityforge.replicant.example.server.entity.tyrell.Building;
import org.realityforge.replicant.example.server.service.tyrell.replicate.Change;
import org.realityforge.replicant.example.server.service.tyrell.replicate.ChangeQueue;
import org.realityforge.replicant.server.EntityMessage;

@Stateless
public class SubscriptionServiceEJB
  implements SubscriptionService, ChangeQueue
{
  @Nullable
  @Override
  public String poll( final int lastKnownChangeSetID, @Nonnull final String clientID )
  {
    return null;
  }

  @Override
  @Nonnull
  public String subscribeToBuilding( @Nonnull final String designator, @Nonnull final Building building )
  {
    return "";
  }

  @Override
  public void unsubscribeFromBuilding( @Nonnull final String clientID, @Nonnull final Building building )
  {
  }

  @Override
  public void saveEntityMessages( @Nonnull final Collection<EntityMessage> messages )
  {

  }

  @Override
  public void routeMessages( @Nonnull final LinkedList<Change> changes )
  {
  }
}
