# Rc2 Rest

## Architecture

* Rc2 consists of REST app/server, client (OS X, iOS, HTML), and compute engine.  

* Explicitly setup to use Postgresql. It is not easily portable to another database.

* Provides basic REST capabilities for managing user information. The user interfaces to R via a "session" using JSON over a WebSocket. The session connects to the rc2compute engine via JSON over a socket. The compute engine does the actual work with R and the unix shell.

* Includes the HTML client.

* Uses [Dropwizard](https://dropwizard.github.io/dropwizard/, "Dropwizard") as basic server platform. This provides

	* Jersey for REST
	* Jetty for container
	* Jackson for JSON
	* JDBI for database access
	
* Uses [pgjdbc-ng](https://github.com/impossibl/pgjdbc-ng) for database drivers. This allows use of database notifications to find out about file changes from the compute engine.

* PersistentObject interface exists for model objects. A static inner class should provide json mapping if required. A Queries interface should provide methods to fetch instances via DBI. Abstract class query interfaces require an external source file.

* A jersey filter provides authentication and authorization.

* injection is used to insert the authenticated user and a database connection to resources via an abstract base class.

* Resources should uses appropriate status codes for error and return a list of RCError objects. 

## Web Client

* Uses [aurelia](http://aurelia.io/) and [Bootstrap](http://getbootstrap.com/)

## Eclipse issues

* After importing the maven project, be sure to remove eclipse's junit library from Properties->Java Build Path->Libraries. It will include versions of hamcrest that don't match with that used by the maven build.

* In Properties->Java Compiler->Annotation Processing:

	* enable project specific settings
	* enable annotations processing
	* enable processing in editor
	* generated source directory: .apt_generated
	
## Password hashing

To get a hashed password to insert in the database, use `mvn exec:java -Dexec.mainClass="edu.wvu.stat.rc2.Rc2Application" -Dexec.args="hashpw"`

## Authentication/Authorization

Only these URLs are allowed without authentication

* POST to /login
* /robots.txt sends a file that disallows all URLs

All other requests must include an **Rc2-Auth** header with a 3 part token. If that header is not found, it also looks for the  token in the **Sec-WebSocket-Protocol** header, which is the only one javascript can set on a websocket upgrade request.

The token is in 3 parts separated by an underscore. See *RC2AuthServletFilter.java* and *LoginResource.java* for details.

## File/Image client-side caching

All instances of RCFile are sent with an E-Tag that is unique per version of the file. If it hasn't changed, content won't be returned or queried from the database.

Session images are handled by id. Since they are never modified, the last-modified header is set to the previous day. Request check the If-Modified-Since header and return a 304.

## Sessions

The RCSessionManager is a singleton that caches open sessions. It runs a cleanup task periodically that closed unused sessions. It also listens for rcfile notifications from the database and passes them on to any open sessions.

An instance of RCSession is opened for each workspace currently being used. Multiple websockets can connect to a single session. 

## WebSockets

Text messages are json messages described elsewhere.

Binary messages consist of a version byte (currently 1), followed by a message type byte (1 for image). Remaining data is payload for message type. For images, it is a 32 bit integer with the imageId. Metadata will have previously been sent via json.

## Integration Testing

To run w/o unit tests, use `mvn verify -Dskip.utests=true`. That will create and use a clean rc2itest database using rc2.sql and testData.sql. The system property `rc2.dbname` will be set to use the rc2itest database.

## Style conventions

* Tabs, not spaces. EditorConfig is setup so you don't have to worry if you use BBEdit or install the [eclipse plugin](https://github.com/ncjones/editorconfig-eclipse).

* class fields are prefixed with an underscore. That way you can tell variables from fields at a glance.

* I hate all upper case. Constants/enums will use camel case.

* all model classes start with a RC prefix (even though they are in a package) so generic class names like User and File won't conflict with library classes.

## Postgresql

For development the lastest version of 9.4 should be installed with the command `./configure -prefix=/usr/local/pgsql9.4 --with-perl --without-python --with-bonjour --with-openssl --with-libxml --with-libxslt`

If running on El Capitan, brew needs to be updated, then `brew install openssl` run. Then the install command for pgsql will be `LDFLAGS=-L/usr/local/opt/openssl/lib CPPFLAGS=-I/usr/local/opt/openssl/include ./configure -prefix=/usr/local/pgsql9.4 --with-perl --without-python --with-bonjour --with-openssl --with-libxml --with-libxslt`

In both cases, you should `cd /usr/local; ln -s pgsql9.4 pgsql` to make a proper symlink so you can just add `/usr/local/pgsql/bin` to your path. Also don't forget to run `initdb -D /usr/local/pgsql9.4/data` and `createuser rc2`. 

To setup the test database, run `createdb -O rc2 rc2test`, `psql -U rc2 rc2test < rc2.sql` and `psql -U rc2 rc2test < testData.sql`


## TODO

* implement login security measures: stop brute force attacks, limit failed logins by account and/or ip address

## Future optimizations to consider

* Use a LoadingCache for RCUsers fetched in the AuthFilter based on auth token. At a minimum, caching for a few seconds should offer a dramatic reduction in db calls if fetching multiple SessionImages.

* Consider a cache for file access if multiple users in same session. There will be multiple hits in same small timeframe.

* *PermissionChecker.java* would be another place for a cache.
