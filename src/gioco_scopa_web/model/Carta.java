package gioco_scopa_web.model;

public class Carta {
	private Seme seme;
	private Integer valore; // value in the range 1 .. 10
	
	public Carta(Seme seme,Integer valore){
		this.seme=seme;
		this.valore=valore;
	}
	
	public Seme get_seme() {
		return seme;
	}
	
	public Integer get_valore() {
		return valore;
	}
	
}
