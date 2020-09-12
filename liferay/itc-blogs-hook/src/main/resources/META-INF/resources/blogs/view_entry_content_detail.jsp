<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<style>
    .news-header {
        padding: 2rem;
        background-color: rgb(241, 247, 252);
    }
</style>

<%@ include file="/blogs/init.jsp" %>

<%
    SearchContainer searchContainer = (SearchContainer)request.getAttribute("view_entry_content.jsp-searchContainer");

    BlogsEntry entry = (BlogsEntry)request.getAttribute("view_entry_content.jsp-entry");
%>

<c:choose>
    <c:when test="<%= BlogsEntryPermission.contains(permissionChecker, entry, ActionKeys.VIEW) && (entry.isVisible() || (entry.getUserId() == user.getUserId()) || BlogsEntryPermission.contains(permissionChecker, entry, ActionKeys.UPDATE)) %>">
        <portlet:renderURL var="viewEntryURL">
            <portlet:param name="mvcRenderCommandName" value="/blogs/view_entry" />
            <portlet:param name="redirect" value="<%= currentURL %>" />

            <c:choose>
                <c:when test="<%= Validator.isNotNull(entry.getUrlTitle()) %>">
                    <portlet:param name="urlTitle" value="<%= entry.getUrlTitle() %>" />
                </c:when>
                <c:otherwise>
                    <portlet:param name="entryId" value="<%= String.valueOf(entry.getEntryId()) %>" />
                </c:otherwise>
            </c:choose>
        </portlet:renderURL>

        <div class="container widget-mode-detail-header">
            <liferay-asset:asset-categories-available
                    className="<%= BlogsEntry.class.getName() %>"
                    classPK="<%= entry.getEntryId() %>"
            >
                <div class="row">
                    <div class="categories col-md-12  widget-metadata">
                        <liferay-asset:asset-categories-summary
                                className="<%= BlogsEntry.class.getName() %>"
                                classPK="<%= entry.getEntryId() %>"
                                displayStyle="simple-category"
                                portletURL="<%= renderResponse.createRenderURL() %>"
                        />
                    </div>
                </div>
            </liferay-asset:asset-categories-available>

            <div class="row">
                <div class="col-md-12 ">
                    <div class="autofit-row">
                        <div class="autofit-col autofit-col-expand">
                            <h3 class="title"><%= HtmlUtil.escape(BlogsEntryUtil.getDisplayTitle(resourceBundle, entry)) %></h3>

                            <%
                                String subtitle = entry.getSubtitle();
                            %>

                            <c:if test="<%= Validator.isNotNull(subtitle) %>">
                                <h4 class="sub-title"><%= HtmlUtil.escape(subtitle) %></h4>
                            </c:if>
                        </div>
                        <%
                            User entryUser = UserLocalServiceUtil.fetchUser(entry.getUserId());

                            String entryUserURL = StringPool.BLANK;

                            if ((entryUser != null) && !entryUser.isDefaultUser()) {
                                entryUserURL = entryUser.getDisplayURL(themeDisplay);
                            }
                        %>
                        <div class="autofit-col visible-interaction">
                            <div class="dropdown dropdown-action">
                                <c:if test="<%= BlogsEntryPermission.contains(permissionChecker, entry, ActionKeys.UPDATE) %>">
                                    <portlet:renderURL var="editEntryURL" windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                        <portlet:param name="mvcRenderCommandName" value="/blogs/edit_entry" />
                                        <portlet:param name="redirect" value="<%= currentURL %>" />
                                        <portlet:param name="entryId" value="<%= String.valueOf(entry.getEntryId()) %>" />
                                    </portlet:renderURL>

                                    <a href="<%= editEntryURL.toString() %>">
                                        <span class="hide-accessible"><liferay-ui:message key="edit-entry" /></span>

                                        <clay:icon
                                                symbol="pencil"
                                        />
                                    </a>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <div class="autofit-row widget-metadata news-header">

                        <div class="autofit-col autofit-col-expand">
                            <div class="autofit-row">
                                <div class="autofit-col autofit-col-expand">
                                    <div class="text-secondary">
                                        <span class="hide-accessible"><liferay-ui:message key="published-date" /></span><liferay-ui:message arguments="<%= LanguageUtil.getTimeDescription(request, System.currentTimeMillis() - entry.getStatusDate().getTime(), true) %>" key="x-ago" translateArguments="<%= false %>" />

                                        <c:if test="<%= blogsPortletInstanceConfiguration.enableReadingTime() %>">
                                            - <liferay-reading-time:reading-time displayStyle="descriptive" model="<%= entry %>" />
                                        </c:if>

                                        <c:if test="<%= blogsPortletInstanceConfiguration.enableViewCount() %>">

                                            <%
                                                AssetEntry assetEntry = _getAssetEntry(request, entry);
                                            %>

                                            - <liferay-ui:message arguments="<%= assetEntry.getViewCount() %>" key='<%= (assetEntry.getViewCount() == 1) ? "x-view" : "x-views" %>' />
                                        </c:if>
                                    </div>
                                    <div>
                                        <%= StringUtil.shorten(HtmlUtil.stripHtml(entry.getDescription()), 400) %>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <%
            String coverImageURL = entry.getCoverImageURL(themeDisplay);
        %>

