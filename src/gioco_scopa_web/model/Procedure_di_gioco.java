package gioco_scopa_web.model;

import gioco_scopa_web.model.Procedure_di_gioco.Stato_nodo.Azione_valore;


//import java.io.PrintWriter;
import java.util.ArrayList;
//import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
//import java.util.Set;


import org.json.*;

import java.io.BufferedWriter; 
import java.io.File; 
import java.io.FileWriter; 
import java.io.IOException;

public class Procedure_di_gioco {
	private ArrayList<Carta> carte=new ArrayList<Carta>(); 
	// fix the rule that first 10 are "DENARI"
	// next 10 are "COPPE", next 10 are "SPADE"
	// last 10 are "BASTONI". 40 cards in total.
	private static Procedure_di_gioco procedure_gioco=null;
	// mazzo stay to the deck of cards in a specif game
	private ArrayList<Carta> mazzo=new ArrayList<Carta>();
	
	private Integer numero_set_giocati=0;
	private Integer numero_set_vinti_computer=0;
	private Integer numero_set_vinti_player=0;
	
	private boolean new_inizio=true;
	
	private HashMap<Integer,Carta> carte_tavolo_gioco=null;
	private ArrayList<Carta> carte_computer_mano_partita=new ArrayList<Carta>();
	private ArrayList<Carta> carte_player_mano_partita=new ArrayList<Carta>();
	
	private Procedure_di_gioco(){
		inizializza_carte();
	}
	
	/*
	public static void main(String[] args){
		Procedure_di_gioco gioco=Procedure_di_gioco.getInstance();
		
		//1)
		Procedure_di_gioco.getInstance().inizializza_carte();
		
		//2)
		gioco.mescola_carte();
		
		//3)
		HashMap<Integer,Carta> onTableCards=gioco.pesca_carte_tavolo();
		
		for (int i=0;i<10;i++){
			Carta c=onTableCards.get(i+1);
			if (c!=null)
			System.out.print(c.get_seme().toString() + " " + c.get_valore().toString() + "\n");
		}
		
		//4)
		ArrayList<Carta> computer=new ArrayList<Carta>();
		ArrayList<Carta> player=new ArrayList<Carta>();
		// true indicates that cards are distributed from computer to player
		// and so the first to begin is the player
		gioco.distribuisci_carte(true,computer,player);
		
		for (int i=0;i<computer.size();i++){
			Carta c=computer.get(i);
			System.out.print(c.get_seme().toString() + " " + c.get_valore().toString() + "\n");
		}
		for (int i=0;i<player.size();i++){
			Carta c=player.get(i);
			System.out.print(c.get_seme().toString() + " " + c.get_valore().toString() + "\n");
		}
		
		//5) alfa-beta pruning
		//prepara nodo iniziale
		Stato_nodo stato=new Stato_nodo(onTableCards,computer,player,null,0,0,0,0);
		Carta cartaComputer=gioco.alfa_beta_search(stato);
		
		cartaComputer=cartaComputer;
		
		
		//Procedure_di_gioco.Stato_nodo s= new Procedure_di_gioco.Stato_nodo();
		
		
		//for (int i=0;i<gioco.mazzo.size();i++){
		//	Carta c=gioco.mazzo.get(i);
		//	System.out.print(c.get_seme().toString() + " " + c.get_valore().toString() + "\n");
		//}
		
	}
*/
	
	private static Integer max(Integer a,Integer b){if (a>b) return a; else return b;}
	
	private static Integer min(Integer a,Integer b){if (a<b) return a; else return b;}
	
	public static Procedure_di_gioco getInstance(){
		if (procedure_gioco==null){
			procedure_gioco=new Procedure_di_gioco();
			Singleton_gioco.getInstance().set_just_once(true);
		}
		return procedure_gioco;
	}
	
	public static JSONObject to_JSon(Alfa_beta_nodo nodo){
		
	try {
		JSONObject obj = new JSONObject();
		JSONObject carta=new JSONObject();
		JSONArray array_combinazione=new JSONArray();
		JSONArray array_figli=new JSONArray();
		Azione_valore azione=nodo.get_azione_padre();
		
		// set the value of the card
		if (azione!=null){
			carta.put("valore",azione.get_azione().get_valore());
			carta.put("seme",azione.get_azione().get_seme());
			// set the value of the catching cards
			if (azione.get_combinazione()!=null){
				ArrayList<Integer> combinazione=Stato_nodo.sottoCombinazioni.get(azione.get_azione().get_valore())
						.get(azione.get_combinazione());
				for(int i=0;i<combinazione.size();i++){
					array_combinazione.put(combinazione.get(i));
				}
			}
		}
		
		ArrayList<Alfa_beta_nodo> figli=nodo.array_figli();
		for(int i=0;i<figli.size();i++){
			Alfa_beta_nodo figlio=figli.get(i);
			JSONObject JSONfiglio=to_JSon(figlio);
			array_figli.put(JSONfiglio);
		}
		
	    obj.put("carta", carta);
	    obj.put("carte_prese", array_combinazione);
	    if (nodo.get_minimax()!=null){
	    	obj.put("minimax", nodo.get_minimax());
	    }
	    else{
	    	obj.put("minimax",false);
	    }
	    obj.put("figli", array_figli);
	    return obj;
	} 
	catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
	}
	
