package SmartGridBillingSenario;

import SmartGridBillingSenario.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.pcap4j.core.*;
import org.pcap4j.packet.BsdLoopbackPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.io.EOFException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ydai on 8/10/17.
 */
@Slf4j
public class Pcap4jExample {

    private static PcapHandle openPcap(String networkInterfaceName, int snaplen) {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(networkInterfaceName);
            if (nif == null) {
                throw new RuntimeException("Couldn't open network interface " +
                        networkInterfaceName);
            } else {
                log.info(
                        "Forward network traffic from " + nif.getName() + "(" +
                                nif.getAddresses() + ")");
            }
            return nif.openLive(snaplen,
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        } catch (PcapNativeException e) {
            log.error(
                    "Couldn't open network interface " + networkInterfaceName,
                    e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws UnknownHostException, PcapNativeException, EOFException, TimeoutException, NotOpenException {

        String deviceName = "lo0";
        // 抓取包长度
        int snaplen = 64 * 1024;

        PcapHandle handle = openPcap(deviceName, snaplen);

        String filter = "ip and tcp and (src host 192.168.0.154)";

        // 设置过滤器
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        runPcapToDispatcher(handle);
    }

    private static void runPcapToDispatcher(PcapHandle pcapHandle) {
        Thread pcapThread = new Thread() {
            public void run() {
                pcapToDispatcher(pcapHandle);
            }
        };

        pcapThread.start();
    }

    private static void pcapToDispatcher(PcapHandle pcapHandle) {
        while (true) {
            try {
                Packet packet = pcapHandle.getNextPacketEx();
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

                        byte[] tcpRawData = tcpPacket.getPayload().getRawData();
                        if (tcpRawData.length > 4) {
                            log.info("TCP, dataoffset {}", tcpRawData.length);
                            String hexValue = Hex.encodeHexString(tcpRawData);
                            log.info("Data with Hex value {}", hexValue);
                            String value = Utils.convertHexToString(hexValue).substring(3);

                            log.info("Package Aquired, value: {}", value);
                        }
                    }
                }
            } catch (PcapNativeException | EOFException | TimeoutException e) {
                continue;
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }
}

