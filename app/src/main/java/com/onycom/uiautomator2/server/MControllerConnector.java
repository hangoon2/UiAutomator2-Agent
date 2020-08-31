package com.onycom.uiautomator2.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.graphics.Point;
import android.view.MotionEvent;

import com.onycom.uiautomator2.controller.DeviceManager;
import com.onycom.uiautomator2.utils.Logger;
import com.onycom.uiautomator2.server.LinkProtocol.Packet;

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
        Point pt = new Point();

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
                        DeviceManager.SettingPermission permission = DeviceManager.SettingPermission.AllSettings;
                        if(packet.dataSize == 1) {
                            int type = (int) packet.data[0];
                            if(type == 1) {
                                permission = DeviceManager.SettingPermission.PartialSettings;
                            } else if(type == 2) {
                                permission = DeviceManager.SettingPermission.None;
                            }
                        }

                        DeviceManager.getInstance().setSettingPermission(permission);

                        byte[] data = ByteBuffer.allocate(4).putInt(1).array();
                        sendResponse(data, data.length);
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
                        pt.x = ByteBuffer.wrap(packet.data, 0, 2).getShort();
                        pt.y = ByteBuffer.wrap(packet.data, 2, 2).getShort();
                        DeviceManager.getInstance().tap(pt);
                    }
                    break;

                    case LinkProtocol.PACKET_TOUCH_DOWN: {
                        pt.x = ByteBuffer.wrap(packet.data, 0, 2).getShort();
                        pt.y = ByteBuffer.wrap(packet.data, 2, 2).getShort();

                        DeviceManager.getInstance().touchDown(pt);
                    }
                    break;

                    case LinkProtocol.PACKET_TOUCH_MOVE: {
                        //  테스트 코드 이후 수정 필요 : 4개의 좌표 처리를 하도록
                        pt.x = ByteBuffer.wrap(packet.data, 0, 2).getShort();
                        pt.y = ByteBuffer.wrap(packet.data, 2, 2).getShort();

                        DeviceManager.getInstance().touchMove(pt);
                    }
                    break;

                    case LinkProtocol.PACKET_TOUCH_UP: {
                        pt.x = ByteBuffer.wrap(packet.data, 0, 2).getShort();
                        pt.y = ByteBuffer.wrap(packet.data, 2, 2).getShort();

                        DeviceManager.getInstance().touchUp(pt);
                    }
                    break;

                    case LinkProtocol.PACKET_SWIPE: {
                        pt.x = ByteBuffer.wrap(packet.data, 0, 2).getShort();
                        pt.y = ByteBuffer.wrap(packet.data, 2, 2).getShort();
                        DeviceManager.getInstance().swipe(pt,
                                new Point( ByteBuffer.wrap(packet.data, 4, 2).getShort(),
                                        ByteBuffer.wrap(packet.data, 6, 2).getShort() ) );
                    }
                    break;

                    case LinkProtocol.PACKET_MULTI_TOUCH_DOWN: {
                        Point[] points = new Point[2];
                        points[0] = new Point( ByteBuffer.wrap(packet.data, 0, 2).getShort(),
                                ByteBuffer.wrap(packet.data, 2, 2).getShort() );
                        points[1] = new Point( ByteBuffer.wrap(packet.data, 4, 2).getShort(),
                                ByteBuffer.wrap(packet.data, 6, 2).getShort() );

                        DeviceManager.getInstance().sendMotionEvent(MotionEvent.ACTION_POINTER_DOWN, points);
                    }
                    break;

                    case LinkProtocol.PACKET_MULTI_TOUCH_MOVE: {
                        Point[] points = new Point[2];
                        points[0] = new Point( ByteBuffer.wrap(packet.data, 0, 2).getShort(),
                                ByteBuffer.wrap(packet.data, 2, 2).getShort() );
                        points[1] = new Point( ByteBuffer.wrap(packet.data, 4, 2).getShort(),
                                ByteBuffer.wrap(packet.data, 6, 2).getShort() );

                        DeviceManager.getInstance().sendMotionEvent(MotionEvent.ACTION_MOVE, points);
                    }
                    break;

                    case LinkProtocol.PACKET_MULTI_TOUCH_UP: {
                        Point[] points = new Point[2];
                        points[0] = new Point( ByteBuffer.wrap(packet.data, 0, 2).getShort(),
                                ByteBuffer.wrap(packet.data, 2, 2).getShort() );
                        points[1] = new Point( ByteBuffer.wrap(packet.data, 4, 2).getShort(),
                                ByteBuffer.wrap(packet.data, 6, 2).getShort() );

                        DeviceManager.getInstance().sendMotionEvent(MotionEvent.ACTION_POINTER_UP, points);
                    }
                    break;

                    case LinkProtocol.PACKET_PORTRAIT: {
                        DeviceManager.getInstance().screenRotate(false);
                    }
                    break;

                    case LinkProtocol.PACKET_LANDSCAPE: {
                        DeviceManager.getInstance().screenRotate(true);
                    }
                    break;

                    case LinkProtocol.PACKET_TEST_HIERARCHY_DUMP: {
                        String filePath = DeviceManager.getInstance().dumpHierarchyData("scene.xml");

                        byte[] data = filePath.getBytes();
                        sendResponse(data, data.length);
                    }
                    break;

                    case LinkProtocol.PACKET_TEST_SELECT_OBJECT: {

                    }
                    break;

                    case LinkProtocol.PACKET_TEST_SEARCH_OBJECT: {

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

    private void sendResponse(byte[] data, int size) {
        byte[] buff = LinkProtocol.makeMessage(0, LinkProtocol.PACKET_COMMON_RESPONSE, size, data);
        if(outStream != null) {
            try {
                outStream.write(buff);
                outStream.flush();
                Logger.info("Send Response : " + size);
            } catch(IOException e) {
                Logger.info( "MControllerConnector::sendResponse() IOException - " + e.getMessage() );
            }
        }
    }

}