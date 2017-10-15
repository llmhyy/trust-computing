package SmartGridBillingSenario.attack;

import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.*;
import org.pcap4j.packet.BsdLoopbackPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ydai on 12/10/17.
 */
@Slf4j
public abstract class Pcap4j {

    private static final String networkInterfaceName = "lo0";

    private static int snaplen = 1024 * 1024;

    private Thread pcapSnapThread;

    private PcapHandle handle;

    private String host;

    public Pcap4j(String host) {
        this.host = host;

        handle = openPcap();
    }

    public void startCapture() {
        if (handle != null) {
            runPcapToDispatcher();
        } else {
            log.error("No Valid Handle!!!");
        }
    }

    private void runPcapToDispatcher() {
        pcapSnapThread = new Thread() {
            public void run() {
                pcapToDispatcher();
            }
        };

        pcapSnapThread.start();
    }

    private synchronized void pcapToDispatcher() {
        while (true) {
            try {
                Packet packet = handle.getNextPacketEx();
                if (packet != null) {
                    packet.getHeader();
                    byte[] rawData = packet.getRawData();
                    if (rawData.length < 14) {
                        continue;
                    }

                    IpV4Packet ipV4Packet = (IpV4Packet) ((BsdLoopbackPacket) packet).getPayload();
                    TcpPacket tcpPacket = (TcpPacket) ipV4Packet.getPayload();


                    // means have more data then header
                    if (tcpPacket.getPayload() != null) {
                        String srcAddr = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
                        String dstAddr = ipV4Packet.getHeader().getDstAddr().getHostAddress();
                        String srcPort = tcpPacket.getHeader().getSrcPort().valueAsString();
                        String dstPort = tcpPacket.getHeader().getDstPort().valueAsString();
                        handleTcpData(srcAddr, dstAddr, srcPort, dstPort, tcpPacket.getPayload());
                    }
                }
            } catch (PcapNativeException | EOFException | TimeoutException e) {
                continue;
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload);

    private PcapHandle openPcap() {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(networkInterfaceName);
            if (nif == null) {
                throw new RuntimeException("Couldn't open network interface " +
                        networkInterfaceName);
            } else {
                log.info(
                        "Forward network traffic from {} ({})", nif.getName(), nif.getAddresses());
            }
            PcapHandle resultHandle = nif.openLive(snaplen,
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 20);

            String filter = "ip and tcp and (src host " + host + ")";

            // 设置过滤器
            resultHandle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
            return resultHandle;
        } catch (PcapNativeException | NotOpenException e) {
            log.error(
                    "Couldn't open network interface " + networkInterfaceName,
                    e);
            return null;
        }
    }


}
