package org.realityforge.replicant.example.client.services;

import org.realityforge.replicant.example.client.entity.TyrellClientSession;

public interface DataLoaderService
{
  void connect();

  void disconnect();

  TyrellClientSession getSession();

  void downloadAll();

  void subscribeToAll();
}
