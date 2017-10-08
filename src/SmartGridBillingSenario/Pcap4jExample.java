package SmartGridBillingSenario;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

import java.io.EOFException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ydai on 8/10/17.
 */
public class Pcap4jExample {

    public static void main(String[] args) throws UnknownHostException, PcapNativeException, EOFException, TimeoutException, NotOpenException {
        InetAddress addr = InetAddress.getByName("localhost");
        PcapNetworkInterface nif = Pcaps.getDevByAddress(addr);

        int snapLen = 65536;
        PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
        int timeout = 10;
        while (true) {
            PcapHandle handle = nif.openLive(snapLen, mode, timeout);

            Packet packet = handle.getNextPacketEx();
            handle.close();

            IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
            Inet4Address srcAddr = ipV4Packet.getHeader().getSrcAddr();
            System.out.println(srcAddr);
        }
    }
}
