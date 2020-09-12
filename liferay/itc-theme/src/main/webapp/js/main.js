AUI().ready(
    'liferay-hudcrumbs', 'liferay-navigation-interaction', 'liferay-sign-in-modal',
    function(A) {
        var signIn = A.one('#sign-in');
        if (signIn && signIn.getData('redirect') !== 'true') {
            signIn.plug(Liferay.SignInModal);
        }
    }
);

function inplaceEnterHandler(inplaceWidgetVar, event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        event.stopPropagation();
        PF(inplaceWidgetVar).save();
        return false;
    }
}
