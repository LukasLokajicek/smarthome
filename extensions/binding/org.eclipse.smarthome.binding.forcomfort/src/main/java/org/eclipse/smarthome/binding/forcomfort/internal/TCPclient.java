package org.eclipse.smarthome.binding.forcomfort.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPclient {

    private String host;
    private Integer port;

    private Socket socket;
    private TcpThread tcpThread;

    private Logger logger = LoggerFactory.getLogger(TCPclient.class);

    private TCPlistener listener;
    private ReconnectingThread reconnect;
    private long sleepMillis = 10000;

    private boolean keepAlive = true;

    public TCPclient(String host, Integer port, TCPlistener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public boolean connectToServer() {
        keepAlive = true;
        try {
            socket = new Socket(host, port);
            if (socket.isConnected()) {
                tcpThread = new TcpThread();
                return true;
            } else {
                logger.warn("Cannot connect to server [" + host + ":" + port + "]");
            }
        } catch (UnknownHostException e) {
            logger.warn("This host is Unknown : " + host);
            logger.debug("Stack error", e);
        } catch (IOException e) {
            logger.warn("Cannot connect to server [" + host + ":" + port + "]");
            logger.debug("Stack error", e);
        }
        if (keepAlive) {
            reconnect = new ReconnectingThread("Reconecting Thread");
            reconnect.start();
        }
        return false;
    }

    public void setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
    }

    public void sendToserver(String msg) {
        if (tcpThread != null)
            tcpThread.send(msg);
    }

    private class TcpThread extends Thread {
        private PrintWriter out;
        private BufferedReader in;

        private boolean isRunning = false;

        public TcpThread() {
            if (!isRunning)
                start();
        }

        public void send(String message) {
            if (out != null)
                out.println(message);
            else {
                logger.warn("Cannot send command - Connection is not established");
                if (keepAlive) {
                    reconnect = new ReconnectingThread("Reconecting Thread");
                    reconnect.start();
                }
            }
        }

        public void cancelConnection() {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("Cannot close TCP [" + host + ":" + port + "] connection.");
                isRunning = false;
                return;
            }
            try {
                join();
            } catch (InterruptedException e) {
                logger.debug("Cannot join TCP Thread");
            }
            logger.info("[" + host + ":" + port + "] Socket was closed");
            isRunning = false;

        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String inputLine;

                listener.changeStatus(ThingStatus.ONLINE);
                logger.info("4com4t was connected to Server [" + host + ":" + port + "]");
                isRunning = true;

                while ((inputLine = in.readLine()) != null) {
                    logger.info("[" + host + ":" + port + "] received:\n" + inputLine);

                    if (inputLine.equals("END"))
                        break;
                    listener.receivedFromServer(inputLine);
                }
                out.close();
                in.close();
                socket.close();
                logger.info("[" + host + ":" + port + "] Socket was closed");
                isRunning = false;
            } catch (IOException e) {
                if (keepAlive) {
                    logger.warn("[" + host + ":" + port + "] Error occured: ");
                    logger.debug("Stack error", e);
                    out.close();
                    try {
                        in.close();
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (keepAlive) {
                reconnect = new ReconnectingThread("Reconecting Thread");
                reconnect.start();
            }

        }

    }

    private class ReconnectingThread extends Thread {

        private boolean recconecting = false;
        private boolean run = true;

        public ReconnectingThread(String name) {
            super(name);
        }

        public void cancel() {
            try {
                run = false;
                join();
            } catch (InterruptedException e) {
                logger.info("TCP reconnecting thread was interrupted.");
            }
        }

        @Override
        public synchronized void start() {
            logger.info("Reconnecting Thread Started");
            if (!recconecting) {
                recconecting = true;
                super.start();
            }
        }

        @Override
        public void run() {
            int attempt = 0;
            while (run) {

                if (socket == null || socket.isClosed()) {

                    logger.debug("Try no. " + attempt++);
                    listener.changeStatus(ThingStatus.OFFLINE);
                    try {
                        socket = new Socket(host, port);
                    } catch (UnknownHostException e) {
                        logger.warn("This host is Unknown : " + host + "\n");
                        logger.debug("Stack error", e);
                    } catch (IOException e) {
                        logger.warn("Cannot connect to server (" + host  + ":"+ port + ") " + e.getMessage());
                    }
                }

                if (socket != null && socket.isConnected() && !socket.isClosed()) {
                    tcpThread = new TcpThread();
                    recconecting = false;
                    break;
                }

                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    logger.warn("Couldn't sleep Reconnecting Thread");
                    logger.debug("Stack error", e);
                }

            }
        }

    }

    public void disconnect() {
        keepAlive = false;
        reconnect.cancel();
        tcpThread.cancelConnection();

    }
}
