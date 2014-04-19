package gioco_scopa_web.model;

public class Singleton_gioco {
	private Boolean just_once=false;
	private static Singleton_gioco singleton_gioco=null;
	
	private Singleton_gioco(){}
	
	public static Singleton_gioco getInstance(){
		if (singleton_gioco==null){
			singleton_gioco=new Singleton_gioco();
		}
		return singleton_gioco;
	}
	
	public void set_just_once(Boolean one){
		just_once=one;
	}
	
	public Boolean get_just_once(){
		return just_once;
	}
}
