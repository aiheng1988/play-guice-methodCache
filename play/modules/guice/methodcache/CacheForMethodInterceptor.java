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
		String key = CacheKeyUtil.dealWithCacheKey(cacheFor.key(), invocation.getArguments());
		if (key != null) {
			result = Cache.get(key);
			if (result != null) {
				Logger.debug("Hit cache for key: " + key + " method:"+ methodName);
				System.out.println("Hit cache for key: " + key + " method:"+ methodName);
				if (result == Null.nullObj) {
					return null;
				}
				return result;
			}
		} else {
			Logger.debug("The cache key is empty, will not be cached!");
			System.out.println("The cache key is empty, will not be cached!");
		}
		result = invocation.proceed();
		if (method.getReturnType() != void.class && key != null) {
			result = (result == null && cacheFor.cacheNullAble()) ? Null.nullObj : result;
			if (result != null) {
				String cacheTime = getCacheTime(cacheFor.time());
				Cache.set(key, result, cacheTime);
				if(cacheFor.isRemember()){
					CacheKeyManage.addCacheKey(key, cacheTime);
				}
				Logger.debug("set cache for key: " + key + " method:"+ methodName);
				System.out.println("set cache for key: " + key + " method:"+ methodName);
			} else {
				System.out.println(methodName+ ":the return value is null, not cache!");
				Logger.debug(methodName+ ":the return value is null, not cache!");
			}
		} else {
			System.out.println(methodName+ ":No return value, unable to set the cache!");
			Logger.debug(methodName+ ":No return value, unable to set the cache!");
		}
		return result;
	}

	public static String getMethodName(Object obj, Method method) {
		String clazzName = obj != null ? obj.getClass().getName() : "";
		int index = clazzName.indexOf("$");
		if (index < 0) {
			index = clazzName.length();
		}
		clazzName = clazzName.substring(0, index);
		String methodName = method != null ? method.getName() : "";
		return clazzName + "." + methodName;
	}

	public static String getCacheTime(String time) {
		if(time == null || "".equals(time)){
			throw new IllegalArgumentException("Invalid cacheTime pattern : " + String.valueOf(time));
		}
		String result = time;
		if (StringUtils.startsWith(time, "cron.")) {
			result = Play.configuration.getProperty(time);
		}
		return result;
	}
}