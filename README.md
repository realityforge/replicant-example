replicant-example
=================

[![Build Status](https://secure.travis-ci.org/realityforge/replicant-example.png?branch=master)](http://travis-ci.org/realityforge/replicant-example)

A sample application that demonstrates the use of the [replicant](https://github.com/realityforge/replicant) library.

TODO
====

* Add EntityListener method so you can be notified when entities are initially added.
  - Remove BuildingDataLoaded Event
* Consider stopping the propagation of attribute change messages on initial add of entity.
* Inline bulk loads so that they are part of packet layer and are sequenced correctly.
* create generic server-side subscription handler for all these top level components.
* Handle scenario where session goes away. Gracefully handle re-authentication etc.
* Create a PacketManager that manages the sequencing and control flow associated with connections.
* Document how this all works together!
