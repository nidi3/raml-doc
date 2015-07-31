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
        <param-name>ramlLocation</param-name>
        <param-value>classpath://api/myRaml.raml</param-value>
    </init-param>
    <init-param>
        <param-name>tryOut</param-name>
        <param-value>true</param-value>
    </init-param>
    <init-param>
        <param-name>baseUriParameters</param-name>
        <param-value>host=$host,path=$path/..</param-value>
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
ramlLocation | Where the RAML is located, protocols like `file://`, `classpath://`, `http://` are supported.
tryOut | Enable sending requests to the server to interactively test the API.
baseUri | The URL the test requests should be sent to (overrides the baseUri setting in the RAML file).
baseUriParameters | Set the parameter values of the baseUri in the RAML file. The format is `parameter=value,...`. Special values are `$host` and `$path` which are replaced by the actual host and path of the running servlet.

Another possibility is to subclass RamlDocServlet and override the configuration methods.