	public static String JSon_to_string_albero(JSONObject obj){
		String obj_string=obj.toString();
		String return_obj=new String();
		Integer num_tab=0;
		for(int i=0;i<obj_string.length();i++){
			
			for(int z=0;z<num_tab;z++){
				if (z==0){
					return_obj=return_obj.concat(",");
					return_obj=return_obj.concat("\n");
				}
				return_obj=return_obj.concat("        ");
			}
			
			Integer index_figli=obj_string.indexOf("\"figli\":[",i);
			Integer index_fine=obj_string.indexOf("]}",i);
			
			// insert tab
			int j=index_figli;
			int from=i;
			int to=index_figli;
			String append=new String();
			while (j<index_fine && j!=-1){
				return_obj=return_obj.concat(append);
				append="";
				return_obj=return_obj.concat(obj_string.substring(from,to));
				return_obj=return_obj.concat("\n");
				for(int z=0;z<num_tab;z++){
					return_obj=return_obj.concat("        ");
				}
				num_tab++;
				
				return_obj=return_obj.concat("\"figli\":[");
				
				append=append.concat("\n");
				for(int z=0;z<num_tab;z++){
					append=append.concat("        ");
				}
				j=obj_string.indexOf("\"figli\":[",to+9);
				from=to+9; // move up by "figli":[".length()
				to=j;
			}
			
			while (index_fine+2<=obj_string.length() && obj_string.substring(index_fine,index_fine+2).equals("]}")){
				num_tab--;
				return_obj=return_obj.concat("]}");
				index_fine=index_fine+2;
			}
			i=index_fine;
		}
		
		return return_obj;
	}
	
	public boolean new_match_is_available(){
		return new_inizio;
	}
	
	public void set_new_match_available(boolean match){
		new_inizio=match;
	}
	
	public Carta alfa_beta_search(Stato_nodo stato){
		Carta returnAzione=null;
		Alfa_beta_nodo padre=new Alfa_beta_nodo(null);
		Integer azioneValore=/*Procedure_di_gioco.getInstance().*/max_value(stato,Integer.MIN_VALUE,Integer.MAX_VALUE,padre);
		JSONObject obj=/*Procedure_di_gioco.getInstance().*/to_JSon(padre);
		String string_json=JSon_to_string_albero(obj);
		
		File outputFile = new File("/home/marcobaesso/Eclipse_Workspace/gioco_scopa_web/WebContent/data/albero.txt"); 
		try {
			FileWriter fw = new FileWriter(outputFile.getAbsolutePath()); 
			outputFile.getPath();
			BufferedWriter bw = new BufferedWriter(fw); 
			bw.write(string_json);  
			bw.close(); 
			fw.close(); 
		} catch (IOException e) { 
			e.printStackTrace(); 
		} 
		
		
		returnAzione=stato.get_best_action();
		return returnAzione;
	}
	
	private Integer max_value(Stato_nodo stato,Integer alfa,Integer beta,Alfa_beta_nodo padre){
		if (stato.terminal_test()){
			padre.set_minimax(stato.utility());
			padre.set_alfa(alfa);
			padre.set_beta(beta);
			return stato.utility();
		}
		else{
			ArrayList<Stato_nodo> azioni=stato.azioni(Stato_nodo.Turno.MAX);
			
			for(int i=0;i<azioni.size();i++){
				padre.add_figlio(azioni.get(i).azioneValore);
			}
			
			Integer v=Integer.MIN_VALUE;
			Integer best_pos=0;
			for(int i=0;i<azioni.size();i++){
				Integer min_val=min_value(azioni.get(i),alfa,beta,padre.get_figlio(i));
				if (v<min_val){
					v=min_val;
					best_pos=i;
				}
				if (v>=beta){
					padre.set_minimax(v);
					padre.set_alfa(alfa);
					padre.set_beta(beta);
					return v;
				}
				alfa=max(alfa,v);
			}
			
			padre.set_minimax(v);
			padre.set_alfa(alfa);
			padre.set_beta(beta);
			
			if (stato.is_root_state()){
				stato.set_best_action(azioni.get(best_pos));
			}
			return v;
		}
	}
	
