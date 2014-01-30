package org.realityforge.replicant.client.transport;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RequestManager
{
  private final Map<String, RequestEntry> _requests = new HashMap<>();
  private int _requestID;

  @Nonnull
  public final RequestEntry newRequestRegistration( final boolean bulkLoad )
  {
    final RequestEntry entry = new RequestEntry( newRequestID(), bulkLoad );
    _requests.put( entry.getRequestID(), entry );
    return entry;
  }

  @Nullable
  public final RequestEntry getRequest( @Nonnull final String requestID )
  {
    return _requests.get( requestID );
  }

  public final boolean completeRequest( @Nonnull final String requestID )
  {
    final RequestEntry entry = _requests.remove( requestID );
    if ( null != entry )
    {
      entry.complete();
      return true;
    }
    else
    {
      return false;
    }
  }

  protected String newRequestID()
  {
    return String.valueOf( ++_requestID );
  }
}
