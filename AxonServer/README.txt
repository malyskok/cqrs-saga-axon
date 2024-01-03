This is the Axon Server distribution.

For information about the Axon Framework and Axon Server,
visit https://docs.axoniq.io.

Running Axon Server
-------------------

Axon Server can run in a cluster (this requires a license). Since Axon Server does not know on initialization if
it should run standalone, or as a node in a cluster, you will have to configure this.

To start the server on a specific node run the command:

    java -jar axonserver.jar

On the first node (or to run standalone) initialize the cluster using:

    java -jar axonserver-cli.jar init-cluster

On the other nodes, connect to the first node using:

    java -jar axonserver-cli.jar register-node -h <first-node-hostname>

For more information on setting up clusters and context check the reference guide at:

https://docs.axoniq.io/reference-guide/axon-server/installation/local-installation/axon-server-ee

Once Axon Server is running you can view its configuration using the Axon Dashboard at http://<axonserver>:8024.
Instead of doing this initialization through the command line, you can also use the Axon Dashboard to initialize.

Release Notes for version 2023.2.1
----------------------------------

Bug fixes:

- TLS communication between Axon Server nodes cannot validate trusted certificates when there is no trust manager file configured
- Deleting a context does not delete all its metrics

Release Notes for version 2023.2.0
----------------------------------

- TLS Certificate and Key Replacement at Runtime

Axon Server now supports the hot (runtime) replacement of certificates and keys used for TLS, eliminating the need for server restarts.

- Enhanced Metrics Exposure

We have revamped the metrics exposed by Axon Server for better clarity and comprehensibility. Adhering to the 4 golden signals terminology, metrics are now systematically organized.
Users can access both old and new style metrics in this version. However, there’s an option to disable the old style metrics.

- Upgraded Diagnostics Package

To aid in issue resolution, Axon Server now provides a more comprehensive diagnostics package.
The package now contains more detailed information about raft status.
It offers a snapshot of metrics and health information.
There’s a listing of files in the replication group.
Information about multi-tier storage is included.
Logs are included as well.

Other Improvements:

- We’ve addressed various security concerns through dependency updates. Additionally, several bugs have been identified and rectified.
- The role ‘MONITOR’ is now granted permission to access the ‘internal/raft/status’ endpoint.
- We’ve transitioned to new versions for some of the external libraries used in Axon Server.

Release Notes for version 2023.1.2
----------------------------------

Bug fixes:

- Metrics no longer collected when an application reconnects to Axon Server

Release Notes for version 2023.1.1
----------------------------------

New Features and Enhancements:

- Initialize standalone

To simplify initialization of Axon Server, it now supports a new property "axoniq.axonserver.standalone=true". When this property is set on a clean Axon Server instance it initializes the server with a "default" context.

- Development mode

Fixed the option to reset the event store from the UI (in development mode). This option now also works in an
Axon Server cluster.

- LDAP extension update

The new version of the LDAP extension supports configuration of a trust manager file. The location of the file
can be specified through the property "axoniq.axonserver.enterprise.ldap.trust-manager-file".

Bug Fixes:

This release contains fixes for the following issues:
- Validation of tiered storage properties when not using the UI
- Race condition while writing to the global index
- Limitation on the number of requests per context fails if there are timed out requests


Release Notes for version 2023.1.0
----------------------------------

New Features and Enhancements:

- Event Transformation

The new Event Transformation feature allows users to perform specific event transformations like updates and deletes in the event store, utilizing the Event Transformation API. This functional change is intended to facilitate more flexible event management in rare instances where modifications are unavoidable.

- Forced Client Reconnection

In the application view, users are now provided with an option to force the client to reconnect. This addition aims to offer a practical tool for addressing client connectivity issues.

- Node Removal from Cluster

It is now possible to remove a node from the cluster through the user interface (UI). This functionality, previously accessible only via the command-line interface (CLI) and REST API, has been expanded to the UI for broader accessibility.

- Temporary Adjustment to Development Mode

In this release, we have temporarily disabled the 'Development Mode/Event Purge' feature. Users should now utilize the 'Delete/Create Context' operation as an alternative. This change will remain in place until a more efficient solution is implemented.

- Enhanced Memory Management
In an effort to optimize performance, we have updated Axon Server's approach to memory management for file resources. Prior to this release, Axon Server primarily depended on the Java garbage collector to reclaim memory used by memory-mapped files. With this update, memory management is now undertaken directly by Axon Server, enhancing efficiency in file resource usage.

Bug Fixes

This release also contains fixes for the following issues:
- Replication group creation did not work in conjunction with the HTTPS (-s) option
- Race condition in unregister node leaves node partially in the cluster

Product Updates

- Unified Axon Server artifact

The Axon Server artifact has been updated to simplify the deployment process. Instead of separate artifacts for the Axon Server Standard Edition and Enterprise Edition, from now on we are releasing a single artifact. The Axon Server features will adjust automatically based on the presence of a provided license. Note that the Axon Server Standard Edition remains open-source, but separate releases will no longer be made.


For release notes on earlier releases (Standard Edition and Enterprise Edition) check the release notes pages in the reference guide (https://docs.axoniq.io/reference-guide/release-notes/rn-axon-server).

Configuring Axon Server
=======================

Axon Server uses sensible defaults for all of its settings, so it will actually
run fine without any further configuration. However, if you want to make some
changes, below are the most common options. You can change them using an
"axonserver.properties" file in the directory where you run Axon Server. For the
full list, see the Reference Guide. https://docs.axoniq.io/reference-guide/axon-server

* axoniq.axonserver.name
  This is the name Axon Server uses for itself. The default is to use the
  hostname.
* axoniq.axonserver.hostname
  This is the hostname clients will use to connect to the server. Note that
  an IP address can be used if the name cannot be resolved through DNS.
  The default value is the actual hostname reported by the OS.
* server.port
  This is the port where Axon Server will listen for HTTP requests,
  by default 8024.
* axoniq.axonserver.port
  This is the port where Axon Server will listen for gRPC requests,
  by default 8124.
* axoniq.axonserver.internal-port
  This is the port where Axon Server will listen for gRPC requests from other AxonServer nodes,
  by default 8224.
* axoniq.axonserver.event.storage
  This setting determines where event messages are stored, so make sure there
  is enough diskspace here. Losing this data means losing your Events-sourced
  Aggregates' state! Conversely, if you want a quick way to start from scratch,
  here's where to clean.
* axoniq.axonserver.snapshot.storage
  This setting determines where aggregate snapshots are stored.
* axoniq.axonserver.controldb-path
  This setting determines where Axon Server stores its configuration information.
  Losing this data will affect Axon Server's ability to determine which
  applications are connected, and what types of messages they are interested
  in.
* axoniq.axonserver.replication.log-storage-folder
  This setting determines where the replication logfiles are stored.
* axoniq.axonserver.accesscontrol.enabled
  Setting this to true will require clients to pass a token.

The Axon Server HTTP server
===========================

Axon Server provides two servers; one serving HTTP requests, the other gRPC.
By default, these use ports 8024 and 8124 respectively, but you can change
these in the settings as described above.

The HTTP server has in its root context a management Web GUI, a health
indicator is available at "/actuator/health", and the REST API at "/v1'. The
API's Swagger endpoint finally, is available at "/swagger-ui/index.html", and gives
the documentation on the REST API.