	private Integer min_value(Stato_nodo stato,Integer alfa,Integer beta,Alfa_beta_nodo padre){
		if (stato.terminal_test()){
			padre.set_minimax(stato.utility());
			padre.set_alfa(alfa);
			padre.set_beta(beta);
			return stato.utility();
		}
		else{
			ArrayList<Stato_nodo> azioni=stato.azioni(Stato_nodo.Turno.MIN);
			
			for(int i=0;i<azioni.size();i++){
				padre.add_figlio(azioni.get(i).azioneValore);
			}
			
			Integer v=Integer.MAX_VALUE;
			for(int i=0;i<azioni.size();i++){
				v=min(v,max_value(azioni.get(i),alfa,beta,padre.get_figlio(i)));
				if (v<=alfa){
					padre.set_minimax(v);
					padre.set_alfa(alfa);
					padre.set_beta(beta);
					return v;
				}
				beta=min(beta,v);
			}
			
			padre.set_minimax(v);
			padre.set_alfa(alfa);
			padre.set_beta(beta);
			
			return v;
		}
	}

	public void pesca_carte_tavolo_gioco(){
		carte_tavolo_gioco=pesca_carte_tavolo();
	}
	
	// the cards that must put on the table must to be different
	// on numbers, to semplify the play
	// so draw four cards from the mix up deck that are different in values
	private HashMap<Integer,Carta> pesca_carte_tavolo(){
		HashMap<Integer,Carta> cards=new HashMap<Integer,Carta>();
		HashMap<Integer,Integer> differentNumbers=new HashMap<Integer,Integer>();
		// orderedIndexes is an Hash because of an Hash keep his element in order
		HashMap<Integer,Boolean> orderedIndexes=new HashMap<Integer,Boolean>();

		for(int i=0; i<mazzo.size() && differentNumbers.size()<4; i++){
			Carta carta=mazzo.get(i);
			Integer bool=differentNumbers.get(carta.get_valore());
			if (bool==null){
				differentNumbers.put(carta.get_valore(), new Integer(i));
				orderedIndexes.put(new Integer(i),true);
			}
		}
		Object[] keys=orderedIndexes.keySet().toArray();
		
		// remove from the mish up dick the cards that now are on the table
		for(int i=0; i<keys.length; i++){
			Carta rimossa=mazzo.remove((int) keys[i]-i);
			cards.put(rimossa.get_valore(), rimossa);
		}
		return cards;
	}
	
	public void distribuisci_carte_mano_gioco(boolean begin_from_computer){
		carte_computer_mano_partita.clear();
		carte_player_mano_partita.clear();		
		distribuisci_carte(begin_from_computer,carte_computer_mano_partita,carte_player_mano_partita);
	}
	
	// begin_from_computer=true if cards are distributed from computer
	// to the player, false otherwise
	// true means that computer starts
	private void distribuisci_carte(boolean begin_from_computer,ArrayList<Carta> computer,ArrayList<Carta> player){
		ArrayList<Carta> first=null;
		ArrayList<Carta> second=null;
		if (begin_from_computer){
			first=computer;
			second=player;
		}
		else{
			first=player;
			second=computer;
		}
		for(int i=0;i<6;i++){
			Carta carta=mazzo.remove(0);
			if ((i % 2)==0){
				first.add(carta);
			}
			else{
				second.add(carta);
			}
		}
	}
		
	// index must to be in the range 0 .. 39 included 
	public Carta get_carta(int index){
		return carte.get(index);
	}
	
	public void mescola_carte(){
		ArrayList<Carta>mazzo_output=new ArrayList<Carta>();
		Random randomize=new Random();
		// mix up the deck first time
		mescola(0,39,randomize,mazzo_output,true);
		
		// simulate a good mix up deck
		rimescola_carte(1000,randomize,mazzo_output,false);
		
		/*
		for (int i=0;i<mazzo.size();i++){
			Carta c=mazzo.get(i);
			System.out.print(c.get_seme().toString() + " " + c.get_valore().toString() + "\n");
		}
		*/
		
	}
	
	private void rimescola_carte(int numvolte, Random randomize,ArrayList<Carta>mazzo_output,boolean first_time){
		for (int j=0;j<numvolte;j++){
			mazzo_output.clear();
			mescola(0,39,randomize,mazzo_output,false);
			mazzo.clear();
			for (int i=0;i<mazzo_output.size();i++){
				mazzo.add(mazzo_output.get(i));
			}
		}
	}
		
