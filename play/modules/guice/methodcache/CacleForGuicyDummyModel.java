package play.modules.guice.methodcache;


import com.google.inject.AbstractModule;
import static com.google.inject.matcher.Matchers.*;

public class CacleForGuicyDummyModel extends AbstractModule{

	@Override
	protected void configure() {
		bindInterceptor(any(),annotatedWith(CacheForMethod.class),new CacheForMethodInterceptor());		
	}

}
