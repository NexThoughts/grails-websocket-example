package grails.websocket.example

import grails.converters.JSON
import grails.util.Environment
import grails.web.JSONBuilder

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpoint

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/grails-websocket-example/chatroomServerEndpoint
 * Where "localhost" is the address of the host,
 * "grails-websocket-example" is the name of the package
 * and "chatroomServerEndpoint" is the address to access this class from the server
 */

//Session here is a websocket session not a normal servlet session
@WebListener
@ServerEndpoint("/chatroomServerEndpoint")
public class MyServletChatListenerAnnotated implements ServletContextListener {
	
	private final Logger log = LoggerFactory.getLogger(getClass().name)
	
	static final Set<Session> chatroomUsers = ([] as Set).asSynchronized()

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.servletContext
		final ServerContainer serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer")
		try {
			//registers as a websocket endpoint
			if (Environment.current == Environment.DEVELOPMENT) {
				serverContainer.addEndpoint(MyServletChatListenerAnnotated)				
			}

			def ctx = servletContext.getAttribute(GA.APPLICATION_CONTEXT)

			def grailsApplication = ctx.grailsApplication

			def config = grailsApplication.config
			//setting the session
			int defaultMaxSessionIdleTimeout = config.myservlet.timeout ?: 0
			serverContainer.defaultMaxSessionIdleTimeout = defaultMaxSessionIdleTimeout
		}
		catch (IOException e) {
			log.error e.message, e
		}
	}
	


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }


  /**
   * @OnOpen allows us to intercept the creation of a new session.
   * The session class allows us to send data to the user.
   * In the method onOpen, we'll let the user know that the handshake was
   * successful.
   */
    @OnOpen
	public void handleOpen(Session userSession) {
      System.out.println('On open called')
		chatroomUsers.add(userSession)
	}
  /**
   * When a user sends a message to the server, this method will intercept the message
   * and allow us to react to it. For now the message is read as a String.
   */
	@OnMessage
	public String handleMessage(String message,Session userSession) throws IOException {

		System.out.println(message+'__message');
		def myMsgToBeSent=[:]
		JSONBuilder jSON = new JSONBuilder ()
		String username=(String) userSession.getUserProperties().get("username")
    //will get the username and if username is not set then it will set and append user provided message

		if (!username) {
			userSession.getUserProperties().put("username", message)
			myMsgToBeSent.put("message", "System:connected as ==>"+message)
			def myMsgToBeSentAsJson=myMsgToBeSent as JSON
			userSession.getBasicRemote().sendText(myMsgToBeSentAsJson as String)
      System.out.println("username is set")
		}else{
      //The number of chatroom in a session will be sent the message
			Iterator<Session> iterator=chatroomUsers.iterator()
			myMsgToBeSent.put("message", "${username}:${message}")
			def myMsgToBeSentAsJson=myMsgToBeSent as JSON
      System.out.println("message is send")
			while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(myMsgToBeSentAsJson as String)
		}
	}
  /**
   * The user closes the connection.
   *
   * Note: you can't send messages to the client from this method
   */
	@OnClose
	public void handeClose(Session userSession) {
     System.out.println("chat ended");
		chatroomUsers.remove(userSession)
	}
	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace()
	}
	
}