	// first_time suggests that it is the first time you mix up the cards
	// and so the cards are taken from the Procedure_di_gioco
	private void mescola(int min,int max,Random randomize,ArrayList<Carta>mazzo_output,boolean first_time){
		//divide et impera
		int random=0;
		if (min==max){  //case 0
			mazzo_output.add(ottieni_carta_mazzo(first_time,min));
			if (first_time){ 
				mazzo.add(ottieni_carta_mazzo(first_time,min));
			}
			return;
		}
		else{
			random=randomize.nextInt(max - min +1) + min; // nextInt(num) return a number between the range 0 .. num-1 included
			// random can be a number between max and min included
			// so index of divide et impera arrays in case of random=max are
			// the first must be length max-min-1; the second is one element and i can system it directly (case1)
			// otherwise divide et impera can procede regularry also if random is the min element because the next
			// call to mescola is in the case0
			
			int dado=randomize.nextInt(2);
			if (dado==1){
				if (random==max){  // case1
					mescola(min,random-1,randomize,mazzo_output,first_time);
					mazzo_output.add(ottieni_carta_mazzo(first_time,max));
					if (first_time){
						mazzo.add(ottieni_carta_mazzo(first_time,max));
					}
				}
				else{  // case2
					mescola(min,random,randomize,mazzo_output,first_time);
					mescola(random+1,max,randomize,mazzo_output,first_time);
				}
			}
			else{
				if (random==max){
					mescola(min,random-1,randomize,mazzo_output,first_time);
					mazzo_output.add(ottieni_carta_mazzo(first_time,max));
					if (first_time){
						mazzo.add(ottieni_carta_mazzo(first_time,max));
					}
				}
				else{	
					mescola(random+1,max,randomize,mazzo_output,first_time);
					mescola(min,random,randomize,mazzo_output,first_time);
				}
			}
			}
		}
	
	// first_time suggests you from which deck you have to mix up the cards
	private Carta ottieni_carta_mazzo(boolean first_time,int index){
		if (first_time)
			return /*Procedure_di_gioco.getInstance().*/get_carta(index);
		else
			return mazzo.get(index);
	}
	
	public void inizializza_carte(){
		if (carte.size()!=40){
			carte.clear();
			carte.add(new Carta(Seme.DENARI,1));
			carte.add(new Carta(Seme.DENARI,2));
			carte.add(new Carta(Seme.DENARI,3));
			carte.add(new Carta(Seme.DENARI,4));
			carte.add(new Carta(Seme.DENARI,5));
			carte.add(new Carta(Seme.DENARI,6));
			carte.add(new Carta(Seme.DENARI,7));
			carte.add(new Carta(Seme.DENARI,8));
			carte.add(new Carta(Seme.DENARI,9));
			carte.add(new Carta(Seme.DENARI,10));
			carte.add(new Carta(Seme.COPPE,1));
			carte.add(new Carta(Seme.COPPE,2));
			carte.add(new Carta(Seme.COPPE,3));
			carte.add(new Carta(Seme.COPPE,4));
			carte.add(new Carta(Seme.COPPE,5));
			carte.add(new Carta(Seme.COPPE,6));
			carte.add(new Carta(Seme.COPPE,7));
			carte.add(new Carta(Seme.COPPE,8));
			carte.add(new Carta(Seme.COPPE,9));
			carte.add(new Carta(Seme.COPPE,10));
			carte.add(new Carta(Seme.SPADE,1));
			carte.add(new Carta(Seme.SPADE,2));
			carte.add(new Carta(Seme.SPADE,3));
			carte.add(new Carta(Seme.SPADE,4));
			carte.add(new Carta(Seme.SPADE,5));
			carte.add(new Carta(Seme.SPADE,6));
			carte.add(new Carta(Seme.SPADE,7));
			carte.add(new Carta(Seme.SPADE,8));
			carte.add(new Carta(Seme.SPADE,9));
			carte.add(new Carta(Seme.SPADE,10));
			carte.add(new Carta(Seme.BASTONI,1));
			carte.add(new Carta(Seme.BASTONI,2));
			carte.add(new Carta(Seme.BASTONI,3));
			carte.add(new Carta(Seme.BASTONI,4));
			carte.add(new Carta(Seme.BASTONI,5));
			carte.add(new Carta(Seme.BASTONI,6));
			carte.add(new Carta(Seme.BASTONI,7));
			carte.add(new Carta(Seme.BASTONI,8));
			carte.add(new Carta(Seme.BASTONI,9));
			carte.add(new Carta(Seme.BASTONI,10));	
		}
	}
	
	public String get_string_seme(Seme seme){
		switch (seme){
		case DENARI: return "DENARI";
		case COPPE: return "COPPE";
		case SPADE: return "SPADE"; 
		case BASTONI: return "BASTONI";
		default: return null;
		}
	}
	
	public HashMap<Integer,Carta> get_carte_tavolo_gioco() {
		return carte_tavolo_gioco;
	}

	public ArrayList<Carta> get_carte_computer_mano_partita(){
		return carte_computer_mano_partita;
	}
	
	public ArrayList<Carta> get_carte_player_mano_partita(){
		return carte_player_mano_partita;
	}
	
	public static class Alfa_beta_nodo{
		private Azione_valore azione_padre=null;
		private ArrayList<Alfa_beta_nodo> azioni_figli=null;
		private Integer alfa=null;
		private Integer beta=null;
		private Integer minimax=null;
		
