replicant-example
=================

[![Build Status](https://secure.travis-ci.org/realityforge/replicant-example.png?branch=master)](http://travis-ci.org/realityforge/replicant-example)

A sample application that demonstrates the use of the [replicant](https://github.com/realityforge/replicant) library.

TODO
====

* Allow poll to return a list of packets.
* Rework transport layer so that it can occur over websockets, sse, long polling etc.
* Rework client-side subscription so we can easily track which subscription(s) an entity belongs to and thus which entities should be unloaded when we unsubscribe.
* Rework transport layer so that a client+job identifier comes back from the server. This allows the client to decide whether a changeset is a bulk load or not.
* Rework the graph Encode so that it can handle the scenario where the same object is encountered twice in graph traversal.
* Handle scenario where session goes away. Gracefully handle re-authentication etc.
* Document how this all works together!
