replicant-example
=================

[![Build Status](https://secure.travis-ci.org/realityforge/replicant-example.png?branch=master)](http://travis-ci.org/realityforge/replicant-example)

A sample application that demonstrates the use of the [replicant](https://github.com/realityforge/replicant) library.

TODO
====

* Upgrade domgen to generate Encoder. Do this by marking certain entities as "subscribable" and
  traverse to children based on set of modes. Includes a default mode that just goes to all
  inverse relationships?
* create generic server-side subscription handler for all these top level components.
* Handle scenario where session goes away. Gracefully handle re-authentication etc.
* Document how this all works together!
