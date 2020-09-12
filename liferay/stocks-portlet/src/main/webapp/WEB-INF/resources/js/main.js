function initCarousel($) {
    var specialSlider = $(".owl-carousel.stocks-container");
    if (specialSlider) {
        specialSlider.owlCarousel({
            margin: 0,
            loop: true,
            dots: false,
            navText: ['', ''],
            navClass: ['owl-prev fa fa-angle-left', 'owl-next fa fa-angle-right'],
            animateOut: 'fadeOut',
            responsiveClass: true,
            responsiveBaseElement: ".owl-carousel.stocks-container",
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
    $(document).ready(function ($) {
        if ($().owlCarousel) {
            initCarousel($);
        }
    });
    function spaInitializer() {
        initCarousel($);
        Liferay.detach('endNavigate', spaInitializer);
    }
    Liferay.on('endNavigate', spaInitializer);
})();

