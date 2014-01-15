package org.realityforge.replicant.example.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback invoked on completion of every job
 */
public class GlobalAsyncCallback
  implements AsyncCallback
{
  @Override
  public void onFailure( final Throwable caught )
  {
    Window.alert( "Failed!" );
  }

  @Override
  public void onSuccess( final Object result )
  {
  }
}