		public Alfa_beta_nodo(Azione_valore padre){
			this.azione_padre=padre;
			this.azioni_figli=new ArrayList<Alfa_beta_nodo>();
		}
		
		public ArrayList<Alfa_beta_nodo> array_figli(){
			return azioni_figli;
		}
		
		public Azione_valore get_azione_padre(){
			return azione_padre;
		}
		
		public void add_figlio(Azione_valore figlio){
			azioni_figli.add(new Alfa_beta_nodo(figlio));
		}
		
		public Alfa_beta_nodo get_figlio(Integer index){
			return azioni_figli.get(index);	
		}

		public Integer get_alfa() {
			return alfa;
		}

		public void set_alfa(Integer alfa) {
			this.alfa = alfa;
		}

		public Integer get_beta() {
			return beta;
		}

		public void set_beta(Integer beta) {
			this.beta = beta;
		}

		public Integer get_minimax() {
			return minimax;
		}

		public void set_minimax(Integer minimax) {
			this.minimax = minimax;
		}

	}
	
	public static class Stato_nodo{
		private static HashMap<Integer,ArrayList<ArrayList<Integer>>> sottoCombinazioni=new HashMap<Integer,ArrayList<ArrayList<Integer>>>();
		private HashMap<Integer,Carta> carteTavolo=new HashMap<Integer,Carta>();
		private ArrayList<Carta> computer=new ArrayList<Carta>();
		private ArrayList<Carta> player=new ArrayList<Carta>();
		private Azione_valore azioneValore=null; // score obtained with that moves
		private Integer num_denari_player=0;
		private Integer num_denari_computer=0;
		private Integer num_carte_player=0;
		private Integer num_carte_computer=0;
		
		private Carta bestAction=null; // fix the best action; use to fix the best action
									   // only for the root state
		
		public static enum Turno{MIN,MAX}
		
		/* PUNTEGGIO
			|1|: Re Bello
			|1|: 7 Bello
			|1|: 1 scopa
			|0.25|: per ogni 7
			|1/6 arrotonda per eccesso|: per ogni denaro se ne il player ne il computer sono arrivati ad almeno 6 denari
			|1/21 arrotonda per eccesso|: per ogni carta se ne il player ne il computer sono arrivati ad almento 21 carte
		*/
		
		public static HashMap<Integer,Carta> copy_carte_tavolo(Stato_nodo stato){
			HashMap<Integer,Carta> clone=new HashMap<Integer,Carta>();
			for (int i=0;i<10;i++){
				Carta carta=stato.carteTavolo.get(i+1);
				if (carta!=null){clone.put(i+1, carta);}
			}
			return clone;
		}
		
		public static ArrayList<Carta> copy_computer(Stato_nodo stato){
			ArrayList<Carta> clone=new ArrayList<Carta>();
			for (int i=0;i<stato.computer.size();i++){
				clone.add(stato.computer.get(i));
			}
			return clone;
		}
		
		public static ArrayList<Carta> copy_player(Stato_nodo stato){
			ArrayList<Carta> clone=new ArrayList<Carta>();
			for (int i=0;i<stato.player.size();i++){
				clone.add(stato.player.get(i));
			}
			return clone;
		}
		
		public Stato_nodo(HashMap<Integer,Carta> carteTavolo,ArrayList<Carta> computer,
				ArrayList<Carta> player,Azione_valore azioneValore,
				Integer num_cartePlayer,Integer num_carteComputer,Integer num_denariPlayer,
				Integer num_denariComputer){
			this.carteTavolo=carteTavolo;
			this.computer=computer;
			this.player=player;
			this.azioneValore=azioneValore;
			this.num_denari_player=num_denariPlayer;
			this.num_denari_computer=num_denariComputer;
			this.num_carte_player=num_cartePlayer;
			this.num_carte_computer=num_carteComputer;
		}
		
