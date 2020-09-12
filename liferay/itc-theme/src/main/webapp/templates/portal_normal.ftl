<!DOCTYPE html>

<#include init />

<html class="${root_css_class}" dir="<@liferay.language key="lang.dir" />" lang="${w3c_language_id}">

<head>
    <title>${the_title} - ${company_name}</title>
    <meta content="initial-scale=1.0, width=device-width" name="viewport" />
    <@liferay_util["include"] page=top_head_include />
    <!— Yandex.Metrika counter —>
    <script type="text/javascript" >
        (function(m,e,t,r,i,k,a){m[i]=m[i]||function(){(m[i].a=m[i].a||[]).push(arguments)};
            m[i].l=1*new Date();k=e.createElement(t),a=e.getElementsByTagName(t)[0],k.async=1,k.src=r,a.parentNode.insertBefore(k,a)})
        (window, document, "script", "https://mc.yandex.ru/metrika/tag.js", "ym");

        ym(${yandexMetric}, "init", {
            clickmap:true,
            trackLinks:true,
            accurateTrackBounce:true,
            webvisor:true
        });
    </script>
    <noscript><div><img src="https://mc.yandex.ru/watch/57388288" style="position:absolute; left:-9999px;" alt="" /></div></noscript>
    <!— /Yandex.Metrika counter —>
    <#if (friendlyURL == "/home" || isMainPage) && !(currentURL?startsWith("/c/")) && currentURL != "/c">
        <meta name="yandex-verification" content="${yandexWebMaster}" />
    </#if>
</head>

