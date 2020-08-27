package io.appium.uiautomator2.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import io.appium.uiautomator2.utils.Logger;
import io.appium.uiautomator2.controller.DeviceManager;
import io.appium.uiautomator2.server.LinkProtocol.Packet;

public class MControllerConnector {

    private static MControllerConnector connector = null;
    private Socket clientSock = null;
	private DataInputStream inputStream = null;
	private OutputStream outStream = null;
    private ServerSocket serverSocket = null;		

    private LinkProtocol protocol = new LinkProtocol();
    private Thread socketMonitorThread = null;
    private Thread protocolThread = null;
    private boolean bConnect = false;
	
    final int SERVER_PORT = 2013;

    private MControllerConnector() {

    }

    private void setConnect(boolean bConnect) {
        this.bConnect = bConnect;
    }

    private boolean isConnected() {
        return bConnect;
    }

    public static void open() {
        if(connector == null) {
            connector = new MControllerConnector();
        }

        connector.connect();
    }

    private void connect() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            serverSocket.setSoTimeout(15000);

            Logger.info("MControllerConnector::connect() Socket Ready");
            clientSock = serverSocket.accept();
            Logger.info("MControllerConnector::connect() Accept OK");
            inputStream = new DataInputStream(clientSock.getInputStream());
            outStream = clientSock.getOutputStream();
            
            DeviceManager.getInstance().initialize();
            setConnect(true);

            startMonitor();

            run();
        } catch(IOException e) {
            Logger.info( "MControllerConnector::connect() IOException - " + e.toString() );
            MControllerConnector.close();
        }
    }

    private void startMonitor() {
        socketMonitorThread = new Thread(new Runnable() {

            public void run() {
                Logger.info("MControllerConnector::startMonitor");

                while( !Thread.currentThread().isInterrupted() ) {
                    try {
                        Thread.sleep(1);

                        int ret = inputStream.available();
                        if(ret < 1) continue;

                        byte[] buffer = new byte[ret];

                        inputStream.readFully(buffer);
                        protocol.putRawData(buffer);
                    } catch(InterruptedException e) {
                        Logger.info( "MControllerConnector::startMonitor() InterruptedException - " + e.getMessage() );
                        break;
                    } catch(IOException e) {
                        Logger.info( "MControllerConnector::startMonitor() IOException - " + e.getMessage() );
                        break;
                    }
                }

                MControllerConnector.close();
                Logger.info("MControllerConnector::startMonitor() - End startMonitor");
            }

        });

        socketMonitorThread.start();
    }

    private void run() {
        protocolThread = new Thread(protocol);
        protocolThread.start();

        Packet packet = null;

        while( isConnected() ) {
            try {
                Thread.sleep(1);
            } catch(InterruptedException e) {
                Logger.info( "MControllerConnector::run() InterruptedException - " + e.getMessage() );
                MControllerConnector.close();
                break;
            }

            packet = protocol.getPacket();
            if(packet != null) {
                Logger.info("MControllerConnector::run Receive Data - " + packet.code);

                switch(packet.code) {
                    case LinkProtocol.PACKET_START: {

                    }
                    break;

                    case LinkProtocol.PACKET_STOP: {
                        MControllerConnector.close();
                    }
                    break;

                    case LinkProtocol.PACKET_KEY: {
                        short keyCode =  ByteBuffer.wrap(packet.data, 1, 2).getShort();
                        DeviceManager.getInstance().sendKey(keyCode, packet.data[0] == 0 ? false : true);
                    }
                    break;

                    case LinkProtocol.PACKET_TAP: {

                    }
                    break;

                    case LinkProtocol.PACKET_TOUCH_DOWN: {

                    }
                    break;

                    case LinkProtocol.PACKET_TOUCH_MOVE: {

                    }
                    break;

                    case LinkProtocol.PACKET_TOUCH_UP: {

                    }
                    break;
                }
            }
        }
    }

    public static void close() {
        if(connector != null) {
            connector.setConnect(false);

            if(connector.socketMonitorThread != null) {
                connector.socketMonitorThread.interrupt();
                connector.socketMonitorThread = null;
            }

            if(connector.protocolThread != null) {
                connector.protocolThread.interrupt();
                connector.protocolThread = null;
            }

            if(connector.inputStream != null) {
                try {
					connector.inputStream.close();
				} catch (IOException e) {
					Logger.info( "MControllerConnector::close() IOException - " + e.getMessage() );
				}

				connector.inputStream = null;
            }

            if(connector.outStream != null) {
				try {
					connector.outStream.close();
				} catch (IOException e) {
					Logger.info( "MControllerConnector::close() IOException - " + e.getMessage() );
				}

				connector.outStream = null;
            }
            
            if(connector.clientSock != null) {
				try {
					connector.clientSock.close();
				} catch (IOException e) {
					Logger.info( "MControllerConnector::close() IOException - " + e.getMessage() );
				}

				connector.clientSock = null;
            }
            
            if(connector.serverSocket != null) {
				try {
					connector.serverSocket.close();
				} catch (IOException e) {
					Logger.info( "MControllerConnector::close() IOException - " + e.getMessage() );
				}
			
				connector.serverSocket = null;
			}
			
            DeviceManager.getInstance().close();
            
            connector = null;
        }
    }

}