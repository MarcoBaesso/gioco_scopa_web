package gioco_scopa_web.presenter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;

import gioco_scopa_web.model.*;

import org.json.*;

/**
 * Need tomcat-koyote.jar on class path, otherwise has compile error "the hierarchy of the type ... is inconsistent"
 *
 */
public class Web_socket extends MessageInbound{

	private WsOutbound myoutbound;
	private Procedure_di_gioco gioco;
	private JSONObject json_obj_buffer=new JSONObject();
	
	public Web_socket(HttpServletRequest httpServletRequest) {}

	@Override
	public void onOpen(WsOutbound outbound) {
	//setting only a client per time
	this.myoutbound = outbound;
	try{
		synchronized(Singleton_gioco.getInstance()){
			while (Singleton_gioco.getInstance().get_just_once()==true){
				Singleton_gioco.getInstance().wait();
			}
			gioco=Procedure_di_gioco.getInstance(); 
		}
	 }
	 catch (InterruptedException e){
		 e.printStackTrace();
	 }
 }
 
	@Override
	public void onClose(int status) {
	synchronized(Singleton_gioco.getInstance()){
		Singleton_gioco.getInstance().set_just_once(false);
		gioco.azzera_per_nuova_partita();
	 	Singleton_gioco.getInstance().notify();
	}
	}
 
	@Override
	protected void onBinaryMessage(ByteBuffer arg0) throws IOException {}

