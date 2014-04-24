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
	private boolean new_set=false;
	private boolean begin_from_computer=true;
	
	// stato carte
	private HashMap<Integer,Carta> carte_tavolo_gioco=null;
	private ArrayList<Carta> carte_computer_mano_partita=new ArrayList<Carta>();
	private ArrayList<Carta> carte_player_mano_partita=new ArrayList<Carta>();
	
	// stato punteggio 
	private Integer num_carte_player_totale=0;
	private Integer num_carte_computer_totale=0;
	private Integer num_denari_player_totale=0;
	private Integer num_denari_computer_totale=0;
	private Integer num_7_player_totale=0;
	private Integer num_7_computer_totale=0;
	private boolean re_bello_player_totale=false;
	private boolean re_bello_computer_totale=false;
	private boolean sette_bello_player_totale=false;
	private boolean sette_bello_computer_totale=false;
	private Integer num_scope_player_totale=0;
	private Integer num_scope_computer_totale=0;
	private Stato_nodo.Turno ultimo_a_prendere=null;
	
	private Stato_nodo state=null;
	
	private Procedure_di_gioco(){
		inizializza_carte();
	}
	
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
	
	public static boolean ultima_mano(){
		if (Procedure_di_gioco.getInstance().mazzo.size()<6)
			return true;
		else
			return false;
	}
	
	public void azzera_per_nuova_partita(){
		mazzo.clear();
		
		numero_set_giocati=0;
		numero_set_vinti_computer=0;
		numero_set_vinti_player=0;
		
		new_inizio=true;
		new_set=false;
		begin_from_computer=true;
		
		// stato carte
		carte_tavolo_gioco=null;
		carte_computer_mano_partita.clear();
		carte_player_mano_partita.clear();
		
		// stato punteggio 
		num_carte_player_totale=0;
		num_carte_computer_totale=0;
		num_denari_player_totale=0;
		num_denari_computer_totale=0;
		num_7_player_totale=0;
		num_7_computer_totale=0;
		re_bello_player_totale=false;
		re_bello_computer_totale=false;
		sette_bello_player_totale=false;
		sette_bello_computer_totale=false;
		num_scope_player_totale=0;
		num_scope_computer_totale=0;
		ultimo_a_prendere=null;
		
		state=null;	
	}
	
	public void azzera_per_nuovo_set(){
		mazzo.clear();
		
		new_set=false;
		begin_from_computer=!begin_from_computer;
		
		// stato carte
		carte_tavolo_gioco=null;
		carte_computer_mano_partita.clear();
		carte_player_mano_partita.clear();
		
		// stato punteggio 
		num_carte_player_totale=0;
		num_carte_computer_totale=0;
		num_denari_player_totale=0;
		num_denari_computer_totale=0;
		num_7_player_totale=0;
		num_7_computer_totale=0;
		re_bello_player_totale=false;
		re_bello_computer_totale=false;
		sette_bello_player_totale=false;
		sette_bello_computer_totale=false;
		num_scope_player_totale=0;
		num_scope_computer_totale=0;
		ultimo_a_prendere=null;
		
		state=null;	
	}
	
	public void dai_il_vincitore(){
		Integer punti_computer=0;
		Integer punti_player=0;
		numero_set_giocati++;

		if (num_carte_computer_totale>num_carte_player_totale){
			punti_computer++;
		}
		else{
			if (num_carte_computer_totale<num_carte_player_totale){
				punti_player++;
			}
		}

		if (num_denari_computer_totale>num_denari_player_totale){
			punti_computer++;
		}
		else{
			if (num_denari_computer_totale<num_denari_player_totale){
				punti_player++;
			}
		}

		if (num_7_computer_totale>num_7_player_totale){
			punti_computer++;
		}
		else{
			if (num_7_computer_totale<num_7_player_totale){
				punti_player++;
			}
		}

		if (re_bello_player_totale) punti_player++;
		
		if (re_bello_computer_totale) punti_computer++;

		if (sette_bello_player_totale) punti_player++;
		
		if (sette_bello_computer_totale) punti_computer++;
		
		punti_computer=punti_computer+num_scope_computer_totale;
		punti_player=punti_player+num_scope_player_totale;

		if (punti_computer>punti_player){
			numero_set_vinti_computer++;
		}
		else{
			if (punti_computer<punti_player){
				numero_set_vinti_player++;
			}
		}
	}
	
	public Carta mossa_avversario(Carta carta){
		/*
		if (get_carte_tavolo_gioco().get(carta.get_valore())!=null || 
				carta.get_valore()==1 || 
				!combinazione_presente(carta) ||
				get_combinazioni_disponibili(carta).size()==1){
			aggiorna_stato_gioco(Stato_nodo.Turno.MIN,carta);
			return carta;
		}
		else
			return null;
		*/
		aggiorna_stato_gioco(Stato_nodo.Turno.MIN,carta);
		return carta;
	}
	
	public boolean combinazione_presente(Carta carta){
		ArrayList<ArrayList<Integer>> lista_combinazioni=Stato_nodo.sottoCombinazioni.get(carta.get_valore());
		boolean segnale=true;
		for (int i=0;i<lista_combinazioni.size() && segnale;i++){
			ArrayList<Integer> combinazione=lista_combinazioni.get(i);
			boolean allarme=true;
			for (int z=0;z<combinazione.size() && allarme;z++){
				if (carte_tavolo_gioco.get(combinazione.get(z))==null){
					allarme=false;
				}
			}
			if (allarme){segnale=false;}
		}
		if (segnale=false)
			return true;
		else
			return false;
	}
	
	public ArrayList<ArrayList<Integer>> get_combinazioni_disponibili(Carta carta){
		ArrayList<ArrayList<Integer>> liste=new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> liste_combinazioni=Stato_nodo.sottoCombinazioni.get(carta.get_valore());
		for (int i=0;i<liste_combinazioni.size();i++){
			ArrayList<Integer> combinazione=liste_combinazioni.get(i);
			int z=0;
			boolean allarme=true;
			for (z=0;z<combinazione.size() && allarme;z++){
				if (carte_tavolo_gioco.get(combinazione.get(z))!=null){
					allarme=true;
				}
				else{
					allarme=false;
				}
			}
			if (allarme){
				liste.add(combinazione);
			}
		}
		return liste;
	}
	
	public void aggiorna_stato_gioco(Stato_nodo.Turno turno,Carta carta){
		ArrayList<Carta> mano=null;
		
		switch (turno){
		case MAX: mano=carte_computer_mano_partita; break;
		case MIN: mano=carte_player_mano_partita; break;
		default: return;
		}
		
		Carta azione=carta;
		if (carte_tavolo_gioco.get(azione.get_valore())!=null){
			HashMap<Integer,Carta> carte_prese=new HashMap<Integer,Carta>();
			carte_prese.put(azione.get_valore(),carte_tavolo_gioco.get(azione.get_valore())); // -- 23/04 change second parameter,
			HashMap<Integer,Carta> carte_tavolo=new HashMap<Integer,Carta>(carte_tavolo_gioco);
																 						
			carte_tavolo_gioco.remove(azione.get_valore()); //    before was azione
					
			for(int h=0;h<mano.size();h++){
				Carta card=mano.get(h);
				if (azione.get_valore().equals(card.get_valore()) && azione.get_seme().equals(card.get_seme()))
					mano.remove(h);
			}
			
			aggiorna_punteggio_partita(turno,azione,carte_prese,carte_tavolo);	
			
		}
		else{
			// check if the action (the card) is an ace, on the table there is no ace
			// because of the control is in the else branch
			if (azione.get_valore()==1){
				HashMap<Integer,Carta> carte_prese=
					(HashMap<Integer,Carta>) new HashMap<Integer,Carta>(carte_tavolo_gioco); // add all the card on the table
				HashMap<Integer,Carta> carte_tavolo=new HashMap<Integer,Carta>(carte_tavolo_gioco);
						
				carte_tavolo_gioco.clear();
				
				for(int h=0;h<mano.size();h++){
					Carta card=mano.get(h);
					if (azione.get_valore().equals(card.get_valore()) && azione.get_seme().equals(card.get_seme()))
						mano.remove(h);
				}
				
				aggiorna_punteggio_partita(turno,azione,carte_prese,carte_tavolo);
				
			}
			else{
				if (turno==Stato_nodo.Turno.MAX){
					Integer numero_combinazione=state.get_best_state().get_azione_valore().get_combinazione(); 
					
					if (numero_combinazione!=null){
						ArrayList<Integer> combinazione=Stato_nodo.sottoCombinazioni.get(azione.get_valore()).get(numero_combinazione);
						
						HashMap<Integer,Carta> carte_prese=new HashMap<Integer,Carta>();
						HashMap<Integer,Carta> carte_tavolo=new HashMap<Integer,Carta>(carte_tavolo_gioco);
						
						// now can be removed cards from carte_tavolo_gioco
						for (int z=0;z<combinazione.size();z++){
							carte_prese.put(combinazione.get(z), carte_tavolo_gioco.get(combinazione.get(z)));
							carte_tavolo_gioco.remove(combinazione.get(z));
						}
						
						for(int h=0;h<mano.size();h++){
							Carta card=mano.get(h);
							if (azione.get_valore().equals(card.get_valore()) && azione.get_seme().equals(card.get_seme()))
								mano.remove(h);
						}
						
						aggiorna_punteggio_partita(turno,azione,carte_prese,carte_tavolo);
						
					}
					else{
						carte_tavolo_gioco.put(azione.get_valore(),azione);
						
						for(int h=0;h<mano.size();h++){
							Carta card=mano.get(h);
							if (azione.get_valore().equals(card.get_valore()) && azione.get_seme().equals(card.get_seme()))
								mano.remove(h);
						}
						
						aggiorna_punteggio_partita(turno,azione,null,null);
						
					}
				}
				else{
					ArrayList<ArrayList<Integer>> liste=get_combinazioni_disponibili(azione);
					if (liste.size()==0){
						carte_tavolo_gioco.put(azione.get_valore(),azione);
						
						for(int h=0;h<mano.size();h++){
							Carta card=mano.get(h);
							if (azione.get_valore().equals(card.get_valore()) && azione.get_seme().equals(card.get_seme()))
								mano.remove(h);
						}
						
						aggiorna_punteggio_partita(turno,azione,null,null);
					}
					else{
						if (liste.size()>=1){
							ArrayList<Integer> combinazione=liste.get(0);
							HashMap<Integer,Carta> carte_prese=new HashMap<Integer,Carta>();
							HashMap<Integer,Carta> carte_tavolo=new HashMap<Integer,Carta>(carte_tavolo_gioco);
							
							// now can be removed cards from carte_tavolo_gioco
							for (int z=0;z<combinazione.size();z++){
								carte_prese.put(combinazione.get(z), carte_tavolo_gioco.get(combinazione.get(z)));
								carte_tavolo_gioco.remove(combinazione.get(z));
							}
							
							for(int h=0;h<mano.size();h++){
								Carta card=mano.get(h);
								if (azione.get_valore().equals(card.get_valore()) && azione.get_seme().equals(card.get_seme()))
									mano.remove(h);
							}
							
							aggiorna_punteggio_partita(turno,azione,carte_prese,carte_tavolo);
						}
					}
				}
			}
		}
	}
	
	public void aggiorna_punteggio_partita(Stato_nodo.Turno turno, Carta azione,
			HashMap<Integer,Carta> carte_prese,HashMap<Integer,Carta> carte_tavolo){

	if (carte_prese!=null){
		this.ultimo_a_prendere=turno;
	if (carte_tavolo.size()==carte_prese.size()){ // scopa, se tira un asso è scopa solo
		   											// se in tavolo c'è un asso
		boolean segnale=true;
		if (azione.get_valore()==1){
			if (carte_tavolo.size()==1 && carte_tavolo.get(1)!=null){
				segnale=true;
			}else{
				segnale=false;
			}
		}
		if (segnale){
			switch (turno){
				case MAX: num_scope_computer_totale++; break;
				case MIN: num_scope_player_totale++; break;
				default: 
			}
		}
	}
	
	Carta presa=azione;
	
	for (int i=0;i<=10;i++){
		if (presa!=null){
			switch (turno){
			case MAX: num_carte_computer_totale++; break;
			case MIN: num_carte_player_totale++; break;
			default: 
			}
			if (presa.get_seme()==Seme.DENARI){
				switch (turno){
				case MAX: num_denari_computer_totale++; break;
				case MIN: num_denari_player_totale++; break;
				default: 
				}
				if (presa.get_valore()==10){ // REbello
					switch (turno){
					case MAX: re_bello_computer_totale=true; break;
					case MIN: re_bello_player_totale=true; break;
					default: 
					}
				}
				if (presa.get_valore()==7){ // 7bello
					switch (turno){
					case MAX: sette_bello_computer_totale=true; break;
					case MIN: sette_bello_player_totale=true; break;
					default: 
					}	
				}
			}
			if (presa.get_valore()==7){	// primiera
				switch (turno){
				case MAX: num_7_computer_totale++; break;
				case MIN: num_7_player_totale++; break;
				default: 
				}	
			}
		}
		presa=carte_prese.get(i+1);
	}
	}// fine primo if

		if (ultima_mano() && carte_computer_mano_partita.size()==0 && carte_player_mano_partita.size()==0){
			for (int i=0;i<10;i++){
				Carta carta=carte_tavolo_gioco.get(i+1);
				if (carta!=null){
					switch (this.ultimo_a_prendere){
					case MAX: num_carte_computer_totale++; break;
					case MIN: num_carte_player_totale++; break;
					default: 
					}
					if (carta.get_seme()==Seme.DENARI){
						switch (this.ultimo_a_prendere){
						case MAX: num_denari_computer_totale++; break;
						case MIN: num_denari_player_totale++; break;
						default: 
						}
						if (carta.get_valore()==10){ // REbello
							switch (this.ultimo_a_prendere){
							case MAX: re_bello_computer_totale=true; break;
							case MIN: re_bello_player_totale=true; break;
							default: 
							}
						}
						if (carta.get_valore()==7){ // 7bello
							switch (this.ultimo_a_prendere){
							case MAX: sette_bello_computer_totale=true; break;
							case MIN: sette_bello_player_totale=true; break;
							default: 
							}	
						}
					}
					if (carta.get_valore()==7){	// primiera
						switch (this.ultimo_a_prendere){
						case MAX: num_7_computer_totale++; break;
						case MIN: num_7_player_totale++; break;
						default: 
						}	
					}
				}
			}
		}
	}
	
	public boolean new_match_is_available(){
		return new_inizio;
	}
	
	public void set_new_match_available(boolean match){
		new_inizio=match;
	}
	
	public boolean new_set_is_available(){
		return new_set;
	}
	
	public void set_new_set_available(boolean set){
		new_set=set;
	}
	
	public Stato_nodo next_state(){
		if (state==null){
			state=new Procedure_di_gioco.Stato_nodo(get_carte_tavolo_gioco(),
					get_carte_computer_mano_partita(),get_carte_player_mano_partita(),
					null,0,0,0,0,0,0,null);
		}else{
			state=new Procedure_di_gioco.Stato_nodo(get_carte_tavolo_gioco(),
					get_carte_computer_mano_partita(),get_carte_player_mano_partita(),
					null,num_carte_player_totale,num_carte_computer_totale,num_denari_player_totale,
					num_denari_computer_totale,num_7_player_totale,num_7_computer_totale,null);
		}
		return state;
	}
	
	public Carta alfa_beta_search(Stato_nodo stato){
		Carta returnAzione=null;
		Alfa_beta_nodo padre=new Alfa_beta_nodo(null);
		max_value(stato,Integer.MIN_VALUE,Integer.MAX_VALUE,padre);
		JSONObject obj=to_JSon(padre);
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
				Integer max_val=min_value(azioni.get(i),alfa,beta,padre.get_figlio(i));
				if (v<max_val){
					v=max_val;
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
	
	public Carta get_carta(String carta_string){
		String seme=carta_string.substring(carta_string.indexOf("_")+1,carta_string.length());
		Integer valore=Integer.parseInt(carta_string.substring(0,carta_string.indexOf("_")));
		Integer i=-1;
		switch (seme){
		case "DENARI": i=0; break;
		case "COPPE": i=1; break;
		case "SPADE": i=2; break;
		case "BASTONI": i=3; break;
		}
		if (i!=-1)
			return carte.get(10*i+valore-1);
		else
			return null;
	}
	
	public void mescola_carte(){
		ArrayList<Carta>mazzo_output=new ArrayList<Carta>();
		Random randomize=new Random();
		// mix up the deck first time
		mescola(0,39,randomize,mazzo_output,true);
		
		// simulate a good mix up deck
		rimescola_carte(1000,randomize,mazzo_output,false);	
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
	
	public void attendi(long time){
		
	
		try {
			synchronized(this){
				wait(time);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	
	public boolean get_begin_from_computer(){return begin_from_computer;}
	public Integer get_num_set_giocati(){return numero_set_giocati;}
	public Integer get_num_set_vinti_computer(){return numero_set_vinti_computer;}
	public Integer get_num_set_vinti_player(){return numero_set_vinti_player;}
	public Integer get_num_carte_player_partita(){return num_carte_player_totale;}
	public Integer get_num_carte_computer_partita(){return num_carte_computer_totale;}
	public Integer get_num_denari_player_partita(){return num_denari_player_totale;}
	public Integer get_num_denari_computer_partita(){return num_denari_computer_totale;}
	public Integer get_num_7_player_partita(){return num_7_player_totale;}
	public Integer get_num_7_computer_partita(){return num_7_computer_totale;}
	public Integer get_num_scope_player_partita(){return num_scope_player_totale;}
	public Integer get_num_scope_computer_partita(){return num_scope_computer_totale;}
	public boolean get_re_bello_player_partita(){return re_bello_player_totale;}
	public boolean get_re_bello_computer_partita(){return re_bello_computer_totale;}
	public boolean get_sette_bello_player_partita(){return sette_bello_player_totale;}
	public boolean get_sette_bello_computer_partita(){return sette_bello_computer_totale;}
	
	public String get_ultimo_a_prendere(){
		switch (ultimo_a_prendere){
			case MAX : return "MAX";
			case MIN : return "MIN";
		}
		return null;
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
		private Integer num_7_computer=0;
		private Integer num_7_player=0;
		
		private Turno ultimo_a_prendere=null;
		
		private Carta bestAction=null; // fix the best action; use to fix the best action
									   // only for the root state
		
		private Stato_nodo bestState=null;
		
		public static enum Turno{MIN,MAX}
		
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
				Integer num_denariComputer,Integer num_7Player,Integer num_7Computer,Turno ultimoAPrendere){
			this.carteTavolo=carteTavolo;
			this.computer=computer;
			this.player=player;
			this.azioneValore=azioneValore;
			this.num_denari_player=num_denariPlayer;
			this.num_denari_computer=num_denariComputer;
			this.num_carte_player=num_cartePlayer;
			this.num_carte_computer=num_carteComputer;
			this.num_7_player=num_7Player;
			this.num_7_computer=num_7Computer;
			this.ultimo_a_prendere=ultimoAPrendere;
		}
		
		public Azione_valore get_azione_valore(){
			return azioneValore;
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
						carte_prese.put(azione.get_valore(),carteTavolo.get(azione.get_valore())); // -- 23/04 change second parameter,
																								   //    before was azione
						Integer num_cartePlayer=new Integer(num_carte_player);
						Integer num_carteComputer=new Integer(num_carte_computer);
						Integer num_denariPlayer=new Integer(num_denari_player);
						Integer num_denariComputer=new Integer(num_denari_computer);
						Integer num_7Computer=new Integer(num_7_computer);
						Integer num_7Player=new Integer(num_7_player);
						Turno ultimoAPrendere=ultimo_a_prendere;
						
						carte_tavolo.remove(azione.get_valore());
						if (turno==Turno.MIN)
							carte_player.remove(i);
						else
							carte_computer.remove(i);
						
						Integer punti=aggiornaPunteggio(turno,azione,carte_prese,
								 num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer,
								 num_7Player,num_7Computer,ultimoAPrendere,carte_tavolo,carte_computer,carte_player);
								
						Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,null);
							
						Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
								num_cartePlayer,num_carteComputer,num_denariPlayer,
								num_denariComputer,num_7Player,num_7Computer,ultimoAPrendere);
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
							Integer num_7Computer=new Integer(num_7_computer);
							Integer num_7Player=new Integer(num_7_player);
							Turno ultimoAPrendere=ultimo_a_prendere;
	
							if (turno==Turno.MIN)
								carte_player.remove(i);
							else
								carte_computer.remove(i);
							
							Integer punti=aggiornaPunteggio(turno,azione,carte_prese,
									 num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer,
									 num_7Player,num_7Computer,ultimoAPrendere,carte_tavolo,carte_computer,carte_player);
									
							Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,null);
										
							Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
									num_cartePlayer,num_carteComputer,num_denariPlayer,
									num_denariComputer,num_7Player,num_7Computer,ultimoAPrendere);
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
									Integer num_7Computer=new Integer(num_7_computer);
									Integer num_7Player=new Integer(num_7_player);
									Turno ultimoAPrendere=ultimo_a_prendere;
								
									if (turno==Turno.MIN)
										carte_player.remove(i);
									else
										carte_computer.remove(i);
									
									Integer punti=aggiornaPunteggio(turno,azione,carte_prese,
											num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer,
											num_7Player,num_7Computer,ultimoAPrendere,carte_tavolo,carte_computer,carte_player);
												
									Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,j);

									Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
											num_cartePlayer,num_carteComputer,num_denariPlayer,
											num_denariComputer,num_7Player,num_7Computer,ultimoAPrendere);
									returnAzioni.add(stato);
								}
							} // end for
							if (!check){ // there isn't any composition
								HashMap<Integer,Carta> carte_tavolo=Stato_nodo.copy_carte_tavolo(this);
								ArrayList<Carta> carte_computer=Stato_nodo.copy_computer(this);
								ArrayList<Carta> carte_player=Stato_nodo.copy_player(this);
																
								carte_tavolo.put(azione.get_valore(),azione);
								
								Integer num_cartePlayer=new Integer(num_carte_player);
								Integer num_carteComputer=new Integer(num_carte_computer);
								Integer num_denariPlayer=new Integer(num_denari_player);
								Integer num_denariComputer=new Integer(num_denari_computer);
								Integer num_7Computer=new Integer(num_7_computer);
								Integer num_7Player=new Integer(num_7_player);
								Turno ultimoAPrendere=ultimo_a_prendere;
								
								if (turno==Turno.MIN)
									carte_player.remove(i);
								else
									carte_computer.remove(i);
								
								Integer punti=aggiornaPunteggio(turno,azione,null,
										num_cartePlayer,num_carteComputer,num_denariPlayer,num_denariComputer,
										num_7Player,num_7Computer,ultimoAPrendere,carte_tavolo,carte_computer,carte_player);
								
								Stato_nodo.Azione_valore azione_valore=new Azione_valore(azione,punti,null);

								Stato_nodo stato=new Stato_nodo(carte_tavolo,carte_computer,carte_player,azione_valore,
									num_cartePlayer,num_carteComputer,num_denariPlayer,
									num_denariComputer,num_7Player,num_7Computer,ultimoAPrendere);
								returnAzioni.add(stato);	
							}
						}
					}
				}
		return returnAzioni;
		} 
		
		private Integer aggiornaPunteggio(Turno turno, Carta azione, HashMap<Integer,Carta> carte_prese,
				Integer num_cartePlayer, Integer num_carteComputer,
				Integer num_denariPlayer, Integer num_denariComputer,
				Integer num_7Player,Integer num_7Computer,Turno ultimoAPrendere,
				HashMap<Integer,Carta>carte_tavolo,ArrayList<Carta> carte_computer,ArrayList<Carta> carte_player) {
			
			Integer punteggio_precedente=0;
			Integer punteggio=0;
			if (this.azioneValore!=null)
				punteggio_precedente=this.azioneValore.get_valore();
		if (carte_prese!=null){
					
			ultimoAPrendere=turno;
			
			if (carteTavolo.size()==carte_prese.size()){ // scopa
				boolean segnale=true;
				if (azione.get_valore()==1){
					if (carteTavolo.size()==1 && carteTavolo.get(1)!=null){
						segnale=true;
					}else{
						segnale=false;
					}
				} 
				if (segnale)
					punteggio = punteggio + 84;
			}
			
			Carta presa=azione;
			
			for (int i=0;i<=10;i++){
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
					if (presa.get_valore()==7 && num_7Player<3 && num_7Computer<3){	// può essere 7
						// 84/4=21
						punteggio=punteggio + 21;
						if (turno==Turno.MAX){num_7Computer++;}else{num_7Player++;}
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
		}//fine primo if
		else{// la carta è stata aggiunta dato che le carte prese sono 0
			punteggio=punteggio_precedente;
			ultimoAPrendere=ultimo_a_prendere;
		}
		if (Procedure_di_gioco.ultima_mano() && carte_computer.size()==0 && carte_player.size()==0){ // è l'ultima mano??
			Integer temp_punteggio=0;
			for (int i=0;i<10;i++){
				Carta carta=carte_tavolo.get(i+1);
				if (carta!=null){
					if (num_cartePlayer<21 && num_carteComputer<21){ // ++carte
						// 84/21=4
						temp_punteggio=temp_punteggio + 4;
						if (ultimoAPrendere==Turno.MAX){num_carteComputer++;}else{num_cartePlayer++;}
					}
					if (carta.get_seme()==Seme.DENARI){
						if (num_denariPlayer<6 && num_denariComputer<6){ // ++denari
							// 84/6=14
							temp_punteggio=temp_punteggio + 14;					
							if (ultimoAPrendere==Turno.MAX){num_denariComputer++;}else{num_denariPlayer++;}
						}
						if (carta.get_valore()==10 || carta.get_valore()==7){ // 7bello o REbello
							temp_punteggio = temp_punteggio + 84;
						}
					}
					if (carta.get_valore()==7 && num_7Player<3 && num_7Computer<3){	// può essere 7
						// 84/4=21
						temp_punteggio=temp_punteggio + 21;
						if (ultimoAPrendere==Turno.MAX){num_7Computer++;}else{num_7Player++;}
					}	
				}
			}
			if (ultimoAPrendere==Turno.MAX){
				punteggio=punteggio+temp_punteggio;
			}
			else{
				punteggio=punteggio-temp_punteggio;
			}
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
			this.bestState=stato;
		}
		
		public Stato_nodo get_best_state(){
			return bestState;
		}
		
		public static class Azione_valore{
			private Carta azione;
			private Integer numeroCombinazione=null; // null if the choosen card is only one
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
			ArrayList<Integer> tre_uno=new ArrayList<Integer>();tre_uno.add(3);tre_uno.add(1);
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
