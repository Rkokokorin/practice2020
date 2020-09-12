function isDescendant(parent, element) {
    let parentNode = element.parentNode;
    while (parentNode != null) {
        if (parentNode === parent) {
            return true;
        }
        parentNode = parentNode.parentNode;
    }
    return false;
}

function hideOnClickOutside(hideElSelector, elementSelectors) {
    document.addEventListener('click', function (ev) {
        let hideElement = true;
        for (let elSelector of elementSelectors) {
            let selected = $(elSelector);
            let element = selected[0];
            if (element != null) {
                if (ev.target === element || isDescendant(element, ev.target)) {
                    hideElement = false;
                    break;
                }
            }
        }
        if (hideElement) {
            $(hideElSelector).hide();
        }

    })
}
