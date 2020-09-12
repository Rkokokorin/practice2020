jQuery().ready(function ($) {
    'use strict';
    if ($().owlCarousel) {
        
        var mainSlider = $(".owl-carousel.main-slider");
        if (mainSlider) {
            mainSlider.owlCarousel({
                items: 1,
                margin: 30,
                loop: true,
                nav: true,
                dots: false,
                navText: ['', ''],
                navClass: ['owl-prev fa fa-angle-left', 'owl-next fa fa-angle-right'],
                animateOut: 'fadeOut',
                autoplay: true,
                autoplayTimeout: 5000
            });
        }

        var manufacturersSlider = $(".owl-carousel.manufacturers-container");
        if (manufacturersSlider) {
            manufacturersSlider.owlCarousel({
                items: 6,
                margin: 0,
                loop: true,
                nav: true,
                dots: false,
                navText: ['', ''],
                navClass: ['owl-prev fa fa-angle-left', 'owl-next fa fa-angle-right'],
                animateOut: 'fadeOut',
                responsiveClass: true,
                responsiveBaseElement: ".owl-carousel.manufacturers-container",
                responsive: {
                    0: {
                        items: 1,
                        nav: true
                    },
                    440: {
                        items: 2,
                        nav: true
                    },
                    660: {
                        items: 3,
                        nav: true
                    },
                    880: {
                        items: 4,
                        nav: true
                    },
                    1200: {
                        items: 5,
                        nav: true
                    },
                    1370: {
                        items: 6,
                        nav: true
                    }
                }
            });
        }
    
        var manufacturerCerts = $(".manufacturer-certs");
        if (manufacturerCerts) {
            manufacturerCerts.owlCarousel({
                items: 1,
                loop: true,
                navText: ['', ''],
                navClass: ['owl-prev fa fa-angle-left', 'owl-next fa fa-angle-right']
            });
        }
    }

    $(".manufacturer-cert").magnificPopup({
        type: 'image',
        closeOnContentClick: true,
        image: {
            verticalFit: true
        }
    });

    $(".itc-cert").magnificPopup({
        type: 'image',
        closeOnContentClick: true,
        image: {
            verticalFit: true,
            titleSrc: 'data-mfp-title'
        },
        gallery: {
            enabled: true
        }
    });    
});



