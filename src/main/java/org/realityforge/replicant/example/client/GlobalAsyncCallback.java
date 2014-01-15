package org.realityforge.replicant.example.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Callback invoked on completion of every job
 */
public class GlobalAsyncCallback
  implements AsyncCallback
{
  private static final Logger LOG = Logger.getLogger( GlobalAsyncCallback.class.getName() );

  @Override
  public void onFailure( final Throwable caught )
  {
    LOG.log( Level.WARNING, "A request failed", caught );
  }

  @Override
  public void onSuccess( final Object result )
  {
    LOG.log( Level.INFO, "A request succeeded: " + result );
  }
}
