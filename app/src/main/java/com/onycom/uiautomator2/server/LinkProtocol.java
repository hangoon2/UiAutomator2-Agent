package com.onycom.uiautomator2.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.onycom.uiautomator2.utils.Logger;

public class LinkProtocol implements Runnable {

    private enum PacketPos {
        RX_PACKET_POS_START,
        RX_PACKET_POS_HEAD,
        RX_PACKET_POS_DATA,
        RX_PACKET_POS_TAIL;
    }

    public class Packet {
        short code = 0;
        int nHpNo = 0;
        int dataSize = 0;
        byte[] data = null;
        long time = 0;
    }

    /* command code - start */
	final static short PACKET_START = 1;
	/* command code - stop */
	final static short PACKET_STOP = 2;
	/* command code - keypad */
	final static short PACKET_KEY = 10;
	/* command code - tap */
	final static short PACKET_TAP = 51;
	/* command code - touch down */
	final static short PACKET_TOUCH_DOWN = 52;
	/* command code - touch up */
	final static short PACKET_TOUCH_UP = 53;
	/* command code - drag screen */
	final static short PACKET_TOUCH_MOVE = 54;
	/* command code - swipe screen */
	final static short PACKET_SWIPE = 55;
	/* command code - multi touch down */
	final static short PACKET_MULTI_TOUCH_DOWN = 56;
	/* command code - multi touch up */
	final static short PACKET_MULTI_TOUCH_UP = 57;
	/* command code - multi touch move */
	final static short PACKET_MULTI_TOUCH_MOVE = 58;
		
