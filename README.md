# Rc2 Rest

## Architecture

* Rc2 consists of REST app/server, client (OS X, iOS, HTML), and compute engine.  

* Explicitly setup to use Postgresql. It is not easily portable to another database.

* Provides basic REST capabilities for managing user information. The user interfaces to R via a "session" using JSON over a WebSocket. The session connects to the rc2compute engine via JSON over a socket. The compute engine does the actual work with R and the unix shell.

* Includes the HTML client.

* Uses [Dropwizard](https://dropwizard.github.io/dropwizard/, "Dropwizard") as basic server platform. This provides

	* Jersey for REST
	* Jetty for container
	
* Uses [pgjdbc-ng](https://github.com/impossibl/pgjdbc-ng) for database drivers. This allows use of database notifications to find out about file changes from the compute engine.

* Database access is generally accessed via JDBI.

* PersistentObject interface exists for model objects. A static inner class should provide json mapping if required. A Queries interface should provide methods to fetch instances via DBI.

* A jersey filter provides authentication and authorization.

* injection is used to insert the authenticated user and a database connection to resources via an abstract base class.

* Resources should uses appropriate status codes for error and return a list of RCError objects. 

## Style conventions

* Tabs, not spaces.

* class fields are prefixed with an underscore. That way you can tell variables from fields at a glance.

* I hate all upper case. Constants/enums will use camel case.

* all model classes start with a RC prefix (even though they are in a package) so generic class names like User and File won't conflict with library classes.

