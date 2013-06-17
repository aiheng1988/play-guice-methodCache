package play.modules.guice.methodcache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheForMethod {
	//缓存的时间
    String time() default "1h";
    //作为缓存的key
    String key();
}
