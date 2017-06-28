package com.github.plugin.id;

import com.github.base.util.ReflectUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: 吴海旭
 * Date: 2017-06-28
 * Time: 下午5:15
 */
@Intercepts({
		@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class AutoIncrementKeyPlugin implements Interceptor {

	private Logger logger = LoggerFactory.getLogger(AutoIncrementKeyPlugin.class);

	private Properties properties = new Properties();
	private IDGen idGen;
	private Map<String, PropertyColumnPair> interceptTablesSettings;
	// 否要替换sql语句中的xxx.NEXTVAL
	private boolean replaceNextValue;
	// 如果已经设置了keyProperty的值， 是否还要新生成id
	private boolean replaceExistsNewId;
	// 是否初始化状态
	private volatile boolean initialized = false;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		logger.info("AutoIncrementKeyPlugin start");

		if (!initialized) {
			logger.info("AutoIncrementKeyPlugin initialize..");
			initializeConfiguration();
			logger.info("AutoIncrementKeyPlugin initialized");
		}

//		RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();
//		StatementHandler handlerInner = (StatementHandler) ReflectUtil.getFieldValue(handler, "delegate");
		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];

		if (mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
			logger.info("AutoIncrementKeyPlugin start insert");
			BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
			String insertTable = parseInsertTable(boundSql.getSql());

			String idKeyProperty = getIdKeyProperty(insertTable);
			String idKeyColumn = getIdKeyColumn(insertTable);
			boolean hasGenerateKeyConfig = idKeyProperty != null;
			logger.info("table {} keyProperty [{}] keyColumn [{}]", insertTable, idKeyProperty, idKeyColumn);
			if (hasGenerateKeyConfig) {
				logger.info("inject auto_id start");
				// 如果想插入完对象id有值，使用Jdbc3KeyGenerator
				ReflectUtil.setFieldValue(mappedStatement, "keyGenerator", NoKeyGenerator.INSTANCE);
//				ReflectUtil.setFieldValue(mappedStatement, "keyGenerator", Jdbc3KeyGenerator.INSTANCE);
				logger.info("setNewId start");
				setNewId(mappedStatement, boundSql, insertTable, idKeyProperty);
				logger.info("changeStatement start");
				changeStatement(mappedStatement, boundSql, idKeyProperty, idKeyColumn);
			}
		}

		logger.info("AutoIncrementKeyPlugin proceed");
		return invocation.proceed();
	}

	private void changeStatement(MappedStatement mappedStatement, BoundSql boundSql, String keyProperty, String keyColumn) {
		String sql = boundSql.getSql();

		Pattern quoteStartPattern = Pattern.compile("(insert\\s+)(ignore\\s*)?(INTO\\s+[^\\(]+)\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);
		Matcher columnsMatcher = quoteStartPattern.matcher(sql);
		String[] columnsArray = null;
		if (columnsMatcher.find()) {
			String columns = columnsMatcher.group(4);
			columnsArray = columns.split("[\\s]*,[\\s]*");
			boolean includeKey = false;
			for (int columnIndex = 0; columnIndex < columnsArray.length; columnIndex++) {
				String column = columnsArray[columnIndex];
				String clearColumn = cleanDbName(column);
				columnsArray[columnIndex] = clearColumn;
				if (clearColumn.equalsIgnoreCase(keyColumn)) {
					includeKey = true;
					break;
				}
			}

			if (!includeKey) {
				String replacement = String.format("$1$2$3(%s,$4)", keyColumn);
				sql = columnsMatcher.replaceFirst(replacement);

				Pattern valuesPattern = Pattern.compile("(\\bVALUES\\b\\s*)(\\()", Pattern.CASE_INSENSITIVE);

				String replacementValues = "$1$2?,";
				sql = valuesPattern.matcher(sql).replaceFirst(replacementValues);
			}


			if (replaceNextValue) {
				Pattern patternNextVal = Pattern.compile("[\\S]+\\.NEXTVAL", Pattern.CASE_INSENSITIVE);
				sql = patternNextVal.matcher(sql).replaceAll("?");
			}
		}

		ReflectUtil.setFieldValue(boundSql, "sql", sql);

		boolean hasPrimaryKeyParameter = false;
		for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
			if (parameterMapping.getProperty().equals(keyProperty)) {
				hasPrimaryKeyParameter = true;
				break;
			}
		}
		if (!hasPrimaryKeyParameter) {
			ParameterMapping parameterMapping = new ParameterMapping.Builder(
					mappedStatement.getConfiguration(),
					keyProperty,
					long.class).jdbcType(JdbcType.BIGINT)
					.mode(ParameterMode.IN).build();
			int parameterIndex = -1;
			if (columnsArray == null) {
				parameterIndex = 0;
			} else {
				for (int i=0; i<columnsArray.length;i++) {
					if (columnsArray[i].equals(keyColumn)) {
						parameterIndex = i;
						break;
					}
				}
			}
			// if key property not in columnsArray, parameterIndex should be 0
			parameterIndex = Math.max(parameterIndex, 0);
			boundSql.getParameterMappings().add(parameterIndex, parameterMapping);
		}

	}

	private void setNewId(MappedStatement mappedStatement, BoundSql boundSql,
						  String insertTable, String idKeyProperty) {
		Configuration configuration = mappedStatement.getConfiguration();
		MetaObject metaParam = configuration.newMetaObject(boundSql.getParameterObject());
		Object currentId = metaParam.getValue(idKeyProperty);
		boolean hasSetId = currentId != null &&
				(
						(currentId instanceof Integer && ((Integer) currentId) > 0)
								|| (currentId instanceof  Long && (Long) currentId > 0)
				);
		//如果没有设置id， 或者配置了， 强制替换id参数， 那么调用id生成服务，自动生成id
		if (!hasSetId || replaceExistsNewId) {
			// set  external id to pojo object
			IDGen newKeyGenerator = getIDGen();
			long newId = newKeyGenerator.newId(insertTable);
			logger.info("set newId: {}", newId);
			metaParam.setValue(idKeyProperty, newId);
		}
	}

	private synchronized IDGen getIDGen() {
		return idGen;
	}

	private String getIdKeyProperty(String table) {
		if (interceptTablesSettings.containsKey(table)) {
			return interceptTablesSettings.get(table).getProperty();
		}
		return null;
	}

	private String getIdKeyColumn(String table) {
		if (interceptTablesSettings.containsKey(table)) {
			return interceptTablesSettings.get(table).getColumn();
		}
		return null;
	}

	private String parseInsertTable(String sql) {
		Pattern pattern = Pattern.compile("insert\\s+(ignore\\s*)?INTO\\s+([^\\(]+?)\\s*\\(", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		if (matcher.find()) {
			String table = matcher.group(2);
			table = cleanDbName(table);
			int dotIndex = table.indexOf('.');
			if (dotIndex > -1) {
				table = table.substring(dotIndex + 1);
			}
			return table;
		}

		throw new RuntimeSqlException("can not parse insert table from sql " + sql);
	}

	private String cleanDbName(String column) {
		Pattern invalidNamePattern = Pattern.compile("[^a-z0-9_]+", Pattern.CASE_INSENSITIVE);
		return invalidNamePattern.matcher(column).replaceAll("");
	}

	private synchronized void initializeConfiguration() {
		if (idGen == null) {
			String keyGeneratorType = properties.getProperty("idGen.type");
			try {
				if (keyGeneratorType == null || keyGeneratorType.length() == 0) {
					throw new RuntimeException("please specify keyGenerator.type property for plugin");
				}
				Class<? extends IDGen> clazz = (Class<? extends IDGen>) Class.forName(keyGeneratorType);
				idGen = clazz.newInstance();
			} catch (Exception ex) {
				String msg = String.format("keyGenerator Type [%s] error ", keyGeneratorType);
				throw new RuntimeException(msg, ex);
			}
		}

		// like `student=id:id,user=userId:user_id`
		if (interceptTablesSettings == null) {
			String tables = properties.getProperty("interceptTableSettings");
			Map<String, PropertyColumnPair> tableSettings = new TreeMap<String, PropertyColumnPair>(String.CASE_INSENSITIVE_ORDER);
			if (tables != null) {
				String[] tableArray = tables.split("[,;]");
				for (String tableItem : tableArray) {
					int eqIndex = tableItem.indexOf('=');
					String tableName, keyPropertyColumn;
					if (eqIndex == -1) {
						tableName = tableItem.toUpperCase();
						keyPropertyColumn = null;
					} else {
						tableName = tableItem.substring(0, eqIndex).trim().toUpperCase();
						keyPropertyColumn = tableItem.substring(eqIndex+1).trim();
					}

					PropertyColumnPair propertyColumnPair = new PropertyColumnPair();
					if (keyPropertyColumn != null) {
						String[] propertyColumnArray = keyPropertyColumn.split(":");
						propertyColumnPair.setProperty(propertyColumnArray[0]);
						propertyColumnPair.setColumn(propertyColumnArray[1]);
					}
					tableSettings.put(tableName, propertyColumnPair);
				}
			}
			interceptTablesSettings = tableSettings;
		}

		replaceNextValue = properties.getProperty("replaceNextValue", "true").equals("true");
		replaceExistsNewId = properties.getProperty("replaceExistsNewId", "false").equals("true");
		initialized = true;
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	/**
	 * idGen.type 外部自增长id生成服务类class
	 * interceptTableSettings 要拦截insert操作的表配置,以英文逗号或分号分隔， 若不指定拦截器将不工作。
	 * 		例如：student=id:id表示表student有主键的属性为id，数据库列名也为id
	 * 		如果sql语句中的表名带有库名， 会去掉库名， 如： dms.send_d将使用send_d作为表名判断是否要拦截。
	 * replaceNextValue 是否要替换sql语句中的xxx.NEXTVAL, 如果是oracle语法的sql， 要对此类语句做替换， 默认会做替换
	 * replaceExistsNewId 如果已经设置了keyProperty的值， 是否还要新生成id， true表示是， false为否, 默认为false
	 * @param arg
	 */
	@Override
	public void setProperties(Properties arg) {
		if (arg != null) {
			properties.putAll(arg);
		}
	}
}
