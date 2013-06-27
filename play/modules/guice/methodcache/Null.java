package play.modules.guice.methodcache;

class Null {
	private Null(){
		
	}
    static Null nullObj = new Null();
    
    public static boolean isNull(Object obj){
    	return obj == null || obj instanceof Null;
    }
}
