package org.realityforge.replicant.example.client.services;

public interface DataLoaderService
{
  void connect();

  void disconnect();

  TyrellClientSession getSession();

  void downloadAll();

  void subscribeToAll();
}
