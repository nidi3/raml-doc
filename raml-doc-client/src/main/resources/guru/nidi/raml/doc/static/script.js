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

    function parseQuery(q) {
        var i, parts, res = {}, params = q.substring(1).split('&');
        if (q.charAt(0) === '?') {
            for (i = 0; i < params.length; i++) {
                parts = params[i].split('=');
                res[decodeURIComponent(parts[0])] = parts.length == 1 ? null : decodeURIComponent(parts[1]);
            }
        }
        return res;
    }

    function queryEscape(value) {
        return '"' + value.replace(/"/g, '\\\\"') + '"';
    }

    function queryAttr(name, value) {
        return '[' + name + '=' + queryEscape(value) + ']';
    }

    var items = {
        load: function (name) {
            var val = sessionStorage.getItem(name);
            return val ? JSON.parse(val) : null;
        },

        store: function (name, value) {
            sessionStorage.setItem(name, JSON.stringify(value));
        },
        remove: function (name) {
            sessionStorage.removeItem(name);
        }
    };

    return {
        currentScript: function () {
            return document.currentScript || (function () {
                    var scripts = document.getElementsByTagName('script');
                    return scripts[scripts.length - 1];
                }());
        },
        setBaseUri: function (uri) {
            var href = window.location.href,
                hostPos = href.indexOf('://'),
                pathPos = href.indexOf('/', hostPos + 3),
                endPathPos = href.indexOf('/resource/'),
                host = href.substring(hostPos + 3, pathPos),
                path = href.substring(pathPos + 1, endPathPos);
            rd.baseUri = normalize(uri.replace('$host', host).replace('$path', path + '/../..'));

            function normalize(path) {
                var len;
                path = path.replace(/\/\.\//g, '/');
                do {
                    len = path.length;
                    path = path.replace(/\/[^\/]+\/\.\.\//, '/');
                } while (path.length != len);
                return path;
            }
        },

        showTrWithId: function (elem, id) {
            var show, found = false;
            doWithChildren(findParent(elem, 'tbody'), function (tr) {
                if (tr.nodeName === 'TR') {
                    if (hasClass(tr, 'bodyType')) {
                        found = true;
                        show = hasClass(tr, id);
                    }
                    if (found) {
                        tr.style.display = show ? 'table-row' : 'none';
                    }
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
        tryOut: function (button, type, path, securitySchemes) {
            showLoader(true);
            showResponse();
            sendRequest(createRequest(rd.baseUri + path, securitySchemes), handleResponse);

            function createRequest(uri, securitySchemes) {
                var i, prop,
                    req = {
                        type: 'text', uri: uri, body: null, query: '', header: {}
                    },
                    form = findParent(button, 'form'),
                    creds = items.load('creds');
                if (creds) {
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
                var h, url = (r.uri + '?' + r.query).replace(/[?&]$/, ''),
                    req = new XMLHttpRequest();
                req.onreadystatechange = function () {
                    if (req.readyState === 4) {
                        handler(r, url, req);
                    }
                };
                req.addEventListener("load", hideLoader, false);
                req.addEventListener("error", hideLoader, false);
                req.addEventListener("abort", hideLoader, false);
                req.open(type, url, true);
                req.responseType = r.type;
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

            function handleResponse(r, url, req) {
                var response = nextSiblingWithClass(button, 'response'),
                    mimeType = req.getResponseHeader('Content-Type'),
                    isImage = startsWith(simpleMimeType(mimeType), 'image/'),
                    reqHeaderStr = '', i;

                if (isImage && r.type !== 'arraybuffer') {
                    r.type = 'arraybuffer';
                    sendRequest(r, handleResponse);
                    return;
                }

                for (i in req.requestHeaders) {
                    reqHeaderStr += i + ': ' + req.requestHeaders[i] + '\n';
                }
                response.querySelector('[name=requestUrl]').firstChild.nodeValue = url;
                response.querySelector('[name=requestHeaders]').firstChild.nodeValue = reqHeaderStr;
                var resBody = response.querySelector('[name=responseBody]'),
                    resHtml = response.querySelector('[name=responseHtml]'),
                    resImage = response.querySelector('[name=responseImage]');

                if (isImage) {
                    bodyTabs('preview', false, true);
                    bodyContent(false, false, true);
                    resImage.src = 'data:' + mimeType + ';base64,' + btoa(String.fromCharCode.apply(null, new Uint8Array(req.response)));
                } else {
                    bodyTabs('response', true, false);
                    bodyContent(true, false, false);
                    var resText = req.responseText;
                    if (resText.length > 100000) {
                        resText = resText.substring(0, 100000) + '...';
                    }
                    if (isJson(mimeType)) {
                        resBody.innerHTML = PR.prettyPrintOne(js_beautify(resText));
                        linkify(resBody);
                    } else {
                        resBody.firstChild.nodeValue = resText;
                        if (simpleMimeType(mimeType) === 'text/html') {
                            bodyTabs('preview', true, true);
                            bodyContent(true, true, false);
                            resHtml.style.height = (Math.max(resBody.offsetWidth, resHtml.offsetWidth) * .75) + 'px';
                            resHtml.contentDocument.write(resText);
                            resHtml.contentDocument.close();
                        }
                    }
                }
                response.querySelector('[name=responseCode]').firstChild.nodeValue = req.status + ' ' + req.statusText;
                response.querySelector('[name=responseHeaders]').firstChild.nodeValue = req.getAllResponseHeaders();

                function bodyTabs(select, showResponse, showPreview) {
                    var resContent = response.querySelector('.responseContent'),
                        responseTab = response.querySelector('.tab.response'),
                        previewTab = response.querySelector('.tab.preview');

                    rd.showTab(resContent, select);
                    responseTab.style.display = showResponse ? 'inline' : 'none';
                    previewTab.style.display = showPreview ? 'inline' : 'none';
                }

                function bodyContent(text, html, image) {
                    resBody.style.display = text ? 'block' : 'none';
                    resHtml.style.display = html ? 'block' : 'none';
                    resImage.style.display = image ? 'block' : 'none';
                }
            }


            function linkify(resBody) {
                var i, p, a, url, known, strings = resBody.querySelectorAll('.str');
                for (i = 0; i < strings.length; i++) {
                    var content = strings[i].firstChild.nodeValue;
                    if (content.substring(0, 5) === '"http') {
                        url = content.substring(1, content.length - 1);
                        known = knownLink(url);
                        if (known) {
                            url = document.location.protocol + '//' + document.location.host + document.location.pathname + '/' + rd.relPath + known.url + '.html?';
                            for (p in known.query) {
                                url += 'q_' + encodeURIComponent(p) + '=' + encodeURIComponent(known.query[p]) + '&';
                            }
                            for (p in known.vars) {
                                url += 'u_' + encodeURIComponent(p) + '=' + encodeURIComponent(known.vars[p]) + '&';
                            }
                            url += 'method=get&run';
                        }
                        a = document.createElement('a');
                        a.setAttribute('href', url);
                        a.setAttribute('target', '_blank');
                        wrap(strings[i], a);
                        strings[i].className = 'link';
                    }
                }
            }

            function knownLink(url) {
                var i, j, patternParts, res, match, parts, qPos;
                if (url.substring(0, rd.baseUri.length) === rd.baseUri) {
                    url = url.substring(rd.baseUri.length);
                    qPos = url.indexOf('?');
                    parts = (qPos < 0 ? url : url.substring(0, qPos)).split('/');
                    for (i = 0; i < rd.urls.length; i++) {
                        patternParts = rd.urls[i].split('/');
                        if (patternParts.length === parts.length) {
                            res = {url: rd.urls[i], vars: {}, query: parseQuery(url.substring(qPos))};
                            match = true;
                            for (j = 0; j < patternParts.length; j++) {
                                if (patternParts[j].charAt(0) === '{') {
                                    res.vars[patternParts[j].substring(1, patternParts[j].length - 1)] = parts[j];
                                } else if (patternParts[j] !== parts[j]) {
                                    match = false;
                                }
                            }
                            if (match) {
                                return res;
                            }
                        }
                    }
                }
            }

            function isJson(mimeType) {
                mimeType = simpleMimeType(mimeType);
                return mimeType === 'application/json' || mimeType.substring(mimeType.length - 5) === '+json';
            }

            function simpleMimeType(mimeType) {
                mimeType = mimeType || '';
                var pos = mimeType.indexOf(';');
                return pos >= 0 ? mimeType.substring(0, pos) : mimeType;
            }
        },
        login: function (button, name) {
            var i, elem, data = {},
                form = findParent(button, 'form');
            for (i = 0; i < form.elements.length; i++) {
                elem = form.elements[i];
                data[elem.name] = elem.value;
            }
            items.store('creds', {name: name, data: data});
            rd.initSecData(button);
        },
        logout: function (button, global) {
            items.remove('creds');
            if (global) {
                rd.initGlobalSecData(button);
            } else {
                rd.initSecData(button);
            }
        },
        initSecData: function (script) {
            var i, elem,
                form = findParent(script, 'form'),
                data = items.load('creds');

            if (data) {
                data = data.data;
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
            var data = items.load('creds'),
                logout = findParent(script, 'div').querySelector('[name=logout]');
            logout.style.display = data ? 'inline' : 'none';
        },
        showTab: function (span, name) {
            var i, base = span.parentNode,
                tabs = base.querySelectorAll('.tab'),
                bodies = base.querySelectorAll('.tab-body');

            for (i = 0; i < tabs.length; i++) {
                tabs[i].classList.remove('active', 'inactive');
                tabs[i].classList.add(hasClass(tabs[i], name) ? 'active' : 'inactive');
            }
            for (i = 0; i < bodies.length; i++) {
                bodies[i].style.display = hasClass(tabs[i], name) ? 'block' : 'none';
            }
        },
        useExample: function (div) {
            var input, code, rawExample = div.querySelector('.rawExample');
            code = (rawExample ? rawExample : div).firstChild.nodeValue;
            findParent(div, 'tr').querySelectorAll('td textarea,td input')[0].value = code;
        },
        applyQuery: function () {
            var inputs, i, q, foldables, expanded, query = parseQuery(document.location.search);
            for (q in query) {
                if (q.charAt(1) === '_') {
                    inputs = document.querySelectorAll('input' + queryAttr('name', q));
                    for (i = 0; i < inputs.length; i++) {
                        inputs[i].value = query[q];
                    }
                }
            }
            if (query['method']) {
                var method = query['method'].toUpperCase();
                rd.showActionDetail(document.querySelector('.actionHeader.bg_' + method));
                if (query['run'] !== undefined) {
                    document.querySelector('.try.' + method).click();
                }
            }
            if ((expanded = query['expanded']) !== undefined) {
                var ex = (expanded || '').split(',');
                foldables = document.querySelectorAll('.foldable.collapsed');
                for (i = 0; i < foldables.length; i++) {
                    rd.switchFoldable(foldables[i], expanded === null || ex.indexOf(foldables[i].getAttribute('name')) !== -1);
                }
            } else {
                var tree = items.load('tree') || {};
                foldables = document.querySelectorAll('.foldable');
                for (i = 0; i < foldables.length; i++) {
                    expanded = tree[foldables[i].getAttribute('name')];
                    rd.switchFoldable(foldables[i], expanded === undefined ? false : expanded);
                }
            }
            var start, end,
                loc = document.location.pathname;
            if (loc.substring(loc.length - 11) === '/index.html') {
                start = loc.lastIndexOf('/', loc.length - 12) + 1;
                end = loc.length - 11;
            } else {
                start = loc.indexOf('resource') + 8;
                end = loc.length - 5;
            }
            var path = decodeURIComponent(loc.substring(start, end)),
                link = document.querySelector('a' + queryAttr('title', path));
            if (link) {
                link.classList.add('active');
            }
        },
        switchFoldable: function (span, expanded) {
            var cl = span.classList,
                name = span.getAttribute('name'),
                tree = items.load('tree') || {};
            if (hasClass(span, 'collapsed') && expanded !== false) {
                tree[name] = true;
                cl.remove('collapsed');
                nextSiblingWithClass(span.parentNode, 'subLink').style.display = 'block';
            } else if (expanded !== true) {
                tree[name] = false;
                cl.add('collapsed');
                nextSiblingWithClass(span.parentNode, 'subLink').style.display = 'none';
            }
            items.store('tree', tree);
        }
    };
}());