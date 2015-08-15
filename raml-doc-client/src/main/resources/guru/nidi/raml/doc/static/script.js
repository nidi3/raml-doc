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

    function nextSiblingWithClass(elem, clazz) {
        return findNextSibling(elem, function (e) {
            return hasClass(e, clazz);
        });
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

    function wrap(node, wrapper) {
        var next = node.nextSibling, parent = node.parentNode;
        parent.removeChild(node);
        wrapper.appendChild(node);
        if (next) {
            parent.insertBefore(wrapper, next);
        } else {
            parent.appendChild(wrapper);
        }
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
            if (elem) {
                //doWithChildren(elem.parentNode, function (e) {
                //    if (hasClass(e, 'actionDetail')) {
                //        e.style.display = 'none';
                //    }
                //});
                var next = nextSiblingWithClass(elem, 'actionDetail');
                next.style.display = next.style.display === 'block' ? 'none' : 'block';
            }
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
                    if (r = rest(elem.name, 'q_')) {
                        req.query += encodeURIComponent(r) + '=' + encodeURIComponent(elem.value) + '&';
                    } else if (r = rest(elem.name, 'h_')) {
                        req.header[r] = elem.value;
                    } else if (r = rest(elem.name, 'u_')) {
                        req.uri = req.uri.replace('{' + r + '}', elem.value);
                    } else if (hasClass(elem, 'request') && hasClass(elem, 'contentType')) {
                        req.header['Content-Type'] = elem.value;
                    } else if (startsWith(elem.name, 'body') && findParent(elem, 'tr').style.display === 'table-row') {
                        req.body = elem.value;
                    }
                }
            }

            function showLoader(show) {
                var loader = nextSiblingWithClass(button, 'loader');
                loader.style.display = show ? 'inline' : 'none';
            }

            function hideLoader() {
                showLoader(false);
            }

            function showResponse() {
                var response = nextSiblingWithClass(button, 'response');
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
                var response = nextSiblingWithClass(button, 'response'),
                    i, reqHeaderStr = '';
                for (i in req.requestHeaders) {
                    reqHeaderStr += i + ': ' + req.requestHeaders[i] + '\n';
                }
                response.querySelector('[name=requestUrl]').firstChild.nodeValue = url;
                response.querySelector('[name=requestHeaders]').firstChild.nodeValue = reqHeaderStr;
                var resBody = response.querySelector('[name=responseBody]');
                if (isJson(req.getResponseHeader('Content-Type'))) {
                    resBody.innerHTML = PR.prettyPrintOne(js_beautify(req.responseText));
                    linkify(resBody);
                } else {
                    resBody.firstChild.nodeValue = req.responseText;
                }
                response.querySelector('[name=responseCode]').firstChild.nodeValue = req.status + ' ' + req.statusText;
                response.querySelector('[name=responseHeaders]').firstChild.nodeValue = req.getAllResponseHeaders();
            }

            function linkify(resBody) {
                var i, a, strings = resBody.querySelectorAll('.str');
                for (i = 0; i < strings.length; i++) {
                    var content = strings[i].firstChild.nodeValue;
                    if (content.substring(0, 5) === '"http') {
                        a = document.createElement('a');
                        a.setAttribute('href', content.substring(1, content.length - 1));
                        a.setAttribute('target', '_blank');
                        wrap(strings[i], a);
                        strings[i].className = 'link';
                    }
                }
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
            var data = JSON.parse(sessionStorage.getItem('creds')),
                logout = findParent(script, 'div').querySelector('[name=logout]');
            logout.style.display = data ? 'inline' : 'none';
        },
        showModel: function (span, model) {
            span.parentNode.querySelector('.modelTitle').className = (model ? 'active' : 'inactive') + ' modelTitle';
            span.parentNode.querySelector('.exampleTitle').className = (model ? 'inactive' : 'active') + ' exampleTitle';
            span.parentNode.querySelector('.model').style.display = model ? 'block' : 'none';
            span.parentNode.querySelector('.example').style.display = model ? 'none' : 'block';
        },
        useExample: function (div) {
            var input, code, rawExample = div.querySelector('.rawExample');
            code = (rawExample ? rawExample : div).firstChild.nodeValue;
            findParent(div, 'tr').querySelectorAll('td textarea,td input')[0].value = code;
        },
        applyQuery: function () {
            var inputs, i, q, query = parseQuery();
            for (q in query) {
                if (q.charAt(1) === '_') {
                    inputs = document.querySelectorAll('input[name=' + q + ']');
                    for (i = 0; i < inputs.length; i++) {
                        inputs[i].value = query[q];
                    }
                }
            }
            if (query['method']) {
                var method = query['method'].toUpperCase();
                rd.showActionDetail(document.querySelector('.actionHeader.bg_' + method));
                if (query['run']) {
                    document.querySelector('.try.'+method).click();
                }
            }

            function parseQuery() {
                var i, parts, res = {}, params = document.location.search.substring(1).split('&');
                for (i = 0; i < params.length; i++) {
                    parts = params[i].split('=');
                    res[decodeURIComponent(parts[0])] = decodeURIComponent(parts[1]);
                }
                return res;
            }
        }
    };
}());