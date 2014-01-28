package org.realityforge.replicant.example.client.services;

import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManager;

public interface DataLoaderService
{
  void connect();

  void disconnect();

  TyrellSubscriptionManager getSubscriptionManager();

  void downloadAll();

  void subscribeToAll();
}
