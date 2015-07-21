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
    showTrWithId: function (elem, id) {
        var show = null;
        rd.doWithChildren(rd.findParent(elem, 'tbody'), function (tr) {
            if (tr.nodeName === 'TR' && (tr.className.substring(0, 8) === 'bodyType' || show != null)) {
                if (tr.className) {
                    show = tr.className.substring(9) === id;
                }
                tr.style.display = show ? 'table-row' : 'none';
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
            return elem.className && elem.className.substring(0,12) === 'actionDetail';
        });
        next.style.display = next.style.display === 'block' ? 'none' : 'block';
    },
    tryOut:function(button){
        var form = rd.findParent(button, 'form');
    }
};