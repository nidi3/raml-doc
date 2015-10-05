raml-doc [![Build Status](https://travis-ci.org/nidi3/raml-doc.svg?branch=master)](https://travis-ci.org/nidi3/raml-doc)
===========
Generate a HTML documentation of a RAML and allow to interactively test it.

### Demo
Documentation of a [subset of the GitHub API](http://nidi3.github.io/raml-doc/github/output/index.html).

### Usage as standalone tool

```
java -jar raml-doc-standalone.jar -r <raml-file>
```

will generate a new subfolder containing the HTML documentation of the RAML file.

### Usage as a servlet

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

and the RAML documentation is available directly from your application.

The available config parameters are the following:

Name | Meaning | Values
-----|---------|-------
ramlLocations | Comma separated list of RAML files. | Protocols like `file://`, `classpath://`, `http://` are supported.
features | Comma separated list of features to enable. | Features are: <br>`online`: The RAML documentation is available through the application, <br>`download`: The documentation provides a download link to the RAML file, <br>`tryout`: The API can be tried out interactively from within the documentation.
baseUri | The URL the test requests should be sent to (overrides the baseUri setting in the RAML file). |
baseUriParameters | Set the parameter values of the baseUri in the RAML file. | The format is `parameter=value,...`. <br>Special values are `$host` and `$path` which are replaced by the actual host and path of the running servlet.
customization | The location where the customized `favicon.ico` should be loaded from. | For the supported protocols, see ramlLocations parameter.

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
