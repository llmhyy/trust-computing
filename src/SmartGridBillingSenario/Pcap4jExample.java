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
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by ydai on 8/10/17.
 */
@Slf4j
public class Pcap4jExample {

    public static void main(String[] args) throws UnknownHostException, PcapNativeException, EOFException, TimeoutException, NotOpenException {
        // 获取所有网卡设备
        List<PcapNetworkInterface> alldev = Pcaps.findAllDevs();
        // 根据设备名称初始化抓包接口
        PcapNetworkInterface nif = Pcaps.getDevByName("lo0");

        // 抓取包长度
        int snaplen = 64 * 1024;
        // 超时50ms
        int timeout = 50;
        // 初始化抓包器
        PcapHandle.Builder phb = new PcapHandle.Builder(nif.getName()).snaplen(snaplen)
                .promiscuousMode(PcapNetworkInterface.PromiscuousMode.PROMISCUOUS).timeoutMillis(timeout)
                .bufferSize(1 * 1024 * 1024);
        PcapHandle handle = phb.build();
        // handle = nif.openLive(snaplen, PromiscuousMode.NONPROMISCUOUS, timeout);

        /** 设置TCP过滤规则 */
        String filter = "ip and tcp and (src host 192.168.0.154)";

        // 设置过滤器
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);


        while (true) {
            Packet packet = handle.getNextPacket();
            if (packet == null) {
                continue;
            }
            packet.getHeader();
            byte[] rawData = packet.getRawData();
// 如果抓包内容长度都小于最小硬件协议长度，则直接返回。
            if (rawData.length < 14) {
                continue;
            }

            IpV4Packet ipV4Packet = (IpV4Packet) ((BsdLoopbackPacket) packet).getPayload();
            TcpPacket tcpPacket = (TcpPacket) ipV4Packet.getPayload();


            // means have more data then header
            if (tcpPacket.getPayload() != null) {

                byte[] tcpRawData = tcpPacket.getPayload().getRawData();
                if (tcpRawData.length > 0) {
                    log.info("TCP, dataoffset {}", tcpRawData.length);
                    // byte[] dataPart = Arrays.copyOfRange(tcpRawData, tcpRawData.length - dataOffset, tcpRawData.length);
                    String hexValue = Hex.encodeHexString(tcpRawData);
                    log.info("Data with Hex value {}", hexValue);
                    String value = Utils.convertHexToString(hexValue);

                    log.info("Package Aquired, value: {}", value);
                }
            }
        }


    }
}

