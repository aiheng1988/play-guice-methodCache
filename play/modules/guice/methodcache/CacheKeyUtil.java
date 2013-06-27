package play.modules.guice.methodcache;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
/**
 * 缓存key工具类
 * @author rongliang.xiong
 *
 */
public class CacheKeyUtil {
	public static String dealWithCacheKeyByIndex(String key, Object[] args , String[] parameterNames)
			throws IllegalArgumentException, IllegalAccessException {
		String result = key;
		if (key != null && key.length() > 0 && args != null && args.length > 0) {
			if(parameterNames == null || args.length != parameterNames.length){
				throw new IllegalArgumentException("args.length != parameterNames.length");
			}
			String parameterName="";
			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				if (arg == null) {
					arg = "null";
				}
				Class clazz = arg.getClass();
				parameterName = parameterNames[i];
				result = result.replaceAll("\\{" + (parameterName) + "\\}",
						arg.toString());
				if (!isDirect(clazz)) {
					result = dealObjWithCacheKey(String.valueOf(i + 1), arg,
							result);
				}
			}
		}
		return result;
	}

	private static String dealObjWithCacheKey(String prefix, Object arg,
			String key) throws IllegalArgumentException, IllegalAccessException {
		String result = key;
		if (key.indexOf(prefix) >= 0) {
			Class clazz = arg.getClass();
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object value = field.get(arg);
				if (value == null) {
					value = "null";
				}
				result = result.replaceAll(
						"\\{" + prefix + "." + field.getName() + "\\}",
						String.valueOf(value));
				if (!isDirect(value.getClass())) {
					result = dealObjWithCacheKey(
							prefix + "." + field.getName(), value, result);
				}
			}
		}
		return result;
	}

	public static boolean isDirect(Class<?> clazz) {
		return clazz.equals(String.class) || clazz.equals(Integer.class)
				|| Enum.class.isAssignableFrom(clazz)
				|| clazz.equals(Boolean.class) || clazz.equals(Long.class)
				|| clazz.equals(Double.class) || clazz.equals(Float.class)
				|| clazz.equals(Short.class) || clazz.equals(BigDecimal.class)
				|| clazz.isPrimitive() || clazz.equals(Class.class);
	}
}
