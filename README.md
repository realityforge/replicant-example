replicant-example
=================

[![Build Status](https://secure.travis-ci.org/realityforge/replicant-example.png?branch=master)](http://travis-ci.org/realityforge/replicant-example)

A sample application that demonstrates the use of the [replicant](https://github.com/realityforge/replicant) library.

TODO
====

* Rework transport layer so that it can occur over websockets, sse, long polling etc.
* Remove the need for the ugly hack known as domgens "graph_to_subscribe" (Consider most of subscription manager and perhaps remote interface?)
* Handle scenario where session goes away. Gracefully handle re-authentication etc.
* Document how this all works together!
