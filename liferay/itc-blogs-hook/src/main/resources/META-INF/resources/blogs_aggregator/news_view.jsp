<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    final com.liferay.portal.kernel.security.permission.PermissionChecker finalChecker = permissionChecker;
    List<BlogsEntry> newsViewResults = viewResults.stream()
        .filter(e ->  {
            try {
                return BlogsEntryPermission.contains(finalChecker, e, ActionKeys.VIEW);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        })
        .collect(Collectors.toList());
    BlogsEntry firstEntry = newsViewResults.get(0);
    newsViewResults = newsViewResults
            .stream()
            .skip(1)
            .limit(5)
            .collect(Collectors.toList());
    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
%>

<div class="head-news-item col-md-5">
    <%
        PortletURL showErrorMessageURL = renderResponse.createRenderURL();

        showErrorMessageURL.setParameter("mvcRenderCommandName", "/blogs_aggregator/view");
        showErrorMessageURL.setParameter("blogsPortletFound", Boolean.FALSE.toString());

        StringBundler sb = new StringBundler(5);

        sb.append(themeDisplay.getPathMain());
        sb.append("/blogs/find_entry?p_l_id=");
        sb.append(themeDisplay.getPlid());
        sb.append("&noSuchEntryRedirect=");
        sb.append(URLCodec.encodeURL(showErrorMessageURL.toString()));
        sb.append("&entryId=");
        sb.append(firstEntry.getEntryId());

        StringBundler allNewsSb = new StringBundler();
        allNewsSb.append(themeDisplay.getPathMain());
        allNewsSb.append("/blogs/view?p_l_id=");
        allNewsSb.append(themeDisplay.getPlid());

        String allNewsURL = allNewsSb.toString();

        String viewEntryURL = sb.toString();
        String summary = firstEntry.getDescription();

        if (Validator.isNull(summary)) {
            summary = firstEntry.getContent();
        }
        String smallImageURL = HtmlUtil.escape(firstEntry.getSmallImageURL(themeDisplay));
    %>

    <div class="news-image">
        <img src="<%=smallImageURL%>" alt="<%=HtmlUtil.escape(firstEntry.getTitle())%>"/>
    </div>
    <div class="head-news-content">
        <div class="news-date"><%= df.format(firstEntry.getDisplayDate()) %></div>

        <h4 class="news-title"><a href="<%=viewEntryURL%>"><%= HtmlUtil.escape(firstEntry.getTitle()) %></a></h4>

        <div class="news-desc"><%= StringUtil.shorten(HtmlUtil.stripHtml(summary), 200) %></div>
    </div>

</div>
<div class="col-md-7">
    <div class="remaining-items">
    <% for (BlogsEntry entry : newsViewResults) {
        showErrorMessageURL = renderResponse.createRenderURL();

        showErrorMessageURL.setParameter("mvcRenderCommandName", "/blogs_aggregator/view");
        showErrorMessageURL.setParameter("blogsPortletFound", Boolean.FALSE.toString());

        sb = new StringBundler(5);

        sb.append(themeDisplay.getPathMain());
        sb.append("/blogs/find_entry?p_l_id=");
        sb.append(themeDisplay.getPlid());
        sb.append("&noSuchEntryRedirect=");
        sb.append(URLCodec.encodeURL(showErrorMessageURL.toString()));
        sb.append("&entryId=");
        sb.append(entry.getEntryId());

        viewEntryURL = sb.toString();
    %>
        <div class="news-item">
            <div class="news-date"><%= df.format(entry.getDisplayDate()) %></div>

            <h4 class="news-title"><a href="<%=viewEntryURL%>"><%= HtmlUtil.escape(entry.getTitle()) %></a></h4>
        </div>
    <%}%>
    <div class="news-item">
        <a href="/news"><liferay-ui:message key="all-news-link" /> <i class="fas fa-long-arrow-alt-right"></i></a>
    </div>
    </div>
</div>

