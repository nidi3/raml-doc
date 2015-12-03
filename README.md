raml-doc [![Build Status](https://travis-ci.org/nidi3/raml-doc.svg?branch=master)](https://travis-ci.org/nidi3/raml-doc)
===========
Generate an HTML documentation of a RAML file. 
Send test requests to the service directly from within the documentation.

### Usage as standalone tool

The documentation can be generated statically using the command line interface.
 
##### maven 

Download raml-doc-standalone either [manually](http://search.maven.org/remotecontent?filepath=guru/nidi/raml/raml-doc-standalone/0.0.8/raml-doc-standalone-0.0.8.jar) 
or using maven, then execute  
```
java -jar raml-doc-standalone.jar -r <raml-file> -t <output-folder>
```

##### npm

Install raml-doc from npm with `sudo npm install raml-doc -g`

Run it with `raml-doc -r <raml-file> -t <output-folder>`


### Usage as a servlet

The documentation can also be generated from within a web application.
Add this to web.xml

```xml
<servlet>
    <servlet-name>raml-doc</servlet-name>
    <servlet-class>guru.nidi.raml.doc.servlet.RamlDocServlet</servlet-class>
    <init-param>
        <param-name>ramlLocations</param-name>
        <param-value>classpath://api/myRaml.raml</param-value>
    </init-param>
</servlet>

<servlet-mapping>
    <servlet-name>raml-doc</servlet-name>
    <url-pattern>/api/*</url-pattern>
</servlet-mapping>
```

and the documentation is generated at startup and will be available directly from your application.

The available config parameters are the following:

Name | Meaning | Values
-----|---------|-------
ramlLocations | Comma separated list of RAML files. | Protocols like `file://`, `classpath://`, `http://` are supported.
features | Comma separated list of features to enable. | Features are: <br>`online`: The RAML documentation is available through the application, <br>`download`: The documentation provides a download link to the RAML file, <br>`tryout`: The API can be tried out interactively from within the documentation.
baseUri | The URL the test requests should be sent to (overrides the baseUri setting in the RAML file). |
baseUriParameters | Set the parameter values of the baseUri in the RAML file. | The format is `parameter=value,...`. <br>Special values are `$host` and `$path` which are replaced by the actual host and path of the running servlet.
customization | The location where the customized `favicon.ico`, `custom-variables.less`, `custom-style.less` should be loaded from. | For the supported protocols, see ramlLocations parameter. If not given, the first ramlLocation is used.

Another possibility is to subclass RamlDocServlet and override the configuration methods.

### Resulting HTML
The resulting HTML supports the following query parameters in the URL.

Name | Meaning | Value
-----|---------|------
expanded | Which resources in the resource tree on the left should be expanded. | Can be empty (all resources are expanded) or a comma separated list of resource names.
u_* | A URI parameter value to predefine.
q_* | A query parameter value to predefine.
h_* | A header value to predefine.
f_* | A form parameter value to predefine.
method | Which method should be selected. | GET, POST, PUT, DELETE
run | If a request of the selected method should be sent to the server. | none

### Demo
Documentation of a [subset of the GitHub API](http://nidi3.github.io/raml-doc/github/output/index.html).