	@Override
	protected void onTextMessage(CharBuffer inChar) throws IOException {

	JSONObject json_obj=new JSONObject();	
	String message=inChar.toString();
	
	int type_message=0;
	int m1=message.indexOf("type:'newpartita'"); //1
	int m2=message.indexOf("type:'mossa_computer'"); //2
	int m3=message.indexOf("type:'aggiorna_mossa_computer'"); //3
	int m4=message.indexOf("type:'mossa_player'"); //4
	int m5=message.indexOf("type:'dai_carte'"); //5
	int m6=message.indexOf("type:'distribuisci_carte_tavolo'"); //6
	int m7=message.indexOf("type:'newset'"); //7
	if (m1!=-1){
		type_message=1;
	}else{
		if (m2!=-1){
			type_message=2;
		}else{
			if (m3!=-1){
				type_message=3;
			}else{
				if (m4!=-1){
					type_message=4;
				}else{
					if (m5!=-1){
						type_message=5;
					}else{
						if (m6!=-1){
							type_message=6;
						}else{
							if (m7!=-1){
								type_message=7;
							}
						}
					}
				}
			}			
		}
	}
	
	switch (type_message){
		case 1:
		{
			if (gioco.new_match_is_available()){
				gioco.azzera_per_nuova_partita();
				gioco.set_new_match_available(false);
				gioco.mescola_carte();
				gioco.pesca_carte_tavolo_gioco();
				gioco.distribuisci_carte_mano_gioco(true);
				try{
				
				JSONArray json_obj_carte_tavolo=new JSONArray();
				for (int i=0;i<10;i++){
					Carta carta=gioco.get_carte_tavolo_gioco().get(i+1);
					if (carta!=null){
						String carta_json=String.valueOf(carta.get_valore());
						carta_json=carta_json.concat("_" + gioco.get_string_seme(carta.get_seme()));
						json_obj_carte_tavolo.put(carta_json);						
					}
				}
				
				JSONArray json_obj_carte_player=new JSONArray();
				for (int i=0;i<gioco.get_carte_player_mano_partita().size();i++){
					Carta carta=gioco.get_carte_player_mano_partita().get(i);
					if (carta!=null){
						String carta_json=String.valueOf(carta.get_valore());
						carta_json=carta_json.concat("_" + gioco.get_string_seme(carta.get_seme()));
						json_obj_carte_player.put(carta_json);	
					}
				}
				
				json_obj.put("type","newpartita");
				json_obj.put("tavolo",json_obj_carte_tavolo);
				json_obj.put("player",json_obj_carte_player);
				json_obj.put("inizia_computer",gioco.get_begin_from_computer());
				
				CharBuffer outbuffer = CharBuffer.wrap(json_obj.toString());
				this.myoutbound.writeTextMessage(outbuffer);

				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else{
				return;
			}
			break;
		}
		case 2:
		{
			gioco.attendi(2400);
			
			Procedure_di_gioco.Stato_nodo stato=gioco.next_state();
			
			Carta carta_IA_computer=gioco.alfa_beta_search(stato);
			
			String cartajson=String.valueOf(carta_IA_computer.get_valore());
			cartajson=cartajson.concat("_" + gioco.get_string_seme(carta_IA_computer.get_seme()));
			
			gioco.aggiorna_stato_gioco(Procedure_di_gioco.Stato_nodo.Turno.MAX,carta_IA_computer);
			
			JSONArray json_obj_carte_tavolo=new JSONArray();
			for (int i=0;i<10;i++){
				Carta carta=gioco.get_carte_tavolo_gioco().get(i+1);
				if (carta!=null){
					String carta_json=String.valueOf(carta.get_valore());
					carta_json=carta_json.concat("_" + gioco.get_string_seme(carta.get_seme()));
					json_obj_carte_tavolo.put(carta_json);						
				}
			}
			
			try{
			json_obj.put("type","mossa_computer");
			json_obj.put("carta_computer",cartajson);

			json_obj_buffer.put("type","aggiorna_mossa_computer");
			json_obj_buffer.put("tavolo",json_obj_carte_tavolo);
			json_obj_buffer.put("carte_computer", gioco.get_num_carte_computer_partita());
			json_obj_buffer.put("carte_player", gioco.get_num_carte_player_partita());
			json_obj_buffer.put("denari_computer", gioco.get_num_denari_computer_partita());
			json_obj_buffer.put("denari_player", gioco.get_num_denari_player_partita());
			json_obj_buffer.put("primiera_computer", gioco.get_num_7_computer_partita());
			json_obj_buffer.put("primiera_player", gioco.get_num_7_player_partita());
			json_obj_buffer.put("sette_bello_computer", gioco.get_sette_bello_computer_partita());
			json_obj_buffer.put("sette_bello_player", gioco.get_sette_bello_player_partita());
			json_obj_buffer.put("re_bello_computer", gioco.get_re_bello_computer_partita());
			json_obj_buffer.put("re_bello_player", gioco.get_re_bello_player_partita());
			json_obj_buffer.put("scope_computer_totale",gioco.get_num_scope_computer_partita());
			json_obj_buffer.put("scope_player_totale",gioco.get_num_scope_player_partita());
			
			CharBuffer outbuffer = CharBuffer.wrap(json_obj.toString());
			this.myoutbound.writeTextMessage(outbuffer);
			
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		case 3:
		{	
			gioco.attendi(2000);
			
			CharBuffer outbuffer = CharBuffer.wrap(json_obj_buffer.toString());
			this.myoutbound.writeTextMessage(outbuffer);
			json_obj_buffer=new JSONObject();
			break;
		}
		case 4:
		{ 
			String carta_string=message.substring(28, message.length()-2);
			Carta mossa=gioco.get_carta(carta_string);
			Carta carta=gioco.mossa_avversario(mossa);
			if (carta!=null){
				try{
					JSONArray json_obj_carte_tavolo=new JSONArray();
					for (int i=0;i<10;i++){
						Carta card=gioco.get_carte_tavolo_gioco().get(i+1);
						if (card!=null){
							String carta_json=String.valueOf(card.get_valore());
							carta_json=carta_json.concat("_" + gioco.get_string_seme(card.get_seme()));
							json_obj_carte_tavolo.put(carta_json);						
						}
					}
					json_obj.put("type","mossa_player");
					json_obj.put("tavolo",json_obj_carte_tavolo);
					json_obj.put("carte_computer", gioco.get_num_carte_computer_partita());
					json_obj.put("carte_player", gioco.get_num_carte_player_partita());
					json_obj.put("denari_computer", gioco.get_num_denari_computer_partita());
					json_obj.put("denari_player", gioco.get_num_denari_player_partita());
					json_obj.put("primiera_computer", gioco.get_num_7_computer_partita());
					json_obj.put("primiera_player", gioco.get_num_7_player_partita());
					json_obj.put("sette_bello_computer", gioco.get_sette_bello_computer_partita());
					json_obj.put("sette_bello_player", gioco.get_sette_bello_player_partita());
					json_obj.put("re_bello_computer", gioco.get_re_bello_computer_partita());
					json_obj.put("re_bello_player", gioco.get_re_bello_player_partita());
					json_obj.put("scope_computer_totale",gioco.get_num_scope_computer_partita());
					json_obj.put("scope_player_totale",gioco.get_num_scope_player_partita());
					
					CharBuffer outbuffer = CharBuffer.wrap(json_obj.toString());
					this.myoutbound.writeTextMessage(outbuffer);
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			else{
				ArrayList<ArrayList<Integer>> combinazioni=gioco.get_combinazioni_disponibili(mossa);
				JSONArray json_obj_combinazioni=new JSONArray();
				JSONArray json_obj_combinazione=new JSONArray();
				try{
					for (int i=0;i<combinazioni.size();i++){
						ArrayList<Integer> comb=combinazioni.get(i);
						boolean allarme=true;
						for (int z=0;z<comb.size() && allarme;z++){
							json_obj_combinazione.put(comb.get(z));
						}
						json_obj_combinazioni.put(json_obj_combinazione);
						json_obj_combinazione=new JSONArray();
					}
					json_obj.put("type","combinazioni_disponibili");
					json_obj.put("tavolo",json_obj_combinazioni);
					CharBuffer outbuffer = CharBuffer.wrap(json_obj.toString());
					this.myoutbound.writeTextMessage(outbuffer);
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		case 5:
		{
			gioco.distribuisci_carte_mano_gioco(true);
			try{
				JSONArray json_obj_carte_player=new JSONArray();
				for (int i=0;i<gioco.get_carte_player_mano_partita().size();i++){
					Carta carta=gioco.get_carte_player_mano_partita().get(i);
					if (carta!=null){
						String carta_json=String.valueOf(carta.get_valore());
						carta_json=carta_json.concat("_" + gioco.get_string_seme(carta.get_seme()));
						json_obj_carte_player.put(carta_json);	
					}
				}
			
				json_obj.put("type","dai_carte");
				json_obj.put("inizia_computer",gioco.get_begin_from_computer());
				json_obj.put("player",json_obj_carte_player);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			
			CharBuffer outbuffer = CharBuffer.wrap(json_obj.toString());
			this.myoutbound.writeTextMessage(outbuffer);
			break;
		}
		case 6:{
			try{
				gioco.dai_il_vincitore();
				json_obj.put("type","distribuisci_carte_tavolo");
				json_obj.put("ultimo_a_prendere",gioco.get_ultimo_a_prendere());
				json_obj.put("carte_computer", gioco.get_num_carte_computer_partita());
				json_obj.put("carte_player", gioco.get_num_carte_player_partita());
				json_obj.put("denari_computer", gioco.get_num_denari_computer_partita());
				json_obj.put("denari_player", gioco.get_num_denari_player_partita());
				json_obj.put("primiera_computer", gioco.get_num_7_computer_partita());
				json_obj.put("primiera_player", gioco.get_num_7_player_partita());
				json_obj.put("sette_bello_computer", gioco.get_sette_bello_computer_partita());
				json_obj.put("sette_bello_player", gioco.get_sette_bello_player_partita());
				json_obj.put("re_bello_computer", gioco.get_re_bello_computer_partita());
				json_obj.put("re_bello_player", gioco.get_re_bello_player_partita());
				json_obj.put("scope_computer_totale",gioco.get_num_scope_computer_partita());
				json_obj.put("scope_player_totale",gioco.get_num_scope_player_partita());
				
				json_obj.put("num_set_vinti_computer", gioco.get_num_set_vinti_computer());
				json_obj.put("num_set_vinti_player",gioco.get_num_set_vinti_player());
				json_obj.put("num_set_giocati", gioco.get_num_set_giocati());
				
				CharBuffer outbuffer = CharBuffer.wrap(json_obj.toString());
				this.myoutbound.writeTextMessage(outbuffer);
				
				gioco.set_new_set_available(true);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		case 7:{
			{
				gioco.attendi(1000);
				if (gioco.new_set_is_available()){
					gioco.azzera_per_nuovo_set();
					gioco.mescola_carte();
					gioco.pesca_carte_tavolo_gioco();
					gioco.distribuisci_carte_mano_gioco(gioco.get_begin_from_computer());
					
					try{
					
					JSONArray json_obj_carte_tavolo=new JSONArray();
					for (int i=0;i<10;i++){
						Carta carta=gioco.get_carte_tavolo_gioco().get(i+1);
						if (carta!=null){
							String carta_json=String.valueOf(carta.get_valore());
							carta_json=carta_json.concat("_" + gioco.get_string_seme(carta.get_seme()));
							json_obj_carte_tavolo.put(carta_json);						
						}
					}
					
					JSONArray json_obj_carte_player=new JSONArray();
					for (int i=0;i<gioco.get_carte_player_mano_partita().size();i++){
						Carta carta=gioco.get_carte_player_mano_partita().get(i);
						if (carta!=null){
							String carta_json=String.valueOf(carta.get_valore());
							carta_json=carta_json.concat("_" + gioco.get_string_seme(carta.get_seme()));
							json_obj_carte_player.put(carta_json);	
						}
					}
					
					json_obj.put("type","newset");
					json_obj.put("tavolo",json_obj_carte_tavolo);
					json_obj.put("player",json_obj_carte_player);
					json_obj.put("inizia_computer",gioco.get_begin_from_computer());
					
					CharBuffer outbuffer = CharBuffer.wrap(json_obj.toString());
					this.myoutbound.writeTextMessage(outbuffer);

					}
					catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else{
					return;
				}
				break;
			}
		}
	}

	}

}
