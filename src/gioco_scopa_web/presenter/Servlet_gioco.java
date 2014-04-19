package gioco_scopa_web.presenter;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

/**
 * WebSocketServlet is contained in catalina.jar. It also needs servlet-api.jar
 * on build path
 *
 *
 */
@WebServlet("/gioco")
public class Servlet_gioco extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	// for new clients, <sessionId, streamInBound>
	private static ConcurrentHashMap<String, StreamInbound> clients = new ConcurrentHashMap<String, StreamInbound>();
 
	@Override
	protected StreamInbound createWebSocketInbound(String protocol,HttpServletRequest httpServletRequest) {
	// Check if exists
	HttpSession session = httpServletRequest.getSession();
	
	StreamInbound client=null;
    client = new Web_socket(httpServletRequest);
    clients.put(session.getId(), client);
    return client;
	}

}