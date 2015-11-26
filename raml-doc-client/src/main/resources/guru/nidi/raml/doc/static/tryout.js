rd.addHeader = function (e) {
    var tr = rd.dom.findParent(e, 'tr'),
        next = document.createElement('tr');
    next.innerHTML = '<td>' +
        ' <select onchange="rd.setHeaderName(this)">'+
        '  <option>Accept</option><option>Accept-Charset</option><option>Accept-Language</option>'+
        '  <option>Cache-Control</option><option>Pragma</option><option>User-Agent</option></select>' +
        ' <span class="remove clickable" onclick="rd.removeHeader(this)"/>' +
        '</td>' +
        '<td class="additional-header"><input></td>';
    rd.dom.insertAfter(tr, next);
    rd.setHeaderName(next.querySelector('select'));
};

rd.setHeaderName = function (e) {
    var input = rd.dom.nextSiblingWithClass(e.parentNode, 'additional-header').querySelector('input');
    input.name = 'h_' + e.value;
};

rd.removeHeader = function (e) {
    var tr = rd.dom.findParent(e, 'tr');
    tr.parentNode.removeChild(tr);
};

rd.showModifiedTry = function (e) {
    var tryModified = rd.dom.nextSiblingWithClass(e.parentNode.parentNode.firstChild, 'try-modified');
    tryModified.style.display = 'inline';
};

rd.tryOutModified = function (button, type, path, securitySchemes) {
    rd.tryOut(button, type, path, securitySchemes, true);
};

