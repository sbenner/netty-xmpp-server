package com.heim.netty;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.*;
import com.heim.utils.Base64Utils;
import com.heim.utils.ServiceUtils;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jivesoftware.smack.packet.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ServerHandler extends ChannelInboundHandlerAdapter {

    //@Autowired
    static Properties stanzas;

    //final static List<String> val = Collections.synchronizedList(new ArrayList());
    //TODO:
    //handle user rosters in DB
    static Map<String, String> userRoster = new HashMap<>();
    private static Map<ChannelId, SessionContext>
            sessionContextMap = new ConcurrentHashMap<>();
    private static Map<String, ChannelId>
            authorizedUserChannels = new ConcurrentHashMap<>();
    private static Map<String, Chat>
            chatMap = new ConcurrentHashMap<>();

    static {
        try {
            stanzas = ServiceUtils.loadStanzas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        userRoster.put("serg", stanzas.getProperty("rosterSergey"));
        userRoster.put("test", stanzas.getProperty("rosterTest"));
    }

    Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    static Queue<Message> messageQueue = new LinkedBlockingQueue();
    ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 150,
            5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());


    ServerHandler() {

        new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(1000);
                    Message m = messageQueue.poll();
                    if (m != null)
                        executor.execute(new MessageHandler(m));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ).start();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ChannelFuture closeFuture = ctx.channel().closeFuture();

        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                SessionContext c = sessionContextMap.remove(ctx.channel().id());
                authorizedUserChannels.remove(c.getUser() + "@" + c.getTo());
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg == null || msg.toString().trim().equals("</stream:stream>")) {
            ctx.writeAndFlush("");
            return;
        }
        String xmlstring = msg.toString();
        List<Object> objects = new ArrayList<>();
        logger.info("INPUT CHANNEL READ: " + msg.toString());

        SessionContext sessionContext = sessionContextMap.get(ctx.channel().id());
        StringBuilder buffer = null;
        if (sessionContext == null) {
            sessionContext = new SessionContext();
            sessionContext.setPacketBuffer(new StringBuilder());
            sessionContextMap.put(ctx.channel().id(), sessionContext);
        }
        buffer = sessionContext.getPacketBuffer();


        if (xmlstring.contains("auth"))//we cleanup the messy stuff
        {
            buffer.setLength(0);
        }

        if (xmlstring.startsWith("xmlns=") ||
                xmlstring.startsWith("type=") ||
                xmlstring.startsWith("id=") ||
                xmlstring.startsWith("ver=") ||
                xmlstring.startsWith("ext=") ||
                xmlstring.startsWith("node=") ||
                xmlstring.startsWith("version=") ||
                xmlstring.startsWith("to=") ||
                xmlstring.startsWith("from=")) {
            buffer.append(" ");
        }
        buffer.append(xmlstring.replaceAll("[\n\t]", " "));

        if (XmppStreamReader.validate(buffer.toString())) {
            objects = XmppStreamReader.read(buffer.toString(),
                    ctx.channel().id());
            buffer.setLength(0);
        } else {
            if (buffer.toString().contains("auth"))//we cleanup the messy stuff
            {
                buffer.setLength(0);
            }
            return;
        }

        for (Object obj : objects) {

            logger.info("object: " + obj.toString());

            if (obj instanceof Stream) {
                if (!sessionContext.isAuthorized()) {
                    ctx.writeAndFlush(String.format(stanzas.getProperty("start"), ((Stream) obj).getTo())
                            + stanzas.getProperty("features"));
                    sessionContext.setCtx(ctx);
                    sessionContext.setTo(((Stream) obj).getTo());
                } else if (sessionContext.isAuthorized()) {
                    ctx.writeAndFlush(String.format(stanzas.getProperty("authOk"), sessionContext.getTo()));
                    logger.info("mek1");
                    return;
                }
            }
            if (obj instanceof Auth) {

                String user = Base64Utils.decode(((Auth) obj).getValue());
                if (user != null) {
                    sessionContext.setAuthorized(true);
                    sessionContext.setUser(user);
                    ctx.writeAndFlush(stanzas.getProperty("success") + String.format(stanzas.getProperty("authOk"), sessionContext.getTo()));
                    return;
                }


            }
            if (obj instanceof Presence) {
                Presence p = (Presence) obj;
                if (p.getType() != null && p.getType().equals("unavailable")) {
                    //SessionContext c = sessionContextMap.remove(ctx.channel().id());
                    //authorizedUserChannels.remove(c.getUser() + "@" + c.getTo());
                    ctx.close();
                }
            }

            if (obj instanceof Message) {
                messageQueue.add((Message) obj);
            }

            if (obj instanceof Iq) {

                handleIQ(ctx, sessionContext, (Iq) obj);

            }
        }

    }

    private void handleIQ(ChannelHandlerContext ctx, SessionContext sessionContext, Iq obj) {
        Iq res = obj;
        if ((obj.getAny() != null) && obj.getAny() instanceof Bind &&
                obj.getType().equals("set")) {
            res.setType("result");
            Bind b = (Bind) res.getAny();
            String user = sessionContext.getUser() + "@" + sessionContext.getTo();
            String jid = user + "/" + b.getResource();
            // b.setJid();

            sessionContext.setJid(jid);
            authorizedUserChannels.put(user,
                    sessionContext.getCtx().channel().id());

            ctx.writeAndFlush(String.format(stanzas.getProperty("bindOk"), res.getId(), user, jid));

        }
        if ((obj.getAny() != null) && obj.getAny() instanceof Session) {
            if (obj.getType().equals("set")) {
                ctx.writeAndFlush("<iq type=\"result\" id=\"" + obj.getId() + "\"/>");

            }
        }
        if ((obj.getAny() != null) && obj.getAny() instanceof Query) {
            Query q = (Query) obj.getAny();
            if (obj.getType().equals("get") &&
                    q.getNamespace().endsWith(":roster")) {
                ctx.writeAndFlush(String.format(
                        userRoster.get(sessionContext.getUser()),
                        sessionContext.getJid(),
                        sessionContext.getTo(),
                        obj.getId(),
                        sessionContext.getTo()));

            }
        }
    }

    private void handleMessage(Message obj) {

        Optional thread =
                obj.getSubjectOrBodyOrThread()
                        .stream().
                        filter(i -> i instanceof ChatThread).findFirst();

        thread.ifPresent(i -> {
            String threadId = ((ChatThread) i).getValue();
            Chat c = chatMap.get(threadId);
            if (c == null) {
                c = new Chat();
                c.setThreadId(((ChatThread) i).getValue());
                Set<String> peers = new HashSet<>();
                peers.add(obj.getTo());
                peers.add(obj.getFrom());
                c.setPeers(peers);
                chatMap.put(threadId, c);

            } else {
                Set<String> peers = c.getPeers();
                peers.add(obj.getTo());
                peers.add(obj.getFrom());
                c.setPeers(peers);
                chatMap.put(threadId, c);
            }
        });


        ChannelId channelId = obj.getChannelId();
        SessionContext userFromSessionContext = sessionContextMap.get(channelId);
        ChannelId channelToId =
                authorizedUserChannels.get(obj.getTo());
        SessionContext userToSessionContext = null;
        if (channelToId != null)
            userToSessionContext =
                    sessionContextMap.get(channelToId);

        if (userFromSessionContext != null
                && userFromSessionContext.getCtx() != null) {


            logger.info(userFromSessionContext.toString());


            Optional body =
                    obj.getSubjectOrBodyOrThread()
                            .stream().
                            filter(i -> i instanceof Body).findFirst();
            Optional subj =
                    obj.getSubjectOrBodyOrThread()
                            .stream().
                            filter(i -> i instanceof Subject).findFirst();


            logger.info("body present: " + body.isPresent());
            logger.info("subj:" + subj.isPresent());

            if (subj.isPresent() || body.isPresent()) {


                if (userFromSessionContext.isAuthorized()
                        && userToSessionContext != null) {
                    //we get channelId of a sender
                    //
                    String sendFromUser =
                            userFromSessionContext.getUser()
                                    + "@" +
                                    userFromSessionContext.getTo();

                    String sendToUser = userToSessionContext.getUser() + "@" +
                            userToSessionContext.getTo();

                    if (userToSessionContext.getCtx().channel().isWritable()
                            &&
                            sendToUser.equals(obj.getTo())) {
                        String newMessage = String.format(
                                stanzas.getProperty("message"),
                                sendFromUser
                                , obj.getTo()
                                , obj.getId(),
                                subj.isPresent() ? ((Subject) subj.get()).getValue() : "",
                                body.isPresent() ?
                                        ((Body) body.get()).getValue() : "",
                                thread.isPresent() ?
                                        ((ChatThread) thread.get()).getValue() : "");

                        logger.info("NEW MESSAGE:\n " + newMessage);
                        userToSessionContext.getCtx().writeAndFlush(newMessage);

                    } else {
                        sessionContextMap.remove(userToSessionContext.getCtx().channel().id());
                        authorizedUserChannels.remove(sendToUser);
                        messageQueue.add(obj);
                        //store to be sent
                    }
                } else {
                    messageQueue.add(obj);
                }
                // return;
            }
        }
        //   }

    }

    class MessageHandler implements Runnable {

        private Message message;

        MessageHandler(Message message) {
            this.message = message;

        }

        @Override
        public void run() {
            handleMessage(message);
        }


    }


    // Here is how we send out heart beat for idle to long
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) { // idle for no read and write
                //  ctx.writeAndFlush(new LoopBackTimeStamp());
                ctx.channel().close();
            }

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}