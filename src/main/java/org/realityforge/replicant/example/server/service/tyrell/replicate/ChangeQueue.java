package org.realityforge.replicant.example.server.service.tyrell.replicate;

import java.util.Collection;
import java.util.LinkedList;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import org.realityforge.replicant.server.EntityMessage;

@Local
public interface ChangeQueue
{
  /**
   * Queue the specified messages to be saved as a change set.
   *
   * @param messages the messages.
   */
  void saveEntityMessages( @Nonnull Collection<EntityMessage> messages );

  /**
   * Route the global change set to the individual subscribers.
   *
   * @param changes the changes
   */
  void routeMessages( @Nonnull final LinkedList<Change> changes );
}
