function pushCatalogueHistory(namespace) {
    "use strict";
    var paramDict = {};
    var plPageParamName = document.getElementById(namespace+':plPageParamName').value;
    var plRowsParamName = document.getElementById(namespace+':plRowsParamName').value;

    paramDict[plPageParamName] = document.getElementById(namespace+':plPage').value;
    paramDict[plRowsParamName] = document.getElementById(namespace+':plRows').value;

    for (var key in paramDict) {
        if (paramDict[key] === "") {
            delete paramDict[key];
        }
    }

    var query;
    var search = window.location.search;
    if (search === "" && !isEmpty(paramDict)) {
        query = "?";
        query = appendParams(query, paramDict);
    } else {
        query = search;
        query = removeUrlParam(query, plPageParamName);
        query = removeUrlParam(query, plRowsParamName);
        if (query === "" && !isEmpty(paramDict)) {
            query = "?";
            query = appendParams(query, paramDict);
        } else {
            query = query + "&";
            query = appendParams(query, paramDict);
        }
    }
    history.pushState(null, document.title, window.location.origin + window.location.pathname + query);
}


function isEmpty(dict) {
    return Object.keys(dict).length === 0;
}

function removeUrlParam(url, paramName) {
    var pattern = new RegExp('\\b(' + paramName + '=).*?(&|#|$)');
    if (url.search(pattern) >= 0) {
        url = url.replace(pattern, '');
    }
    url = url.replace(/[?#&]$/, '');
    return url;
}

function appendParams(query, paramsDict) {
    for (var key in paramsDict) {
        query = query + key + "=" + paramsDict[key] + "&";
    }
    return query.slice(0, -1)
}