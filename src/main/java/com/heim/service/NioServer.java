package com.heim.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NioServer.class);
    static String text = "<stream:stream" +
            "    xmlns='jabber:client'" +
            "    xmlns:stream='http://etherx.jabber.org/streams'" +
            "    from='localhost'" +
            "    id='c2s_234'" +
            "    version='1.0'>" +
            "<stream:features>" +
            "  <mechanisms xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>" +
            "    <mechanism>PLAIN</mechanism>" +
            "  </mechanisms>" +
            "</stream:features>";
    static String res1 = "<stream:stream" +
            "xmlns='jabber:server'" +
            "xmlns:stream='http://etherx.jabber.org/streams'" +
            "from='localhost'" +
            "id='s2s_123'" +
            "version='1.0'>";
    private final int port;
    private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to MsgServer!\n".getBytes());
    private volatile byte[] headerChunk = null;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public NioServer(int port) throws IOException {
        this.port = port;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String[] args) throws IOException {
        NioServer server = new NioServer(5222);
        (new Thread(server)).start();
    }

    public void run() {
        try {
            logger.info("Server starting on port " + this.port);

            Iterator<SelectionKey> iter;
            SelectionKey key;
            while (this.serverSocketChannel.isOpen()) {
                selector.select();
                iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        this.handleAccept(key);
                    }
                    if (key.isReadable()) {
                        this.handleRead(key);
                    }

                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private ByteBuffer welcome() {
        return ByteBuffer.wrap(text.getBytes());
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        String address = (new StringBuilder(sc.socket().getInetAddress().toString()))
                .append(":").append(sc.socket().getPort()).toString();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, address);
        //  sc.write(welcomeBuf);
        //  welcomeBuf.rewind();
        logger.info("accepted connection from: " + address);

    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        readHeader(ch);

    }

    private void readHeader(SocketChannel ch) throws IOException {
        int read;

        try {

            ByteBuffer body = ByteBuffer.allocate(512);

            while ((read = ch.read(body)) > 0) {
                logger.info("read bytes : " + read);
                body.flip();
                byte chunk[] = new byte[read];
                body.get(chunk);
                System.out.println(new String(body.array()).trim());
                ch.write(welcome());
            }

            // logger.info(messageMap);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}