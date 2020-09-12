package com.tuneit.itc.commons.util;

import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoColumnLocalServiceUtil;
import com.liferay.expando.kernel.service.ExpandoTableLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;

public class ExpandoColumnInitializer {
    private static final Log log = LogFactoryUtil.getLog(ExpandoColumnInitializer.class);

    @SneakyThrows
    public static void initializeExpandoTable(Class<?> modelClass, List<ExpandoColumnDefinition> columns) {
        log.info("Initialize liferay expando for class " + modelClass.getCanonicalName());
        long classNameId = ClassNameLocalServiceUtil.getClassNameId(modelClass);
        log.info("Liferay class name id = " + classNameId);
        List<Long> companyIds = CompanyLocalServiceUtil.getCompanies()
            .stream()
            .map(Company::getCompanyId)
            .collect(Collectors.toList());
        log.info("Company ids: " + companyIds.toString());
        for (Long companyId : companyIds) {
            Role guestRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.GUEST);
            Role userRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.USER);
            Map<Long, String[]> roleIdsToActions = new HashMap<>();
            roleIdsToActions.put(guestRole.getRoleId(), new String[] {ActionKeys.VIEW});
            roleIdsToActions.put(userRole.getRoleId(), new String[] {ActionKeys.VIEW, ActionKeys.UPDATE});
            log.info("Initialize for company " + companyId);
            ExpandoTable table;
            try {
                table = ExpandoTableLocalServiceUtil.getDefaultTable(companyId, classNameId);
            } catch (PortalException e) {
                log.warn("Can not find default expando table for liferay class [" + classNameId + " "
                    + modelClass.getCanonicalName() + "], try to create new one", e);
                table = ExpandoTableLocalServiceUtil.addDefaultTable(companyId, classNameId);
                log.info("New expando table successfully created. Table id = " + table.getTableId() +
                    ", table class name = " + table.getClassName() + ", table class name id = "
                    + table.getClassNameId());
            }

            for (ExpandoColumnDefinition column : columns) {
                log.info("Process column " + column.toString());
                ExpandoColumn currentCol = ExpandoColumnLocalServiceUtil.getColumn(table.getTableId(),
                    column.getName());
                if (currentCol == null) {
                    log.info("Column with name " + column.getName() + " not found in expando table for class "
                        + table.getClassName());
                    currentCol = ExpandoColumnLocalServiceUtil.addColumn(table.getTableId(), column.getName(),
                        column.getType());
                }
                ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId,
                    "com.liferay.expando.kernel.model.ExpandoColumn", ResourceConstants.SCOPE_COMPANY,
                    String.valueOf(companyId), roleIdsToActions);
                log.info("Successfully initialize expando. Expando table id = " + table.getTableId() +
                    ", country code column id = " + currentCol.getColumnId());
            }


        }

    }

    private ExpandoColumnInitializer() {
        throw new UnsupportedOperationException("This is an utility class!");
    }

}
