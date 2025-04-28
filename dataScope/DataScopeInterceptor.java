package com.hzb.erp.api.base.dataScope;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.hzb.erp.annotation.DataScoped;
import com.hzb.erp.common.entity.DataPermission;
import com.hzb.erp.common.entity.StaffOrginfo;
import com.hzb.erp.common.enums.DataScopeTypeEnum;
import com.hzb.erp.api.base.dataScope.DataPermissionService;
import com.hzb.erp.api.base.dataScope.DataScopeEntityEnum;
import com.hzb.erp.security.Util.SecurityUtils;
import com.hzb.erp.api.base.service.StaffAuthService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> 数据权限的mybatisplus实现 </p>
 *
 * @author Ryan 541720500@qq.com
 */
@Slf4j
public class DataScopeInterceptor implements InnerInterceptor {
    @SneakyThrows
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql) {

        if (!SqlCommandType.SELECT.equals(ms.getSqlCommandType())) {
            return;
        }

        /*
         * 获取登录者
         * */
        if (StaffAuthService.getCurrentUserId() == null) {
            return;
        }
        // 获取登录信息 未登录跳过
        try {
            Boolean isStudent = SecurityUtils.isStudent();
            // 学生角色不需要数据范围控制
            if (isStudent == null || isStudent) {
                return;
            }
        } catch (Exception e) {
            log.info("数据权限验证时，因未登录引起的计划内异常:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        /*
         * 获取注解
         * */
        String namespace = ms.getId();
        //获取mapper名称
        String className = namespace.substring(0, namespace.lastIndexOf("."));
        //获取方法名
        String methedName = namespace.substring(namespace.lastIndexOf(".") + 1, namespace.length());
        //获取当前mapper 的方法
        Method[] methods = Class.forName(className).getMethods();

        boolean dataScoped = false;

        for (Method m : methods) {
            if (m.getName().equals(methedName)) {
                DataScoped annotation = m.getAnnotation(DataScoped.class);
                if (annotation != null) {
                    dataScoped = annotation.scoped();
                }
            }
        }
        if (!dataScoped) {
            return;
        }


        // 创建新查询条件
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        String originalSql = boundSql.getSql();
        mpBs.sql(buildSql(originalSql));
        // mpBs.sql(originalSql);

    }

    /**
     * 解析SQL语句，并返回新的SQL语句
     * 注意，该方法使用了JSqlParser来操作SQL，该依赖包Mybatis-plus已经集成了。如果要单独使用，请先自行导入依赖
     *
     * @param sql 原SQL
     * @return 新SQL
     */
    private String buildSql(String sql) {

        try {
            Select statement = (Select) CCJSqlParserUtil.parse(sql);
            SelectBody selectBody = statement.getSelectBody();
            if (!(selectBody instanceof PlainSelect)) {
                return sql;
            }
            PlainSelect plainSelect = (PlainSelect) selectBody;
            FromItem fromTable = plainSelect.getFromItem();
            Table table = (Table) fromTable;
            // log.info("====>table: " + table);

            String entityName = table.getName();
            // log.info("====>entityName: " + table);

            if (StringUtils.isBlank(entityName)) {
                return sql;
            }

            Alias alias = fromTable.getAlias();
            String aliasName = alias == null ? "" : alias.getName();

            String sqlStr = makeCondition(entityName, aliasName);
            if (StringUtils.isBlank(sqlStr)) {
                return sql;
            }

            Expression parseRes = CCJSqlParserUtil.parseCondExpression(sqlStr);
            Expression sqlSegment = new AndExpression(plainSelect.getWhere(), parseRes);
            plainSelect.setWhere(sqlSegment);

//            System.out.println("==============buildSql===================");
//            System.out.println(plainSelect);
//            System.out.println("==============buildSql===================");

            return plainSelect.toString();

        } catch (JSQLParserException e) {
            log.error("解析原SQL并构建新SQL错误：" + e);
            return sql;
        }

    }

    /**
     * 数据权限就是通过本方法实现的，在sql语句后面拼接上where过滤条件
     *
     * @return null表示不受数据权限控制
     */
    private String makeCondition(String entityName, String tableAliasName) {

        DataScopeEntityEnum scopeEntityEnum = null;
        for (DataScopeEntityEnum entityEnum : DataScopeEntityEnum.values()) {
            if (entityName.equals(entityEnum.getCode())) {
                scopeEntityEnum = entityEnum;
                break;
            }
        }
        if (scopeEntityEnum == null) {
            return null;
        }

        Long uid = StaffAuthService.getCurrentUserId();
        if (uid == null) {
            return null;
        }
        List<DataPermission> permissions = DataPermissionService.getPermissionList(StaffAuthService.getCurrentUserId(), entityName);
        if (permissions == null || permissions.size() == 0) {
            return null;
        }
        StaffOrginfo orgInfo = DataPermissionService.getOrgInfo(uid);
        if (orgInfo == null) {
            return null;
        }

        List<String> sqlList = new ArrayList<>();

        for (DataPermission dp : permissions) {

            DataScopeTypeEnum scopeType = dp.getScopeType();
            if (scopeType == null
                    || DataScopeTypeEnum.ALL.equals(scopeType)
                    || StringUtils.isBlank(dp.getEntityName())
                    ) {
                continue;
            }

            String scopeSql;
            if (DataScopeTypeEnum.GROUP.equals(scopeType)) {
                if (orgInfo.getGroupId() == null) {
                    continue;
                }
                scopeSql = " in( select id from org where find_in_set('" + orgInfo.getGroupId() + "', id_path))";
            } else if (DataScopeTypeEnum.COM.equals(scopeType)) {
                if (orgInfo.getComId() == null) {
                    continue;
                }
                scopeSql = " in( select id from org where find_in_set('" + orgInfo.getComId() + "', id_path))";
            } else if (DataScopeTypeEnum.DPT.equals(scopeType)) {
                if (orgInfo.getDptId() == null) {
                    continue;
                }
                scopeSql = " in( select id from org where find_in_set('" + orgInfo.getDptId() + "', id_path))";
            } else if (DataScopeTypeEnum.SELF.equals(scopeType)) {
                // 如果是只看自己的而且又没有设置字段，则不受数据权限控制
                if(StringUtils.isBlank(dp.getOwnerField())) {
                    continue;
                }
                scopeSql = "";
            } else if (DataScopeTypeEnum.CUSTOM.equals(scopeType)) {
                scopeSql = " in( select id from org where id in (select org_id from data_permission_custom where permission_id = '" + dp.getId() + "') ) ";
            } else {
                // 设置不正确导致的
                continue;
            }

            if (StringUtils.isNotBlank(scopeSql)) {
                // 使用负责组织字段进行查询
                sqlList.add(buildTableName(tableAliasName, dp.getOwnerOrgField()) + scopeSql);
            } else {
                // 使用负责人字段进行查询
                sqlList.add(buildTableName(tableAliasName, dp.getOwnerField()) + " = " + orgInfo.getStaffId());
            }
        }
        if (sqlList.size() == 0) {
            return null;
        }
        return "(" + StringUtils.join(sqlList, " OR ") + ")";
    }

    private static String buildTableName(String tableAlias, String columnName) {
        if (StringUtils.isNotBlank(tableAlias)) {
            columnName = tableAlias + "." + columnName;
        }
        return columnName;
    }
}
