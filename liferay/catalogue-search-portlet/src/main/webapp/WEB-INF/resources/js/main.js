function pushCatalogueHistory(namespace) {
  "use strict";
  var paramDict = {};
  var catIdParamName = document.getElementById(namespace+':catIdParamName').value;
  var subcatIdParamName = document.getElementById(namespace+':subcatIdParamName').value;
  var manIdParamName = document.getElementById(namespace+':manIdParamName').value;
  var searchQueryParamName = document.getElementById(namespace+':searchQueryParamName').value;
  var partNumberSearchParamName = document.getElementById(namespace+':pnsParamName').value;
  var plPageParamName = document.getElementById(namespace+':plPageParamName').value;
  var plRowsParamName = document.getElementById(namespace+':plRowsParamName').value;
  var warehouseCodeParamName = document.getElementById(namespace+':warehouseCodeParamName').value;

  paramDict[catIdParamName] = document.getElementById(namespace+':catId').value;
  paramDict[subcatIdParamName] = document.getElementById(namespace+':subcatId').value;
  paramDict[manIdParamName] = document.getElementById(namespace+':manId').value;
  paramDict[searchQueryParamName] = document.getElementById(namespace+':searchQuery').value;
  paramDict[partNumberSearchParamName] = document.getElementById(namespace+':pns').value;
  paramDict[plPageParamName] = document.getElementById(namespace+':plPage').value;
  paramDict[plRowsParamName] = document.getElementById(namespace+':plRows').value;
  paramDict[warehouseCodeParamName] = document.getElementById(namespace+':warehouseCode').value;

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
    query = removeUrlParam(query, catIdParamName);
    query = removeUrlParam(query, subcatIdParamName);
    query = removeUrlParam(query, manIdParamName);
    query = removeUrlParam(query, searchQueryParamName);
    query = removeUrlParam(query, partNumberSearchParamName);
    query = removeUrlParam(query, plPageParamName);
    query = removeUrlParam(query, plRowsParamName);
    query = removeUrlParam(query, warehouseCodeParamName);
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

function pushPageParams(namespace) {
  "use strict";
  var paramDict = {};
  var warehouseCodeParamName = document.getElementById(namespace+':warehouseCodeParamName').value;

  paramDict[warehouseCodeParamName] = document.getElementById(namespace+':warehouseCode').value;

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
    query = removeUrlParam(query, warehouseCodeParamName);
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

function initReplacementsCarousel($) {
  let slider = $(".owl-carousel.replacements-container");
  if (slider) {
    slider.owlCarousel({
      margin: 0,
      loop: false,
      rewind: true,
      dots: false,
      navText: ['', ''],
      navClass: ['owl-prev fa fa-angle-left', 'owl-next fa fa-angle-right'],
      animateOut: 'fadeOut',
      responsiveClass: true,
      responsiveBaseElement: ".owl-carousel.replacements-container",
      responsive:{
        0:{
          items: 1,
          nav: true
        },
        440:{
          items: 2,
          nav: true
        },
        660:{
          items: 3,
          nav: true
        },
        880:{
          items: 4,
          nav: true
        },
        1200:{
          items: 5,
          nav: true
        }
      }
    });
  }
}

(function () {
  $(document).ready(function () {
    if ($().owlCarousel) {
      initReplacementsCarousel($);
    }
  });
})();