	/* command code - recent app key */
	final static short PACKET_RECENT_APP = 300;
	/* command code - play stop */
	final static short PACKET_PLAY_STOP = 302;
	/* command code - select object */
	final static short PACKET_TEST_SELECT_OBJECT = 303;
	/* command code - search object */
	final static short PACKET_TEST_SEARCH_OBJECT = 304;
	/* command code - dump hierarchy data */
	final static short PACKET_TEST_HIERARCHY_DUMP = 305;
	/* command code - select menu */
	final static short PACKET_TEST_SELECT_MENU = 307;
	/* rotate screen - portrait */
	final static short PACKET_PORTRAIT = 1003;
	/* rotate screen - landscape */
	final static short PACKET_LANDSCAPE = 1004;
	/* common response */
	final static short PACKET_COMMON_RESPONSE = 10001;
	/* head size of packet */
	static final int PACKET_HEAD_SIZE = 8;
	/* tail size of packet */
	static final int PACKET_TAIL_SIZE = 3;
	/* start flag of packet */
	static final byte START_FLAG = 0x7F;
	/* end flag of packet */
	static final byte END_FLAG = (byte) 0xEF;
	/* position of size within packet */
	static final int MSG_HEAD_SIZE_POS = 1;
	/* position of type within packet */
	static final int MSG_HEAD_TYPE_POS = 5;
	/* position of number within packet */
	static final int MSG_HEAD_NO_POS = 7;
	/* parsing position of packet */ 
	private PacketPos rxStreamOrder = PacketPos.RX_PACKET_POS_START;
	/* list of data received */ 
	private List<ByteBuffer> rxAry = Collections.synchronizedList(new ArrayList<ByteBuffer>());
	/* list of packet received */
	private List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>());

    public LinkProtocol() {

    }

    public void putRawData(final byte[] data) {
        rxAry.add( ByteBuffer.wrap(data) );
    }

    public final Packet getPacket() {
        Packet p = null;

        synchronized(packets) {
            if( !packets.isEmpty() ) {
                p = packets.get(0);
                packets.remove(0);
            }
        }

        return p;
    }

    public void run() {
        byte [] head = new byte [PACKET_HEAD_SIZE];
		byte [] tail = new byte [PACKET_TAIL_SIZE];
		int iPacketPos = 0, iReadPos = 0, iWrite = 0;
		Packet packet = null;
		ByteBuffer msg = null;

        rxStreamOrder = PacketPos.RX_PACKET_POS_START;
        
        while( !Thread.currentThread().isInterrupted() ) {
            try {
                Thread.sleep(1);
            } catch(InterruptedException e) {
                Logger.info( "LinkProtocol::run() InterruptedException - " + e.getMessage() );
                break;
            }

            msg = null;
			
			synchronized (rxAry) {
				if(!rxAry.isEmpty()) {
					msg = rxAry.get(0);
					rxAry.remove(0);
				}
			}
			
			if(msg == null) { 
				continue;
			}
			
            iReadPos = 0;
            
            while(iReadPos < msg.capacity()) {
				if(rxStreamOrder == PacketPos.RX_PACKET_POS_START) {
					for(; iReadPos < msg.capacity(); ++iReadPos) {
						if(msg.get(iReadPos) == LinkProtocol.START_FLAG) {
							rxStreamOrder = PacketPos.RX_PACKET_POS_HEAD;
							iPacketPos = 0;
							break;
						}
					}
				}
				
				if(rxStreamOrder == PacketPos.RX_PACKET_POS_HEAD) {
					iWrite = Math.min(LinkProtocol.PACKET_HEAD_SIZE - iPacketPos, msg.capacity() - iReadPos);
					System.arraycopy(msg.array(), iReadPos, head, iPacketPos, iWrite);
					iPacketPos += iWrite;
					iReadPos += iWrite;
					
					if(iPacketPos == LinkProtocol.PACKET_HEAD_SIZE) {
						packet = new Packet();
						packet.code = ByteBuffer.wrap(head, MSG_HEAD_TYPE_POS, 2).getShort();
						packet.nHpNo = (int)head[MSG_HEAD_NO_POS];
						packet.dataSize = ByteBuffer.wrap(head, MSG_HEAD_SIZE_POS, 4).getInt();
						packet.data = new byte[packet.dataSize];
						rxStreamOrder = PacketPos.RX_PACKET_POS_DATA;
						iPacketPos = 0;
					}
				}
				
				if(rxStreamOrder == PacketPos.RX_PACKET_POS_DATA) {
					iWrite = Math.min(packet.dataSize - iPacketPos, msg.capacity() - iReadPos);
					System.arraycopy(msg.array(), iReadPos, packet.data, iPacketPos, iWrite);
					iPacketPos += iWrite;
					iReadPos += iWrite;
					
					if(iPacketPos == packet.dataSize) {
						rxStreamOrder = PacketPos.RX_PACKET_POS_TAIL;
						iPacketPos = 0;
					}
				}
				
				if(rxStreamOrder == PacketPos.RX_PACKET_POS_TAIL) {
					iWrite = Math.min(PACKET_TAIL_SIZE - iPacketPos, msg.capacity() - iReadPos);
					System.arraycopy(msg.array(), iReadPos, tail, iPacketPos, iWrite);
					iPacketPos += iWrite;
					iReadPos += iWrite;
					
					if(iPacketPos == PACKET_TAIL_SIZE) {
						packet.time = System.currentTimeMillis();
						if(packet.code == LinkProtocol.PACKET_PLAY_STOP) {
//							DeviceManager.getInstance().setPlayStop(true);
						} else {
//							DeviceManager.getInstance().setPlayStop(false);
							
							packets.add(packet);
						}

						rxStreamOrder = PacketPos.RX_PACKET_POS_START;
					}
				}
			}
		}
		
		packets.clear();
		rxAry.clear();
    }

    public static final byte[] makeMessage(int nHpNo, short code, int dataSize, final byte[] data) {
    	ByteBuffer ret = ByteBuffer.allocate(PACKET_HEAD_SIZE + dataSize + PACKET_TAIL_SIZE);

    	ret.put(START_FLAG);
    	ret.putInt(dataSize);
    	ret.putShort(code);
    	ret.put((byte)nHpNo);
    	ret.put(data, 0, dataSize);

    	long sum = 0;
    	ret.position(1);
    	while( ret.position() < ret.capacity() - 4 ) {
    		sum += (long)ret.getShort();
		}

    	if( ret.position() != ret.capacity() - 3 ) {
    		sum += (long)(ret.get() & 0xff);
		}

    	sum = (sum >> 16) + (sum & 0xffff);
    	sum += (sum >> 16);

    	ret.putShort((short)~sum);
    	ret.put(END_FLAG);

    	return ret.array();
	}

}