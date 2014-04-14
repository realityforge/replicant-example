replicant-example
=================

[![Build Status](https://secure.travis-ci.org/realityforge/replicant-example.png?branch=master)](http://travis-ci.org/realityforge/replicant-example)

A sample application that demonstrates the use of the [replicant](https://github.com/realityforge/replicant) library.

TODO
====

* Rework transport layer so that it can occur over websockets, sse, long polling etc.
* Figure out a mechanism for modifying subscription for shifts based on new days.
* Handle scenario where session goes away. Gracefully handle re-authentication etc.
* Document how this all works together!
* Rework AbstractTyrellSessionManager so that SubscriptionServiceEJB need not extend it.
