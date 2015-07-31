var rd = {
    findParent: function (elem, type) {
        do {
            elem = elem.parentNode;
        } while (elem.nodeName !== type.toUpperCase());
        return elem;
    },
    findNextSibling: function (elem, pred) {
        do {
            elem = elem.nextSibling;
        } while (!pred(elem));
        return elem;
    },
    doWithChildren: function (elem, action) {
        var e = elem.firstChild;
        while (e) {
            action(e);
            e = e.nextSibling;
        }
    },
    startsWith: function (s, test) {
        return s.substring(0, test.length) === test;
    },
    rest: function (s, test) {
        return rd.startsWith(s, test) ? s.substring(test.length) : false;
    },
    showTrWithId: function (elem, id) {
        var show = null;
        rd.doWithChildren(rd.findParent(elem, 'tbody'), function (tr) {
            if (tr.nodeName === 'TR' && (rd.startsWith(tr.className, 'bodyType') || show != null)) {
                if (tr.className) {
                    show = tr.className.substring(9) === id;
                }
                tr.style.display = show ? 'table-row' : 'none';
            }
        });
    },
    showBodyWithId: function (elem, id) {
        var show = null;
        rd.doWithChildren(elem.parentNode, function (div) {
            if (div.nodeName === 'DIV' && (rd.startsWith(div.className, 'body') || show != null)) {
                if (div.className) {
                    show = div.className.substring(5) === id;
                }
                div.style.display = show ? 'block' : 'none';
            }
        });
    },
    showActionDetail: function (elem) {
        //rd.doWithChildren(elem.parentNode, function (e) {
        //    if (e.className === 'actionDetail') {
        //        e.style.display = 'none';
        //    }
        //});
        var next = rd.findNextSibling(elem, function (elem) {
            return elem.className && rd.startsWith(elem.className, 'actionDetail');
        });
        next.style.display = next.style.display === 'block' ? 'none' : 'block';
    },
    tryOut: function (button, type, baseUri, path) {
        showLoader(true);
        sendRequest(createRequest(baseUri + path), handleResponse);

        function createRequest(uri) {
            var i, elem, rest,
                req = {
                    uri: uri, body: null, query: '', header: {}
                },
                form = rd.findParent(button, 'form');
            for (i = 0; i < form.elements.length; i++) {
                elem = form.elements[i];
                if (elem.value) {
                    if (rest = rd.rest(elem.name, 'query_')) {
                        req.query += encodeURIComponent(rest) + '=' + encodeURIComponent(elem.value) + '&';
                    } else if (rest = rd.rest(elem.name, 'header_')) {
                        req.header[rest] = elem.value;
                    } else if (rest = rd.rest(elem.name, 'uri_')) {
                        req.uri = req.uri.replace('{' + rest + '}', elem.value);
                    } else if (rd.startsWith(elem.name, 'contentType_http')) {
                        req.header['Content-Type'] = elem.value;
                    } else if (rd.startsWith(elem.name, 'body') && rd.findParent(elem, 'tr').style.display === 'table-row') {
                        req.body = elem.value;
                    }
                }
            }
            return req;
        }

        function showLoader(show) {
            var loader = rd.findNextSibling(button, function (e) {
                return e.className === 'loader';
            });
            loader.style.display = show ? 'inline' : 'none';
        }

        function hideLoader() {
            showLoader(false);
        }

        function sendRequest(r, handler) {
            var h, url = interpretUri(r.uri) + '?' + r.query,
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
            }
            try {
                req.send(r.body);
            } catch (e) {
                alert('Could not send request: ' + e);
            }
        }

        function interpretUri(uri) {
            var href = window.location.href,
                hostPos = href.indexOf('://'),
                pathPos = href.indexOf('/', hostPos + 3),
                endPathPos = href.indexOf('/resource/'),
                host = href.substring(hostPos + 3, pathPos),
                path = href.substring(pathPos + 1, endPathPos);
            return uri.replace('$host', host).replace('$path', path);
        }

        function handleResponse(url, req) {
            rd.doWithChildren(
                rd.findNextSibling(button, function (e) {
                    return e.className === 'response';
                }),
                function (elem) {
                    switch (elem.getAttribute && elem.getAttribute('name')) {
                    case 'requestUrl':
                        elem.firstChild.nodeValue = url;
                        break;
                    case 'responseBody':
                        elem.firstChild.nodeValue = req.responseText;
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
    }
};