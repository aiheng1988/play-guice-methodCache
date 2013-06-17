package play.modules.guice.methodcache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import play.libs.Time;

/**
 * 
 * 将注解缓存所有没有超时的缓存key保存,但是无法保证每一个key都是有用的，
 * 因为缓存有自动淘汰算法，所有需要获取内容后判断是否为null
 * 
 * @author rongliang.xiong
 * 
 */
public class CacheKeyManage {

	private static Map<String, CacheKey> cacheMaps = new ConcurrentHashMap<String, CacheKey>();

	/**
	 * 添加缓存,只让CacheForMethodInterceptor可以调用
	 * 
	 * @param cacheKey 缓存的key
	 * @param cacheTime 缓存时间
	 */
	static void addCacheKey(String cacheKey, String cacheTime) {
		if (cacheKey != null && "".endsWith(cacheKey) && cacheTime != null
				&& !"".equals(cacheTime)) {
			cacheMaps.put(cacheKey,
					new CacheKey(cacheKey, Time.parseDuration(cacheTime)));
		}
	}

	/**
	 * 通过cachekey前缀获取一系列的还存活的缓存内容的key
	 * @param prefix key的前缀
	 * @return 通过前缀获取一些类的key，如果是""则返回所有
	 */
	public static List<String> getAllCacheKeyWith(String prefix) {
		List<String> result = new ArrayList<String>();
		if (prefix == null) {
			for (CacheKey key : cacheMaps.values()) {
				if (System.currentTimeMillis() - key.getAddTime().getTime() >= key
						.getCacheTime()) {
					cacheMaps.remove(key.getKey());
					continue;
				}
				if (key.getKey().startsWith(prefix)) {
					result.add(key.getKey());
				}
			}
		}
		return result;
	}

	/**
	 * 获取所有的还存活的缓存内容的key
	 * @param prefix key的前缀
	 * @return 通过前缀获取一些类的key，如果是""则返回所有
	 */
	public static List<String> getAllCacheKey() {
		return getAllCacheKeyWith("");
	}

	private static class CacheKey {
		/**
		 * 缓存的key
		 */
		private String key;
		/**
		 * 添加时间
		 */
		private Date addTime;
		/**
		 * 缓存的key
		 */
		private int cacheTime;

		CacheKey(String key, int cacheTime) {
			this.key = key;
			this.cacheTime = cacheTime;
			this.addTime = new Date();
		}

		public String getKey() {
			return key;
		}

		public Date getAddTime() {
			return addTime;
		}

		public int getCacheTime() {
			return cacheTime;
		}
	}
}
