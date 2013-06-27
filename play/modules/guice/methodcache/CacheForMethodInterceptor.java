package play.modules.guice.methodcache;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.classloading.enhancers.LocalvariablesNamesEnhancer;

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
		String key = CacheKeyUtil.dealWithCacheKeyByIndex(cacheFor.key(), invocation.getArguments() , getParameterNames(cacheFor, method));
		if (key != null) {
			result = Cache.get(key);
			if (result != null) {
				Logger.info("Hit cache for key: " + key + " method:"+ methodName);
				if (result == Null.nullObj) {
					return null;
				}
				return result;
			}
		} else {
			Logger.info("The cache key is empty, will not be cached!");
		}
		result = invocation.proceed();
		if (method.getReturnType() != void.class && key != null) {
			Object cacheResult = (result == null && cacheFor.cacheNullAble()) ? Null.nullObj : result;
			if (cacheResult != null) {
				String cacheTime = getCacheTime(cacheFor.time());
				Cache.set(key, cacheResult, cacheTime);
				if(cacheFor.rememberCacheKey()){
					CacheKeyManage.addCacheKey(key, cacheTime);
				}
				Logger.info("set cache for key: " + key + " method:"+ methodName);
			} else {
				Logger.info(methodName+ ":the return value is null, not cache!");
			}
		} else {
			Logger.info(methodName+ ":No return value, unable to set the cache!");
		}
		return result;
	}

	/**
	 * 获取参数名
	 * @param cacheFor
	 * @param method
	 * @return
	 */
	private static String[] getParameterNames(CacheForMethod cacheFor  , Method method){
		if(cacheFor.byName()){
			List<String> parameterNames = LocalvariablesNamesEnhancer.lookupParameterNames(method);
			return (String[]) parameterNames.toArray();
		}else{
			int size = method.getParameterTypes().length;
			String[] result = new String[size];
			for(int i = 0 ; i < size ; i++){
				result[i] = String.valueOf(i);
			}
			return result;
		}
	}
	
	private static String getMethodName(Object obj, Method method) {
		String clazzName = obj != null ? obj.getClass().getName() : "";
		int index = clazzName.indexOf("$");
		if (index < 0) {
			index = clazzName.length();
		}
		clazzName = clazzName.substring(0, index);
		String methodName = method != null ? method.getName() : "";
		return clazzName + "." + methodName;
	}

	private static String getCacheTime(String time) {
		if(time == null || "".equals(time)){
			throw new IllegalArgumentException("Invalid cacheTime pattern : " + String.valueOf(time));
		}
		String result = time;
		if (time.startsWith("cron.")) {
			result = Play.configuration.getProperty(time);
		}
		return result;
	}
}