<#assign show_header_search = getterUtil.getBoolean(themeDisplay.getThemeSetting("show-header-search")) />

<#assign currentURL = themeDisplay.getURLCurrent() />
<#assign friendlyURL = themeDisplay.getLayout().getFriendlyURL()/>
<#assign pageId = themeDisplay.getPlid() />

<#assign isMainPage =
    getterUtil.getBoolean(theme_settings["is-mainpage"]) == true
    && httpUtil.getParameterMap(queryStr)?size == 0 />

<#assign 
    contactPhone = getterUtil.getString(themeDisplay.getThemeSetting("contact-phone"))
    contactEmail = getterUtil.getString(themeDisplay.getThemeSetting("contact-email"))
    contactAddr  = getterUtil.getString(themeDisplay.getThemeSetting("contact-address")) 
    yandexMetric = getterUtil.getString(themeDisplay.getThemeSetting("yandex-metrika"))
    yandexWebMaster = getterUtil.getString(themeDisplay.getThemeSetting("yandex-verification"))
/>

<#assign 
    contactsAddress=contactAddr!"г. Новосибирск, ул. Зыряновская, 53"
    googleLink="https://www.google.com/maps/search/${contactsAddress}"
    contactsPhone=contactPhone!"8 (383) 328-1-328"
    contactsEmail=contactEmail!"nsk@itc-electronics.com"
    callCenterPhone=contactPhone!"8 (383) 328-1-328"
/>
<#-- 
    "г. Новосибирск, ул. Зыряновская, 53"
    "8 (383) 328-1-328"
    "nsk@itc-electronics.com"
 -->


<#assign languagePortletPreferences = freeMarkerPortletPreferences.getPreferences({
    "displayStyle": "ddmTemplate_LANGUAGE-SHORT-TEXT-FTL",
    "portletSetupPortletDecoratorId": "barebone"
}) />

<#assign categoriesPortletPreferences = freeMarkerPortletPreferences.getPreferences(
    "portletSetupPortletDecoratorId", "barebone"
) />

<#assign stocksPortletPreferences = freeMarkerPortletPreferences.getPreferences(
    "portletSetupPortletDecoratorId", "barebone"
) />

<#assign navPortletPreferences = freeMarkerPortletPreferences.getPreferences(
    "portletSetupPortletDecoratorId", "barebone"
) />

<#assign currencySelectorPreferences = freeMarkerPortletPreferences.getPreferences(
    "portletSetupPortletDecoratorId", "barebone"
) />

<#assign warehouseSelectorPreferences = freeMarkerPortletPreferences.getPreferences(
    "portletSetupPortletDecoratorId", "barebone"
) />