<body class="${css_class} itc-theme">
    <#assign maximized = ((request.getParameter("p_p_state")!"defaultValue") == "maximized")/>

    <@liferay_ui["quick-access"] contentId="#main-content" />
    <@liferay_util["include"] page=body_top_include />
    <@liferay.control_menu />

    <div class="pt-0" id="wrapper">

        <header>

            <#-- TOP BAR -->

            <div id="topbar">
                <div class="container">
                    <div class="row align-items-center justify-content-end">

                        <#-- WAREHOUSE SELECTOR -->
                        <div id="topbar-warehouse-selector" class="col-auto d-flex">
                            <div class="location d-none d-sm-block">
                                <span><i class="fas fa-map-marker-alt"></i> Склад отгрузки:</span>
                            </div>
                            <@liferay_portlet["runtime"]
                            portletName="warehouseselectorportlet_WAR_warehouseselectorportlet"
                            defaultPreferences="${warehouseSelectorPreferences}"
                            />
                        </div>

                        <div id="topbar-placeholder" class="col px-0 d-block">
                        </div>
                        
                        <#-- LANGUAGE SELECTOR -->
                        <div id="topbar-language-selector" class="col-auto d-none d-md-block">
                            <span>
                                <#if language == "ru_RU" >
                                    <span class="selected">RU</span>
                                <#else>
                                    <a href="/c/portal/update_language?p_l_id=${pageId}&languageId=ru_RU&redirect=${friendlyURL}">RU</a>
                                </#if>
                            </span> | 
                            <span>
                                <#if language == "en_US" >
                                    <span class="selected">EN</span>
                                <#else>
                                    <a href="/c/portal/update_language?p_l_id=${pageId}&languageId=en_US&redirect=${friendlyURL}">EN</a>
                                </#if>
                            </span>
                        </div>

                        <#-- CURRENCY SELECTOR -->
                        <div id="topbar-currency-selector" class="col-auto d-block">
                            <@liferay_portlet["runtime"]
                                portletName="currencyselectorportlet_WAR_currencyselectorportlet"
                                defaultPreferences="${currencySelectorPreferences}"
                            />
                        </div>

                        <#-- LOGIN & REGISTRATION -->
                        <div id="topbar-login-bar" class="col-auto pr-0 d-none d-md-block">
                            <#if !is_signed_in>
                                <a id="sign-in" href="${sign_in_url}" rel="nofollow"
                                   data-redirect="false">
                                    ${sign_in_text}
                                </a>
                                / 
                                <a href="/registration">Регистрация</a>
                            <#else>
                                <i class="fas fa-user"></i>
                                <span class="font-weight-bold mr-1">
                                    ${themeDisplay.user.fullName}
                                </span>
                                <#if sign_out_url??> <a id="sign-out" href="${sign_out_url}" rel="nofollow">${sign_out_text}</a></#if>
                            </#if>
                        </div>

                    </div>
                </div>
            </div>

            <#-- HEADER (LOGO & SEARCH) -->

            <div id="header">
                <div class="container">
                    <div class="row align-items-center">
                        <#-- COMPANY LOGO -->
                        <div id="company-logo" class="col-auto pl-2 pr-3">
                            <a class="${logo_css_class}" href="${site_default_url}" title="<@liferay.language_format arguments='' key='go-to-x' />">
                                <img src="${site_logo}" alt="${logo_description}" height="5rem"/>
                            </a>
                        </div>

                        <#-- COMPANY DESCRIPTION -->
                        <div id="company-desc" class="col-auto pl-0 d-none d-lg-block">
                          Комплексные поставки<br/>
                          электрооборудования<br/>
                          и микроэлектроники
                        </div>

                        <#-- QUICK SEARCH -->
                        <div id="quick-search" class="col">
                            <@liferay_portlet["runtime"]
                                  portletName="quicksearchportlet_WAR_quicksearchportlet"
                                  defaultPreferences="${categoriesPortletPreferences}"
                              />
                        </div>

                        <#-- GO TO BOM -->
                        <div id="go-to-bom" class="col-auto col-md-auto pl-0 d-none d-xl-block">
                            <a href="/bom-search" class="go-to-bom">
                                Быстрый поиск <i class="fas fa-long-arrow-alt-right"></i>
                            </a>
                        </div>

                        <#-- CONTACT PHONE -->
                        <div id="contact-phone" class="col-md-auto pr-0 d-none d-md-block">
                            <a href="tel:${callCenterPhone}">
                                <i class="fas fa-phone-alt"></i> ${callCenterPhone}
                            </a>
                        </div>
                        <#-- NAVBAR TOGGLER (for mobile verision) -->
                        <div class="col-auto d-xl-none navbar-expand-xl p-0">
                            <#if has_navigation && is_setup_complete>
                                <button aria-controls="navigationCollapse"
                                        aria-expanded="false"
                                        aria-label="Toggle navigation"
                                        class="navbar-toggler navbar-toggler-right px-0 py-0"
                                        data-target="#navigationCollapse"
                                        data-toggle="collapse"
                                        type="button">
                                    <span class="navbar-toggler-icon fa fa-bars"></span>
                                </button>
                            </#if>
                        </div>
                    </div>
                </div>
            </div>

            <#-- NAVIGATION (MAIN MENU, PROFILE AND SHOPPING CART) -->

            <div id="navigation">
                <div class="container">
                    <div class="row align-items-center">

                        <#-- MAIN MENU -->
                        <div id="main-menu" class="col-12 col-xl-auto order-5 order-xl-0">
                            <#include "${full_templates_path}/navigation.ftl" />
                        </div>

                        <div id="navigation-placeholder" class="col px-0 d-block">
                        </div>

                        <#-- USER PROFILE LINK -->
                        <div id="user-profile" class="col-auto px-0">
                            <#if is_signed_in>
                                <a href="/account" title="Личный кабинет" class="nav-quick-link">
                                    <img src="${images_folder}/profile.svg"/>
                                </a>
                            </#if>
                        </div>
                        <div id="comparison" class="col-auto px-2">
                            <#if is_signed_in>
                                <a href="/compare" title="Сравнение товаров" class="nav-quick-link">
                                    <img src="${images_folder}/compare.svg"/>
                                </a>
                            </#if>
                        </div>

                        <div id="favorite" class="col-auto pl-0">
                            <#if is_signed_in>
                                <a href="/favorite" title="Избранное" class="nav-quick-link">
                                    <img src="${images_folder}/favorite.svg"/>
                                </a>
                            </#if>
                        </div>

                        <#-- SHOPPING CART STATUS -->
                        <div id="shopping-cart" class="col-auto px-0 d-none d-xl-block">
                            <@liferay_portlet["runtime"]
                                portletName="shoppingcartstatusportlet_WAR_shoppingcartstatusportlet"
                                defaultPreferences="${categoriesPortletPreferences}"
                            />
                        </div>

                        <#if friendlyURL != "/home" || isMainPage>
                            <div class="catalog-nav drop-down" style="display: none">
                                <@liferay_portlet["runtime"]
                                    portletName="categoriesmenuportlet_WAR_categoriesmenuportlet"
                                    defaultPreferences="${categoriesPortletPreferences}"
                                />
                            </div>

                            <script type="text/javascript">
                                $("#navigation .navbar-site li:first-child").on("mouseover", function () {
                                    $(".catalog-nav.drop-down").show();
                                });
                                $("#navigation .catalog-nav.drop-down").on("mouseover", function () {
                                    $(".catalog-nav.drop-down").show();
                                });
                                $("#navigation .catalog-nav.drop-down").on("mouseout", function () {
                                    $(".catalog-nav.drop-down").hide();
                                });
                            </script>
                        </#if>
                    </div>
                </div>
            </div>
        </header>

        <main id="content">

            <#-- CATEGORIES, BANNER & STOCKS -->

            <#if (friendlyURL == "/home" || isMainPage) && !maximized && !(currentURL?startsWith("/c/")) && currentURL != "/c">
                <section class="container">
                    <div class="row">
                        <div id="categories-menu" class="col-md-6 col-lg-5 col-xl-4 px-0 d-none d-lg-block">
                            <@liferay_portlet["runtime"]
                                portletName="categoriesmenuportlet_WAR_categoriesmenuportlet"
                                defaultPreferences="${categoriesPortletPreferences}"
                            />
                        </div>

                        <div id="banner" class="col-md-6 col-lg-7 col-xl-8 px-0 pt-2">
                            <@liferay_portlet["runtime"]
                                instanceId="40103"
                                defaultPreferences="${categoriesPortletPreferences}"
                                portletName="com_liferay_journal_content_web_portlet_JournalContentPortlet"
                            />

                            <@liferay_portlet["runtime"]
                                portletName="stocksportlet_WAR_stocksportlet"
                                defaultPreferences="${stocksPortletPreferences}"
                            />
                        </div>
                    </div>
                </section>
            </#if>

            <#-- PAGE CONTENT -->

            <section class="">
                <h1 class="sr-only">${the_title}</h1>

                <#if selectable>
                    <@liferay_util["include"] page=content_include />
                <#else>
                    ${portletDisplay.recycle()}

                    ${portletDisplay.setTitle(the_title)}

                    <@liferay_theme["wrap-portlet"] page="portlet.ftl">
                        <@liferay_util["include"] page=content_include />
                    </@>
                </#if>
            </section>

            <div class="shader" style="display: none"></div>
        </main>

        <#-- FOOTER -->
        <footer id="footer" class="pt-5">
            <div class="container">
                <div class="row">
                    <div class="footer-column col-12 col-lg-auto d-md-none d-lg-block align-items-center align-items-lg-start mb-4 mb-sm-0">
                        <img src="${images_folder}/itc-logo-white.svg"/>
                    </div>

                    <div class="footer-column col-12 col-md col-lg-auto col-xl mb-5 mb-lg-0 align-items-center align-items-md-start text-center text-md-left">
                        <h4>Адрес</h4>
                        <a href="${googleLink}"><i class="fas fa-map-marker-alt"></i>${contactsAddress}</a>
                        <a href="tel:${contactsPhone}"><i class="fas fa-phone-alt"></i>${contactsPhone}</a>
                        <a href="mailto:${contactsEmail}"><i class="far fa-envelope"></i>${contactsEmail}</a>
                        <a href="/contacts" style="margin-top: 1rem;">Все филиалы</a>
                    </div>

                    <div class="footer-column footer-menu col-xl-2 col-lg-3 col-md-3 col-6 align-items-center align-items-md-start text-center text-md-left">
                        <h4>Клиенту</h4>
                        <a href="/catalogue">Каталог</a>
                        <a href="/manufacturers">Производители</a>
                        <a href="/delivery">Доставка и оплата</a>
                        <a href="/how-to-order">Как заказать</a>
                    </div>

                    <div class="footer-column footer-menu col-xl-2 col-lg-auto col-md-3 col-6 align-items-center align-items-md-start text-center text-md-left">
                        <h4>О компании</h4>
                        <a href="/about-us">О нас</a>
                        <a href="/certificates">Сертификаты</a>
                        <a href="/services">Сервисы</a>
                        <a href="/contract-offer">Договор оферты</a>
                    </div>

                    <div class="contact-us footer-column col-xl-auto col-lg-12 mt-5 mt-md-0 align-items-center align-items-xl-start">
                        <h4>Связаться с нами</h4>

                        <a class="contact-phone" href="tel:${callCenterPhone}"><i class="fas fa-phone-alt"></i>${callCenterPhone}</a>

                        <div class="social-icons">
                            <a class="fab fa-facebook-square" href="https://www.facebook.com/ITC-Electronics-106366594085674"></a>
                            <a class="fab fa-instagram" href="https://www.instagram.com/itc_electronics/"></a>
                            <a class="fab fa-whatsapp-square" href="https://wa.me/%2B79628243033"></a>
                            <a class="fab fa-telegram-plane" href="https://telegram.im/@ITCElectronics"></a>
                            <a class="fab fa-vk" href="https://vk.com/itcelectronics"></a>
                        </div>
                        <a href="/feedback" class="report-feedback">Нашли ошибку?</a>
                    </div>
                </div>
            </div>

            <div id="footer-bottom">
                <div class="container">
                    <div class="row">
                        <div class="col-lg-3 col-md-4 col-12 text-center text-md-left">
                            <a href="/terms-of-use">Пользовательское соглашение</a>
                        </div>
                        <div class="col-lg-3 col-md-4 col-12 my-2 my-md-0 text-center text-md-left">
                            <a href="/privacy-policy">Политика конфиденциальности</a>
                        </div>
                        <div class="col-lg-6 col-md-4 col-12 text-center text-md-left">
                            © ITC ELECTRONICS, 2020
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    </div>
    <@liferay.js file_name="${javascript_folder}/owl.carousel.min.js"/>
    <@liferay.js file_name="${javascript_folder}/jquery.isotope.min.js"/>
    <@liferay.js file_name="${javascript_folder}/jquery.prettyPhoto.js"/>
    <@liferay.js file_name="${javascript_folder}/jquery.flexnav.min.js"/>
    <@liferay.js file_name="${javascript_folder}/jquery.hoverIntent.min.js"/>
    <@liferay.js file_name="${javascript_folder}/jquery.magnific-popup.js"/>
    <@liferay.js file_name="${javascript_folder}/photoswipe.min.js"/>
    <@liferay.js file_name="${javascript_folder}/photoswipe-ui-default.min.js"/>

    <@liferay.js file_name="${javascript_folder}/singlepage.js"/>

    <@liferay_util["include"] page=body_bottom_include />

    <@liferay_util["include"] page=bottom_include />

    </body>

</html>
