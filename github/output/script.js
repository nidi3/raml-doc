var rd = (function () {
    function findParent(elem, type) {
        do {
            elem = elem.parentNode;
        } while (elem.nodeName !== type.toUpperCase());
        return elem;
    }

    function findNextSibling(elem, pred) {
        while (elem && !pred(elem)) {
            elem = elem.nextSibling;
        }
        return elem;
    }

    function doWithChildren(elem, action) {
        var e = elem.firstChild;
        while (e) {
            action(e);
            e = e.nextSibling;
        }
    }

    function startsWith(s, test) {
        return s.substring(0, test.length) === test;
    }

    function rest(s, test) {
        return startsWith(s, test) ? s.substring(test.length) : false;
    }

    function hasClass(node, clazz) {
        return node.className && node.className.indexOf(clazz) >= 0;
    }

    return {
        showTrWithId: function (elem, id) {
            var show = null;
            doWithChildren(findParent(elem, 'tbody'), function (tr) {
                if (tr.nodeName === 'TR' && (hasClass(tr, 'bodyType') || show != null)) {
                    show = hasClass(tr, id);
                    tr.style.display = show ? 'table-row' : 'none';
                }
            });
        },
        showBodyWithId: function (elem, id) {
            var show = null;
            doWithChildren(elem.parentNode, function (div) {
                if (div.nodeName === 'DIV' && (hasClass(div, 'body') || show != null)) {
                    show = hasClass(div, id);
                    div.style.display = show ? 'block' : 'none';
                }
            });
        },
        showActionDetail: function (elem) {
            //doWithChildren(elem.parentNode, function (e) {
            //    if (hasClass(e, 'actionDetail')) {
            //        e.style.display = 'none';
            //    }
            //});
            var next = findNextSibling(elem, function (elem) {
                return hasClass(elem, 'actionDetail');
            });
            next.style.display = next.style.display === 'block' ? 'none' : 'block';
        },
        tryOut: function (button, type, baseUri, path, securitySchemes) {
            showLoader(true);
            showResponse();
            sendRequest(createRequest(baseUri + path, securitySchemes), handleResponse);

            function createRequest(uri, securitySchemes) {
                var i, prop,
                    req = {
                        uri: uri, body: null, query: '', header: {}
                    },
                    form = findParent(button, 'form'),
                    creds = sessionStorage.getItem('creds');
                if (creds) {
                    creds = JSON.parse(creds);
                    if (securitySchemes.indexOf(creds.name + ',') >= 0) {
                        for (prop in creds.data) {
                            setRequestValue(req, {name: prop, value: creds.data[prop]});
                        }
                    }
                }
                for (i = 0; i < form.elements.length; i++) {
                    setRequestValue(req, form.elements[i]);
                }
                return req;
            }

            function setRequestValue(req, elem) {
                var r;
                if (elem.value) {
                    if (r = rest(elem.name, 'query_')) {
                        req.query += encodeURIComponent(r) + '=' + encodeURIComponent(elem.value) + '&';
                    } else if (r = rest(elem.name, 'header_')) {
                        req.header[r] = elem.value;
                    } else if (r = rest(elem.name, 'uri_')) {
                        req.uri = req.uri.replace('{' + r + '}', elem.value);
                    } else if (hasClass(elem, 'request') && hasClass(elem, 'contentType')) {
                        req.header['Content-Type'] = elem.value;
                    } else if (startsWith(elem.name, 'body') && findParent(elem, 'tr').style.display === 'table-row') {
                        req.body = elem.value;
                    }
                }
            }

            function showLoader(show) {
                var loader = findNextSibling(button, function (e) {
                    return hasClass(e, 'loader');
                });
                loader.style.display = show ? 'inline' : 'none';
            }

            function hideLoader() {
                showLoader(false);
            }

            function showResponse() {
                var response = findNextSibling(button, function (e) {
                    return hasClass(e, 'response');
                });
                response.style.display = 'block';
            }

            function sendRequest(r, handler) {
                var h, url = normalize(interpretUri(r.uri) + '?' + r.query),
                    req = new XMLHttpRequest();
                req.onreadystatechange = function () {
                    if (req.readyState === 4) {
                        handler(url, req);
                    }
                };
                req.addEventListener("load", hideLoader, false);
                req.addEventListener("error", hideLoader, false);
                req.addEventListener("abort", hideLoader, false);
                req.open(type, url, true);
                for (h in r.header) {
                    req.setRequestHeader(h, r.header[h]);
                    req.requestHeaders = r.header;
                }
                try {
                    req.send(r.body);
                } catch (e) {
                    alert('Could not send request: ' + e);
                }
            }

            function normalize(path) {
                var len;
                path = path.replace(/\/\.\//g, '/').replace(/[?&]$/, '');
                do {
                    len = path.length;
                    path = path.replace(/\/[^\/]+\/\.\.\//, '/');
                } while (path.length != len);
                return path;
            }

            function interpretUri(uri) {
                var href = window.location.href,
                    hostPos = href.indexOf('://'),
                    pathPos = href.indexOf('/', hostPos + 3),
                    endPathPos = href.indexOf('/resource/'),
                    host = href.substring(hostPos + 3, pathPos),
                    path = href.substring(pathPos + 1, endPathPos);
                return uri.replace('$host', host).replace('$path', path + '/../..');
            }

            function handleResponse(url, req) {
                doWithChildren(
                    findNextSibling(button, function (e) {
                        return hasClass(e, 'response');
                    }),
                    function (elem) {
                        switch (elem.getAttribute && elem.getAttribute('name')) {
                        case 'requestUrl':
                            elem.firstChild.nodeValue = url;
                            break;
                        case 'requestHeaders':
                            var h, s = '';
                            for (h in req.requestHeaders) {
                                s += h + ': ' + req.requestHeaders[h] + '\n';
                            }
                            elem.firstChild.nodeValue = s;
                            break;
                        case 'responseBody':
                            if (isJson(req.getResponseHeader('Content-Type'))) {
                                elem.innerHTML = PR.prettyPrintOne(js_beautify(req.responseText));
                            } else {
                                elem.firstChild.nodeValue = req.responseText;
                            }
                            break;
                        case 'responseCode':
                            elem.firstChild.nodeValue = req.status + ' ' + req.statusText;
                            break;
                        case 'responseHeaders':
                            elem.firstChild.nodeValue = req.getAllResponseHeaders();
                            break;
                        }
                    });
            }

            function isJson(mimeType) {
                mimeType = mimeType || '';
                var pos = mimeType.indexOf(';');
                mimeType = pos >= 0 ? mimeType.substring(0, pos) : mimeType;
                return mimeType === 'application/json' || mimeType.substring(mimeType.length - 5) === '+json';
            }
        },
        login: function (button, name) {
            var i, elem, data = {},
                form = findParent(button, 'form');
            for (i = 0; i < form.elements.length; i++) {
                elem = form.elements[i];
                data[elem.name] = elem.value;
            }
            sessionStorage.setItem('creds', JSON.stringify({name: name, data: data}));
            rd.initSecData(button);
        },
        logout: function (button, global) {
            sessionStorage.removeItem('creds');
            if (global) {
                rd.initGlobalSecData(button);
            } else {
                rd.initSecData(button);
            }
        },
        initSecData: function (script) {
            var i, elem,
                form = findParent(script, 'form'),
                data = sessionStorage.getItem('creds');

            if (data) {
                data = JSON.parse(data).data;
            }
            for (i = 0; i < form.elements.length; i++) {
                elem = form.elements[i];
                if (elem.name === 'login') {
                    elem.style.display = data ? 'none' : 'inline';
                } else if (elem.name === 'logout') {
                    elem.style.display = data ? 'inline' : 'none';
                }
                if (data) {
                    elem.value = data[elem.name];
                }
            }
        },
        initGlobalSecData: function (script) {
            var data = JSON.parse(sessionStorage.getItem('creds'));
            doWithChildren(findParent(script, 'div'), function (elem) {
                if (elem.name === 'logout') {
                    elem.style.display = data ? 'inline' : 'none';
                }
            });
        },
        showModel: function (span, model) {
            span.className = 'active';
            doWithChildren(span.parentNode, function (e) {
                if (e.nodeName === 'SPAN' && e !== span) {
                    e.className = 'inactive';
                }
                if (hasClass(e, 'model')) {
                    e.style.display = model ? 'block' : 'none';
                }
                if (hasClass(e, 'example')) {
                    e.style.display = model ? 'none' : 'block';
                }
            });
        },
        useExample: function (div) {
            var input, code, rawExample = findNextSibling(div.firstChild, function (e) {
                return hasClass(e, 'rawExample');
            });
            code = (rawExample ? rawExample : div).firstChild.nodeValue;
            doWithChildren(findParent(div, 'tr'), function (e) {
                if (e.nodeName === 'TD') {
                    input = findNextSibling(e.firstChild, function (e) {
                        return e.nodeName === 'TEXTAREA' || e.nodeName === 'INPUT';
                    });
                    if (input) {
                        input.value = code;
                    }
                }
            });
        }
    };
}());