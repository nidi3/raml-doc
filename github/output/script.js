var rd = {
    dom: {
        findParent: function (elem, type) {
            do {
                elem = elem.parentNode;
            } while (elem.nodeName !== type.toUpperCase());
            return elem;
        },

        findNextSibling: function (elem, pred) {
            while (elem && !pred(elem)) {
                elem = elem.nextSibling;
            }
            return elem;
        },

        nextSiblingWithClass: function (elem, clazz) {
            return rd.dom.findNextSibling(elem, function (e) {
                return rd.dom.hasClass(e, clazz);
            });
        },

        doWithChildren: function (elem, action) {
            var e = elem.firstChild;
            while (e) {
                action(e);
                e = e.nextSibling;
            }
        },

        hasClass: function (node, clazz) {
            return node.className && node.className.indexOf(clazz) >= 0;
        },

        wrap: function (node, wrapper) {
            var next = node.nextSibling, parent = node.parentNode;
            parent.removeChild(node);
            wrapper.appendChild(node);
            if (next) {
                parent.insertBefore(wrapper, next);
            } else {
                parent.appendChild(wrapper);
            }
        },
        insertAfter: function (node, newNode) {
            var next = node.nextSibling, parent = node.parentNode;
            if (next) {
                parent.insertBefore(newNode, next);
            } else {
                parent.appendChild(newNode);
            }
        }
    },

    string: {
        startsWith: function (s, test) {
            return s.substring(0, test.length) === test;
        },

        rest: function (s, test) {
            return rd.string.startsWith(s, test) ? s.substring(test.length) : false;
        }
    },

    query: {
        parse: function (q) {
            var i, parts, res = {}, params = q.substring(1).split('&');
            if (q.charAt(0) === '?') {
                for (i = 0; i < params.length; i++) {
                    parts = params[i].split('=');
                    res[decodeURIComponent(parts[0])] = parts.length == 1 ? null : decodeURIComponent(parts[1]);
                }
            }
            return res;
        },

        escape: function (value) {
            return '"' + value.replace(/"/g, '\\\\"') + '"';
        },

        attr: function (name, value) {
            return '[' + name + rd.query.escape(value) + ']';
        }
    },

    items: {
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
    },

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
        rd.dom.doWithChildren(rd.dom.findParent(elem, 'tbody'), function (tr) {
            if (tr.nodeName === 'TR') {
                if (rd.dom.hasClass(tr, 'bodyType')) {
                    found = true;
                    show = rd.dom.hasClass(tr, id);
                }
                if (found) {
                    tr.style.display = show ? 'table-row' : 'none';
                }
            }
        });
    },
    showBodyWithId: function (elem, id) {
        var show = null;
        rd.dom.doWithChildren(elem.parentNode, function (div) {
            if (div.nodeName === 'DIV' && (rd.dom.hasClass(div, 'body') || show != null)) {
                show = rd.dom.hasClass(div, id);
                div.style.display = show ? 'block' : 'none';
            }
        });
    },
    showActionDetail: function (elem) {
        if (elem) {
            //rd.dom.doWithChildren(elem.parentNode, function (e) {
            //    if (rd.dom.hasClass(e, 'actionDetail')) {
            //        e.style.display = 'none';
            //    }
            //});
            var next = rd.dom.nextSiblingWithClass(elem, 'actionDetail');
            next.style.display = next.style.display === 'block' ? 'none' : 'block';
        }
    },

    showTab: function (span, name) {
        var i, base = span.parentNode,
            tabs = base.querySelectorAll('.tab'),
            bodies = base.querySelectorAll('.tab-body');

        for (i = 0; i < tabs.length; i++) {
            tabs[i].classList.remove('active', 'inactive');
            tabs[i].classList.add(rd.dom.hasClass(tabs[i], name) ? 'active' : 'inactive');
        }
        for (i = 0; i < bodies.length; i++) {
            bodies[i].style.display = rd.dom.hasClass(bodies[i], name) ? 'block' : 'none';
        }
    },
    useExample: function (div) {
        var input, code, rawExample = div.querySelector('.rawExample');
        code = (rawExample ? rawExample : div).firstChild.nodeValue;
        rd.dom.findParent(div, 'tr').querySelectorAll('td textarea,td input')[0].value = code;
    },
    applyQuery: function () {
        var inputs, i, q, foldables, expanded,
            hash = document.location.hash,
            query = rd.query.parse(document.location.search);
        for (q in query) {
            if (q.charAt(1) === '_') {
                inputs = document.querySelectorAll('input' + rd.query.attr('name=', q));
                for (i = 0; i < inputs.length; i++) {
                    inputs[i].value = query[q];
                }
            }
        }
        var method = (query['method']
            ? query['method']
            : hash.length > 0 ? hash.substring(1) : '').toUpperCase();
        if (method) {
            rd.showActionDetail(document.querySelector('.actionHeader.bg_' + method));
            if (query['run'] !== undefined) {
                document.querySelector('.try.' + method).click();
            }
        } else {
            var actions = document.querySelectorAll('.actionHeader');
            if (actions.length === 1) {
                rd.showActionDetail(actions[0]);
            }
        }
        if ((expanded = query['expanded']) !== undefined) {
            var ex = (expanded || '').split(',');
            foldables = document.querySelectorAll('.foldable.collapsed');
            for (i = 0; i < foldables.length; i++) {
                rd.switchFoldable(foldables[i], expanded === null || ex.indexOf(foldables[i].getAttribute('name')) !== -1);
            }
        } else {
            var tree = rd.items.load('tree') || {};
            foldables = document.querySelectorAll('.foldable');
            for (i = 0; i < foldables.length; i++) {
                expanded = tree[foldables[i].getAttribute('name')];
                rd.switchFoldable(foldables[i], expanded === undefined ? false : expanded);
            }
        }
        var loc = document.location.pathname,
            start = loc.substring(loc.length - 11) === '/index.html'
                ? loc.lastIndexOf('/', loc.length - 12) + 1
                : loc.indexOf('resource') + 8,
            link = document.querySelector('a' + rd.query.attr('href$=', loc.substring(start)));
        if (link) {
            link.classList.add('active');
        }
    },
    switchFoldable: function (span, expanded) {
        var cl = span.classList,
            name = span.getAttribute('name'),
            tree = rd.items.load('tree') || {};
        if (rd.dom.hasClass(span, 'collapsed') && expanded !== false) {
            tree[name] = true;
            cl.remove('collapsed');
            rd.dom.nextSiblingWithClass(span.parentNode, 'subLink').style.display = 'block';
        } else if (expanded !== true) {
            tree[name] = false;
            cl.add('collapsed');
            rd.dom.nextSiblingWithClass(span.parentNode, 'subLink').style.display = 'none';
        }
        rd.items.store('tree', tree);
    }
};