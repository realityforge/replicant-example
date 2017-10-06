package org.realityforge.tyrell.client.model;

import org.realityforge.tyrell.client.ui.ApplicationController;

public final class AppData
{
  public static final ViewService viewService = new Arez_ViewService();
  /**
   * As an ugly hack we directly update this when instantiating component.
   * Should all be done via injection.
   */
  public static ApplicationController controller;

  private AppData()
  {
  }
}
