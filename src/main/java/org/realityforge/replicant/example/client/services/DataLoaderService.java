package org.realityforge.replicant.example.client.services;

import org.realityforge.replicant.example.client.services.replicant.SubscriptionManager;

public interface DataLoaderService
{
  void connect();

  void disconnect();

  SubscriptionManager getSubscriptionManager();

  void downloadAll();
}
