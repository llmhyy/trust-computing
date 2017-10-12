package SmartGridBillingSenario;

import SmartGridBillingSenario.attack.Pcap4j;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.Packet;

import java.io.EOFException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import static SmartGridBillingSenario.utils.Utils.getTcpValue;

/**
 * Created by ydai on 8/10/17.
 */
@Slf4j
public class Pcap4jExample {

    public static void main(String[] args) throws UnknownHostException, PcapNativeException, EOFException, TimeoutException, NotOpenException {

        String host = "192.168.0.154";
        Pcap4j pcap4j = new Pcap4j(host) {
            @Override
            public void handleTcpData(String srcAddr, String dstAddr, String srcPort, String dstPort, Packet payload) {
                byte[] tcpRawData = payload.getRawData();
                if (tcpRawData.length > 4) {

                    log.info("Src = {}:{}", srcAddr, srcPort);
                    log.info("Dst = {}:{}", dstAddr, dstPort);
                    String value = getTcpValue(tcpRawData);
                    log.info("Package Aquired, value: {}", value);
                }
            }
        };

        pcap4j.startCapture();
    }
}

