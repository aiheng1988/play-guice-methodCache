package play.modules.guice.methodcache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法缓存的注解
 * @author rongliang.xiong
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheForMethod {
	//缓存的时间
    String time() default "1h";
    //作为缓存的key
    String key();
    //是否缓存Null值,如果需要缓存NUll，手动获取缓存，需要判断是否是Null实例
    boolean cacheNullAble() default false;
    //是否记录缓存的key
    boolean rememberCacheKey() default false;
    //cachekey 中的表达式通过参数名还是位置,如果为true {}
    boolean byName() default false;
}