rd.tryOut = function (button, type, path, securitySchemes, useModifiedUrl) {
    var response = rd.dom.nextSiblingWithClass(button, 'response'),
        tryModified = rd.dom.nextSiblingWithClass(button, 'try-modified');

    if (!useModifiedUrl) {
        tryModified.style.display = 'none';
    }

    showLoader(true);
    showResponse();
    sendRequest(createRequest(rd.baseUri + path, securitySchemes, useModifiedUrl), handleResponse);

    function createRequest(uri, securitySchemes, useModifiedUrl) {
        var i, prop, url,
            req = {
                type: 'text', uri: uri, body: null, query: '', header: {}
            },
            form = rd.dom.findParent(button, 'form'),
            creds = rd.items.load('creds');
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
        if (useModifiedUrl) {
            url = form.requestUrl.value;
            i = url.indexOf('?');
            if (i < 0) {
                req.uri = url;
                req.query = '';
            } else {
                req.uri = url.substring(0, i);
                req.query = url.substring(i + 1);
            }
        }
        return req;
    }

    function setRequestValue(req, elem) {
        var r;
        if (elem.value) {
            if (r = rd.string.rest(elem.name, 'q_')) {
                req.query += encodeURIComponent(r) + '=' + encodeURIComponent(elem.value) + '&';
            } else if (r = rd.string.rest(elem.name, 'h_')) {
                req.header[r] = elem.value;
            } else if (r = rd.string.rest(elem.name, 'u_')) {
                req.uri = req.uri.replace('{' + r + '}', elem.value);
            } else if (rd.dom.hasClass(elem, 'request') && rd.dom.hasClass(elem, 'contentType')) {
                req.header['Content-Type'] = elem.value;
            } else if (rd.string.startsWith(elem.name, 'body') && rd.dom.findParent(elem, 'tr').style.display === 'table-row') {
                req.body = elem.value;
            }
        }
    }

    function showLoader(show) {
        var loader = rd.dom.nextSiblingWithClass(button, 'loader');
        loader.style.display = show ? 'inline' : 'none';
    }

    function hideLoader() {
        showLoader(false);
    }

    function showResponse() {
        var response = rd.dom.nextSiblingWithClass(button, 'response');
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
        }
        showRequestUrl(url);
        showRequestHeaders(r.header);
        showResponseCode('');
        showResponseHeaders('');
        showResponseImage();
        showResponseText();

        try {
            req.send(r.body);
        } catch (e) {
            alert('Could not send request: ' + e);
        }
    }

    function handleResponse(r, url, req) {
        var mimeType = req.getResponseHeader('Content-Type'),
            isImage = rd.string.startsWith(simpleMimeType(mimeType), 'image/');

        if (isImage && r.type !== 'arraybuffer') {
            r.type = 'arraybuffer';
            sendRequest(r, handleResponse);
            return;
        }

        showResponseCode(req.status + ' ' + req.statusText);
        showResponseHeaders(req.getAllResponseHeaders());
        if (isImage) {
            bodyTabs('preview', false, true);
            showResponseImage(mimeType, req.response);
        } else {
            bodyTabs('response', true, simpleMimeType(mimeType) === 'text/html');
            showResponseText(mimeType, req.responseText);
        }

        function bodyTabs(select, showResponse, showPreview) {
            var resContent = response.querySelector('.responseContent'),
                responseTab = response.querySelector('.tab.response'),
                previewTab = response.querySelector('.tab.preview');

            rd.showTab(resContent, select);
            responseTab.style.display = showResponse ? 'inline' : 'none';
            previewTab.style.display = showPreview ? 'inline' : 'none';
        }
    }

    function showRequestUrl(url) {
        response.querySelector('[name=requestUrl]').value = url;
    }

    function showRequestHeaders(headers) {
        var h, str = '';
        for (h in headers) {
            str += '<div><span class="header-name">' + h + '</span><span>' + headers[h] + '</span></div>';
        }
        response.querySelector('[name=requestHeaders]').innerHTML = str;
    }

    function showResponseCode(code) {
        response.querySelector('[name=responseCode]').firstChild.nodeValue = code;
    }

    function showResponseHeaders(headers) {
        var i, pos, str = '', parts = headers.split('\n');
        for (i = 0; i < parts.length; i++) {
            pos = parts[i].indexOf(':');
            if (pos > 0) {
                str += '<div><span class="header-name">' + parts[i].substring(0, pos) + '</span><span>' + parts[i].substring(pos + 1) + '</span></div>';
            }
        }
        response.querySelector('[name=responseHeaders]').innerHTML = str;
    }

    function showResponseImage(mimeType, data) {
        var resImage = response.querySelector('[name=responseImage]');
        if (data) {
            resImage.src = 'data:' + mimeType + ';base64,' + btoa(String.fromCharCode.apply(null, new Uint8Array(data)));
            resImage.style.display = 'block';
        } else {
            resImage.style.display = 'none';
        }
    }

    function showResponseText(mimeType, text) {
        var resBody = response.querySelector('[name=responseBody]'),
            resHtml = response.querySelector('[name=responseHtml]');

        if (text) {
            if (text.length > 100000) {
                text = text.substring(0, 100000) + '...';
            }
            resBody.style.display = 'block';
            if (isJson(mimeType)) {
                resBody.innerHTML = PR.prettyPrintOne(js_beautify(text));
                linkify(resBody);
            } else {
                resBody.firstChild.nodeValue = text;
                if (simpleMimeType(mimeType) === 'text/html') {
                    resHtml.style.display = 'block';
                    resHtml.style.height = (Math.max(resBody.offsetWidth, resHtml.offsetWidth) * .75) + 'px';
                    resHtml.contentDocument.write(text);
                    resHtml.contentDocument.close();
                }
            }
        } else {
            resBody.style.display = 'none';
            resHtml.style.display = 'none';
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
                rd.dom.wrap(strings[i], a);
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
                    res = {url: rd.urls[i], vars: {}, query: rd.query.parse(url.substring(qPos))};
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
};

rd.login = function (button, name) {
    var i, elem, data = {},
        form = rd.dom.findParent(button, 'form');
    for (i = 0; i < form.elements.length; i++) {
        elem = form.elements[i];
        data[elem.name] = elem.value;
    }
    rd.items.store('creds', {name: name, data: data});
    rd.initSecData(button);
};

rd.logout = function (button, global) {
    rd.items.remove('creds');
    if (global) {
        rd.initGlobalSecData(button);
    } else {
        rd.initSecData(button);
    }
};

rd.initSecData = function (script) {
    var i, elem,
        form = rd.dom.findParent(script, 'form'),
        data = rd.items.load('creds');

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
};

rd.initGlobalSecData = function (script) {
    var data = rd.items.load('creds'),
        logout = rd.dom.findParent(script, 'div').querySelector('[name=logout]');
    logout.style.display = data ? 'inline' : 'none';
};