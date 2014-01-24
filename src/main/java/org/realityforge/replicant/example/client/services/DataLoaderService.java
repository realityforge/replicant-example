package org.realityforge.replicant.example.client.services;

public interface DataLoaderService
{
  void connect();

  void disconnect();

  void subscribeToRoster( int rosterID );

  void unsubscribeFromRoster( int rosterID );

  void subscribeToAll();

  void downloadAll();
}
