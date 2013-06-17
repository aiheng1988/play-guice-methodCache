package play.modules.guice.methodcache;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import play.Logger;
import play.Play;
import play.cache.Cache;

/**
 * 基于guice和play-guice实现的一个在方法上使用注解来实现缓存的功能
 * 
 * @author rongliang.xiong
 * 
 */
public class CacheForMethodInterceptor implements MethodInterceptor {
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = null;
		Method method = invocation.getMethod();
		String methodName = getMethodName(invocation.getThis(), method);
		CacheForMethod cacheFor = method.getAnnotation(CacheForMethod.class);
		String key = dealWithCacheKey(cacheFor.key(), invocation.getArguments());
		if (StringUtils.isNotBlank(cacheFor.key())) {
			result = Cache.get(key);
			if (result != null) {
				Logger.debug("命中缓存了 key: " + key + " 方法:" + methodName);
				System.out.println("命中缓存了 key: " + key + " 方法:" + methodName);
				return result;
			}
		}
		result = invocation.proceed();
		if (result != null) {
			Cache.set(key, result, getCacheTime(cacheFor.time()));
			Logger.debug("设置缓存了 key: " + key + " 方法:" + methodName);
			System.out.println("设置缓存了 key: " + key + " 方法:" + methodName);
		} else {
			System.out.println(methodName + " 没有返回值，无法设置缓存!");
			Logger.debug(methodName + " 没有返回值，无法设置缓存!");
		}
		return result;
	}
	
	public static String getMethodName(Object obj ,Method method){
		String clazzName = obj!= null ? obj.getClass().getName() : "";
		clazzName = clazzName.substring(0, clazzName.indexOf("$"));
		String methodName = method != null ? method.getName() : "";
		return clazzName+"."+methodName;
	}

	public static String getCacheTime(String time) {
		String result = time;
		if (StringUtils.startsWith(time, "cron.")) {
			result = Play.configuration.getProperty(time);
			;
		}
		return result;
	}

	public static String dealWithCacheKey(String key, Object[] args)
			throws IllegalArgumentException, IllegalAccessException {
		String result = key;
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				if (arg == null) {
					arg = "null";
				}
				Class clazz = arg.getClass();
				result = result.replaceAll("\\{" + (i + 1) + "\\}",
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