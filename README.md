raml-doc [![Build Status](https://travis-ci.org/nidi3/raml-doc.svg?branch=master)](https://travis-ci.org/nidi3/raml-doc)
===========
Generate a HTML documentation of a RAML and allow to interactively test it.

### Usage as standalone tool

```
java -jar raml-doc-standalone.jar -r file://<raml-file>
```

will generate a new subfolder containing the HTML documentation of the RAML file.

### Usage as a servlet

Add this to web.xml

```
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

Name | Value
-----|-------
ramlLocations | Where the RAML is located, protocols like `file://`, `classpath://`, `http://` are supported.
features | Comma separated list of features to enable. Features are: `online`: The RAML documentation is available through the application, `download`: The documentation provides a download link to the RAML file, `tryout`: The API can be tried out interactively from within the documentation.
baseUri | The URL the test requests should be sent to (overrides the baseUri setting in the RAML file).
baseUriParameters | Set the parameter values of the baseUri in the RAML file. The format is `parameter=value,...`. Special values are `$host` and `$path` which are replaced by the actual host and path of the running servlet.

Another possibility is to subclass RamlDocServlet and override the configuration methods.
