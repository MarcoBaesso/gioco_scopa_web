package gioco_scopa_web.presenter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;

/**
 * Need tomcat-koyote.jar on class path, otherwise has compile error "the hierarchy of the type ... is inconsistent"
 * @author wangs
 *
 */
public class Web_socket extends MessageInbound{

 private String name;
 
 private WsOutbound myoutbound;
 
 public Web_socket(HttpServletRequest httpSerbletRequest) {
  
 }
 @Override
 public void onOpen(WsOutbound outbound) {
  System.out.println("on open..");
  this.myoutbound = outbound;
  try {
   this.myoutbound.writeTextMessage(CharBuffer.wrap("hi, what's your name?"));
   
  } catch (Exception e) {
   throw new RuntimeException(e);
  }
  
 }
 
 @Override
 public void onClose(int status) {
  System.out.println("Close client");
  //remove from list
 }
 
 @Override
 protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
  
 }

 @Override
 protected void onTextMessage(CharBuffer inChar) throws IOException {
  
  System.out.println("Accept msg");
  CharBuffer outbuf = CharBuffer.wrap("- " + this.name + " says : ");
  CharBuffer buf = CharBuffer.wrap(inChar);
  
  if(name != null) {
   this.myoutbound.writeTextMessage(outbuf);
   this.myoutbound.writeTextMessage(buf);
  } else {
   this.name = inChar.toString();
   
   CharBuffer welcome = CharBuffer.wrap("== Welcome " + this.name + "!");
   this.myoutbound.writeTextMessage(welcome);
  }
  
  this.myoutbound.flush();
  
 }

}