		public ArrayList<Stato_nodo> azioni(Turno turno){
			ArrayList<Stato_nodo> returnAzioni=new ArrayList<Stato_nodo>();
			ArrayList<Carta> computer_o_player=null;
			
			switch (turno){
				case MIN:
					computer_o_player=player;
					break;
				case MAX:
					computer_o_player=computer;
					break;
			}
			
			for (int i=0;i<computer_o_player.size();i++){
					Carta azione=computer_o_player.get(i);
					if (carteTavolo.get(azione.get_valore())!=null){
							
						HashMap<Integer,Carta> carte_tavolo=Stato_nodo.copy_carte_tavolo(this);
						ArrayList<Carta> carte_computer=Stato_nodo.copy_computer(this);
						ArrayList<Carta> carte_player=Stato_nodo.copy_player(this);
						
						HashMap<Integer,Carta> carte_prese=new HashMap<Integer,Carta>();
						carte_prese.put(azione.get_valore(),azione);
						Integer num_cartePlayer=new Integer(num_carte_player);
						Integer num_carteComputer=new Integer(num_carte_computer);
						Integer num_denariPlayer=new Integer(num_denari_player);
						Integer num_denariComputer=new Integer(num_denari_computer);
						Integer punti=aggiornaPunteggio(turno,azione,carte_prese,
						 num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer);
						
						Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,null);
						
						carte_tavolo.remove(azione.get_valore());
						if (turno==Turno.MIN)
							carte_player.remove(i);
						else
							carte_computer.remove(i);
							
						Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
								num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer);
						returnAzioni.add(stato);
					}
					else{
						// check if the action (the card) is an ace, on the table there is no ace
						// because of the control is in the else branch
						if (azione.get_valore()==1){

							HashMap<Integer,Carta> carte_tavolo=new HashMap<Integer,Carta>();
							ArrayList<Carta> carte_computer=Stato_nodo.copy_computer(this);
							ArrayList<Carta> carte_player=Stato_nodo.copy_player(this);
							
							HashMap<Integer,Carta> carte_prese=
								(HashMap<Integer,Carta>) new HashMap<Integer,Carta>(carteTavolo); // add all the card on the table
							Integer num_cartePlayer=new Integer(num_carte_player);
							Integer num_carteComputer=new Integer(num_carte_computer);
							Integer num_denariPlayer=new Integer(num_denari_player);
							Integer num_denariComputer=new Integer(num_denari_computer);
							Integer punti=aggiornaPunteggio(turno,azione,carte_prese,
							 num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer);
							
							Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,null);
								
							if (turno==Turno.MIN)
								carte_player.remove(i);
							else
								carte_computer.remove(i);
								
							Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
									num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer);
							returnAzioni.add(stato);
						}
						else{ // add all the composition of the cards
							ArrayList<ArrayList<Integer>> lista_combinazioni=sottoCombinazioni.get(azione.get_valore());
							boolean check=false;
							for(int j=0;j<lista_combinazioni.size();j++){
								ArrayList<Integer> combinazione =lista_combinazioni.get(j); 
								Boolean segnale=true;
								for (int z=0;z<combinazione.size() && segnale;z++){
									if (this.carteTavolo.get(combinazione.get(z))==null){
										segnale=false;
									}
								}
								if (segnale){
									check=true; // there is at least one composition of cards
									HashMap<Integer,Carta> carte_tavolo=Stato_nodo.copy_carte_tavolo(this);
									ArrayList<Carta> carte_computer=Stato_nodo.copy_computer(this);
									ArrayList<Carta> carte_player=Stato_nodo.copy_player(this);
								
									HashMap<Integer,Carta> carte_prese=new HashMap<Integer,Carta>();
									
									for (int z=0;z<combinazione.size();z++){
										carte_prese.put(combinazione.get(z), carte_tavolo.get(combinazione.get(z)));
										carte_tavolo.remove(combinazione.get(z));
									}
									
									Integer num_cartePlayer=new Integer(num_carte_player);
									Integer num_carteComputer=new Integer(num_carte_computer);
									Integer num_denariPlayer=new Integer(num_denari_player);
									Integer num_denariComputer=new Integer(num_denari_computer);
									Integer punti=aggiornaPunteggio(turno,azione,carte_prese,
										num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer);
											
									Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,j);
								
									if (turno==Turno.MIN)
										carte_player.remove(i);
									else
										carte_computer.remove(i);

									Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
											num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer);
									returnAzioni.add(stato);
								}
							} // end for
							if (!check){ // there isn't any composition
								HashMap<Integer,Carta> carte_tavolo=Stato_nodo.copy_carte_tavolo(this);
								ArrayList<Carta> carte_computer=Stato_nodo.copy_computer(this);
								ArrayList<Carta> carte_player=Stato_nodo.copy_player(this);
								
								Integer punti=0;
								if (this.azioneValore!=null)
									punti=this.azioneValore.get_valore();
								
								Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,null);
								
								carte_tavolo.put(azione.get_valore(),azione);
								
								Integer num_cartePlayer=new Integer(num_carte_player);
								Integer num_carteComputer=new Integer(num_carte_computer);
								Integer num_denariPlayer=new Integer(num_denari_player);
								Integer num_denariComputer=new Integer(num_denari_computer);
								
								if (turno==Turno.MIN)
									carte_player.remove(i);
								else
									carte_computer.remove(i);
									
								Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
									num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer);
								returnAzioni.add(stato);	
							}
						}
					}
				}
		return returnAzioni;
		} 
		
		private Integer aggiornaPunteggio(Turno turno, Carta azione, HashMap<Integer,Carta> carte_prese,
				Integer num_cartePlayer, Integer num_carteComputer,
				Integer num_denariPlayer, Integer num_denariComputer) {
			Integer punteggio_precedente=0;
				if (this.azioneValore!=null)
					punteggio_precedente=this.azioneValore.get_valore();
			Integer punteggio=0;
			
			if (carteTavolo.size()==carte_prese.size()){ // scopa
				punteggio = punteggio + 84;
			}
			
			Carta presa=azione;
			
			for (int i=0;i<10;i++){
				if (presa!=null){
					if (num_cartePlayer<21 && num_carteComputer<21){ // ++carte
						// 84/21=4
						punteggio=punteggio + 4;
						if (turno==Turno.MAX){num_carteComputer++;}else{num_cartePlayer++;}
					}
					if (presa.get_seme()==Seme.DENARI){
						if (num_denariPlayer<6 && num_denariComputer<6){ // ++denari
							// 84/6=14
							punteggio=punteggio + 14;					
							if (turno==Turno.MAX){num_denariComputer++;}else{num_denariPlayer++;}
						}
						if (presa.get_valore()==10 || presa.get_valore()==7){ // 7bello o REbello
							punteggio = punteggio + 84;
						}
					}
					else{ // non è denari, ma può essere 7
						if (presa.get_valore()==7){
							// 84/4=21
							punteggio=punteggio + 21;
						}
					}
				}
				presa=carte_prese.get(i+1);
			}
			
			if (turno==Turno.MAX){
				punteggio=punteggio_precedente + punteggio;
			}
			else{
				punteggio=punteggio_precedente - punteggio;
			}
			return punteggio;
		}

		public Boolean terminal_test(){
			if (computer.size()==0 && player.size()==0){
				return true;
			}
			return false;
		}
		
		public Boolean is_root_state(){
			return azioneValore==null;
		}
		
		public Integer utility(){
			return azioneValore.get_valore();
		}

		public Carta get_best_action() {
			return bestAction;
		}

		public void set_best_action(Stato_nodo stato) {
			this.bestAction = stato.azioneValore.get_azione();
		}
		
		public static class Azione_valore{
			private Carta azione;
			private Integer numeroCombinazione; // null if the choosen card is only one
			private Integer valore;
			
			public Azione_valore(Carta azione,Integer valore,Integer numeroCombinazione){
				this.azione=azione;
				this.valore=valore;
				this.numeroCombinazione=numeroCombinazione;
			}

			public Carta get_azione() {
				return azione;
			}

			public Integer get_combinazione(){
				return numeroCombinazione;
			}
			
			public Integer get_valore() {
				return valore;
			}
		}
		
		static{
			sottoCombinazioni.put(1, new ArrayList<ArrayList<Integer>>());
			sottoCombinazioni.put(2, new ArrayList<ArrayList<Integer>>());
			ArrayList<Integer> uno_due=new ArrayList<Integer>();uno_due.add(1);uno_due.add(2);
			sottoCombinazioni.put(3, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(3).add(uno_due);
			ArrayList<Integer> tre_uno=new ArrayList<Integer>();tre_uno.add(3);uno_due.add(1);
			sottoCombinazioni.put(4, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(4).add(tre_uno);
			ArrayList<Integer> tre_due=new ArrayList<Integer>();tre_due.add(3);tre_due.add(2);
			ArrayList<Integer> quattro_uno=new ArrayList<Integer>();quattro_uno.add(4);quattro_uno.add(1);
			sottoCombinazioni.put(5, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(5).add(tre_due);sottoCombinazioni.get(5).add(quattro_uno);
			ArrayList<Integer> cinque_uno=new ArrayList<Integer>();cinque_uno.add(5);cinque_uno.add(1);
			ArrayList<Integer> quattro_due=new ArrayList<Integer>();quattro_due.add(4);quattro_due.add(2);
			ArrayList<Integer> tre_due_uno=new ArrayList<Integer>();tre_due_uno.add(3);tre_due_uno.add(2);tre_due_uno.add(1);
			sottoCombinazioni.put(6, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(6).add(cinque_uno);sottoCombinazioni.get(6).add(quattro_due);sottoCombinazioni.get(6).add(tre_due_uno);
			ArrayList<Integer> sei_uno=new ArrayList<Integer>();sei_uno.add(6);sei_uno.add(1);
			ArrayList<Integer> cinque_due=new ArrayList<Integer>();cinque_due.add(5);cinque_due.add(2);
			ArrayList<Integer> quattro_tre=new ArrayList<Integer>();quattro_tre.add(4);quattro_tre.add(3);
			ArrayList<Integer> quattro_due_uno=new ArrayList<Integer>();quattro_due_uno.add(4);quattro_due_uno.add(2);quattro_due_uno.add(1);
			sottoCombinazioni.put(7, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(7).add(sei_uno);sottoCombinazioni.get(7).add(cinque_due);sottoCombinazioni.get(7).add(quattro_tre);sottoCombinazioni.get(7).add(quattro_due_uno);
			ArrayList<Integer> sette_uno=new ArrayList<Integer>();sette_uno.add(7);sette_uno.add(1);
			ArrayList<Integer> sei_due=new ArrayList<Integer>();sei_due.add(6);sei_due.add(2);
			ArrayList<Integer> cinque_tre=new ArrayList<Integer>();cinque_tre.add(5);cinque_tre.add(3);
			ArrayList<Integer> cinque_due_uno=new ArrayList<Integer>();cinque_due_uno.add(5);cinque_due_uno.add(2);cinque_due_uno.add(1);
			ArrayList<Integer> quattro_tre_uno=new ArrayList<Integer>();quattro_tre_uno.add(4);quattro_tre_uno.add(3);quattro_tre_uno.add(1);
			sottoCombinazioni.put(8, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(8).add(sette_uno);sottoCombinazioni.get(8).add(sei_due);sottoCombinazioni.get(8).add(cinque_tre);sottoCombinazioni.get(8).add(cinque_due_uno);sottoCombinazioni.get(8).add(quattro_tre_uno);
			ArrayList<Integer> otto_uno=new ArrayList<Integer>();otto_uno.add(8);otto_uno.add(1);
			ArrayList<Integer> sette_due=new ArrayList<Integer>();sette_due.add(7);sette_due.add(2);
			ArrayList<Integer> sei_tre=new ArrayList<Integer>();sei_tre.add(6);sei_tre.add(3);
			ArrayList<Integer> cinque_quattro=new ArrayList<Integer>();cinque_quattro.add(5);cinque_quattro.add(4);
			ArrayList<Integer> sei_due_uno=new ArrayList<Integer>();sei_due_uno.add(6);sei_due_uno.add(2);sei_due_uno.add(1);
			ArrayList<Integer> cinque_tre_uno=new ArrayList<Integer>();cinque_tre_uno.add(5);cinque_tre_uno.add(3);cinque_tre_uno.add(1);
			ArrayList<Integer> quattro_tre_due=new ArrayList<Integer>();quattro_tre_due.add(4);quattro_tre_due.add(3);quattro_tre_due.add(2);
			sottoCombinazioni.put(9, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(9).add(otto_uno);sottoCombinazioni.get(9).add(sette_due);sottoCombinazioni.get(9).add(sei_tre);sottoCombinazioni.get(9).add(cinque_quattro);sottoCombinazioni.get(9).add(sei_due_uno);sottoCombinazioni.get(9).add(cinque_tre_uno);sottoCombinazioni.get(9).add(quattro_tre_due);
			ArrayList<Integer> nove_uno=new ArrayList<Integer>();nove_uno.add(9);nove_uno.add(1);
			ArrayList<Integer> otto_due=new ArrayList<Integer>();otto_due.add(8);otto_due.add(2);
			ArrayList<Integer> sette_tre=new ArrayList<Integer>();sette_tre.add(7);sette_tre.add(3);
			ArrayList<Integer> sei_quattro=new ArrayList<Integer>();sei_quattro.add(6);sei_quattro.add(4);
			ArrayList<Integer> sette_uno_due=new ArrayList<Integer>();sette_uno_due.add(7);sette_uno_due.add(1);sette_uno_due.add(2);
			ArrayList<Integer> cinque_tre_due=new ArrayList<Integer>();cinque_tre_due.add(5);cinque_tre_due.add(3);cinque_tre_due.add(2);
			ArrayList<Integer> sei_tre_uno=new ArrayList<Integer>();sei_tre_uno.add(6);sei_tre_uno.add(3);sei_tre_uno.add(1);
			ArrayList<Integer> cinque_quattro_uno=new ArrayList<Integer>();cinque_quattro_uno.add(5);cinque_quattro_uno.add(4);cinque_quattro_uno.add(1);
			ArrayList<Integer> quattro_tre_uno_due=new ArrayList<Integer>();quattro_tre_uno_due.add(4);quattro_tre_uno_due.add(3);quattro_tre_uno_due.add(1);quattro_tre_uno_due.add(2);
			sottoCombinazioni.put(10, new ArrayList<ArrayList<Integer>>());sottoCombinazioni.get(10).add(nove_uno);sottoCombinazioni.get(10).add(otto_due);sottoCombinazioni.get(10).add(sette_tre);sottoCombinazioni.get(10).add(sei_quattro);sottoCombinazioni.get(10).add(sette_uno_due);sottoCombinazioni.get(10).add(cinque_tre_due);sottoCombinazioni.get(10).add(sei_tre_uno);sottoCombinazioni.get(10).add(cinque_quattro_uno);sottoCombinazioni.get(10).add(quattro_tre_uno_due);
		}

	}	
}