<%--        <c:if test="<%= Validator.isNotNull(coverImageURL) %>">
            <img src="<%= coverImageURL %>" alt="cover image"/>
        </c:if>
--%>
        <!-- text resume -->

        <div class="container widget-mode-detail-header" id="<portlet:namespace /><%= entry.getEntryId() %>">
            <c:if test="<%= Validator.isNotNull(coverImageURL) %>">

                <%
                    String coverImageCaption = entry.getCoverImageCaption();
                %>

                <c:if test="<%= Validator.isNotNull(coverImageCaption) %>">
                    <div class="row">
                        <div class="col-md-9 ">
                            <div class="cover-image-caption">
                                <small><%= entry.getCoverImageCaption() %></small>
                            </div>
                        </div>
                    </div>
                </c:if>
            </c:if>

            <div class="row">
                <div class="col-md-9  widget-mode-detail-text">
                    <%= entry.getContent() %>
                </div>
                <div class="col-md-3 entry-navigation">
                    <h2><strong><liferay-ui:message key="more-news-entries" /></strong></h2>
                    <div class="widget-mode-card">
                        <%
                            BlogsEntry[] prevAndNext = BlogsEntryServiceUtil.getEntriesPrevAndNext(entry.getEntryId());

                            BlogsEntry previousEntry = prevAndNext[0];
                            BlogsEntry nextEntry = prevAndNext[2];
                        %>

                        <c:if test="<%= (previousEntry != null) || (nextEntry != null) %>">
                            <%
                                request.setAttribute("view_entry_related.jsp-blogs_entry", previousEntry);
                            %>

                            <liferay-util:include page="/blogs/view_entry_related.jsp" servletContext="<%= application %>" />

                            <%
                                request.setAttribute("view_entry_related.jsp-blogs_entry", nextEntry);
                            %>

                            <liferay-util:include page="/blogs/view_entry_related.jsp" servletContext="<%= application %>" />
                        </c:if>

                    </div>
                </div>
            </div>

            <liferay-expando:custom-attributes-available
                    className="<%= BlogsEntry.class.getName() %>"
            >
                <div class="row">
                    <div class="col-md-9  widget-mode-detail">
                        <liferay-expando:custom-attribute-list
                                className="<%= BlogsEntry.class.getName() %>"
                                classPK="<%= entry.getEntryId() %>"
                                editable="<%= false %>"
                                label="<%= true %>"
                        />
                    </div>
                </div>
            </liferay-expando:custom-attributes-available>

            <liferay-asset:asset-tags-available
                    className="<%= BlogsEntry.class.getName() %>"
                    classPK="<%= entry.getEntryId() %>"
            >
                <div class="row">
                    <div class="col-md-9  widget-mode-detail">
                        <div class="entry-tags">
                            <liferay-asset:asset-tags-summary
                                    className="<%= BlogsEntry.class.getName() %>"
                                    classPK="<%= entry.getEntryId() %>"
                                    portletURL="<%= renderResponse.createRenderURL() %>"
                            />
                        </div>
                    </div>
                </div>
            </liferay-asset:asset-tags-available>
        </div>

        <div class="container">
            <div class="row">
                <div class="col-md-9  widget-mode-detail">

                    <%
                        request.setAttribute("entry_toolbar.jsp-entry", entry);
                    %>

                    <liferay-util:include page="/blogs/entry_toolbar.jsp" servletContext="<%= application %>">
                        <liferay-util:param name="showFlags" value="<%= Boolean.TRUE.toString() %>" />
                    </liferay-util:include>
                </div>
            </div>

            <c:if test="<%= blogsPortletInstanceConfiguration.enableRelatedAssets() %>">
                <div class="row">
                    <div class="col-md-9  widget-mode-detail">

                        <%
                            AssetEntry assetEntry = _getAssetEntry(request, entry);
                        %>

                        <div class="entry-links">
                            <liferay-asset:asset-links
                                    assetEntryId="<%= (assetEntry != null) ? assetEntry.getEntryId() : 0 %>"
                                    className="<%= BlogsEntry.class.getName() %>"
                                    classPK="<%= entry.getEntryId() %>"
                            />
                        </div>
                    </div>
                </div>
            </c:if>
        </div>
    </c:when>
    <c:otherwise>

        <%
            if (searchContainer != null) {
                searchContainer.setTotal(searchContainer.getTotal() - 1);
            }
        %>

    </c:otherwise>
</c:choose>

<%!
    private AssetEntry _getAssetEntry(HttpServletRequest request, BlogsEntry entry) throws PortalException, SystemException {
        AssetEntry assetEntry = (AssetEntry)request.getAttribute("view_entry_content.jsp-assetEntry");

        if (assetEntry == null) {
            assetEntry = AssetEntryLocalServiceUtil.getEntry(BlogsEntry.class.getName(), entry.getEntryId());

            request.setAttribute("view_entry_content.jsp-assetEntry", assetEntry);
        }

        return assetEntry;
    }
%>