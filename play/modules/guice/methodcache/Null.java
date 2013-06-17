package play.modules.guice.methodcache;

/**
 * 判断是否为空
 * @author rongliang.xiong
 *
 */
class Null {
	private Null(){
		
	}
    static Null nullObj = new Null();
    
    public static boolean isNull(Object obj){
    	return obj == null || obj instanceof Null;
    }
}
