var rd = {
    findParent: function (elem, type) {
        do {
            elem = elem.parentNode;
        } while (elem.nodeName !== type.toUpperCase());
        return elem;
    },
    showTrWithId: function (elem, id) {
        var show = null, tr = rd.findParent(elem, 'tbody').firstChild;
        while (tr) {
            if (tr.nodeName === 'TR' && (tr.className.substring(0, 8) === 'bodyType' || show != null)) {
                if (tr.className) {
                    show = tr.className.substring(9) === id;
                }
                tr.style.display = show ? 'table-row' : 'none';
            }
            tr = tr.nextSibling;
        }
    }
};