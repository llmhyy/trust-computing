package SmartGridBillingSenario;

import org.apache.commons.codec.binary.Base64;
import org.pcap4j.core.*;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.io.EOFException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by ydai on 8/10/17.
 */
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
            byte[] rawData = packet.getRawData();
// 如果抓包内容长度都小于最小硬件协议长度，则直接返回。
            if (rawData.length < 14) {
                continue;
            }

            String rawDataString = Base64.encodeBase64String(rawData);
            IpV4Packet ipV4Packet = null;
            TcpPacket tcpPacket = null;
// 由于默认过滤器过滤为IP和TCP协议包，可以直接判断rawData长度。
// 只判断IpV4协议，通过rawData数据得出IpV4头部长度。header_length标识在rawDta第15字节值，即（刨去前14位Ethernet协议长度）的后4个bit，
// 则IpV4协议头部长度，最长为4位二进制数最大值15(4bit最大值) * 4 = 60 字节（1字节为8位即rawData数组中的一个数字）
//            int ipV4HeaderLength = 0;
//            try {
//                ipV4HeaderLength =
//                        Integer.parseInt(Integer.toHexString(rawData[14]).charAt(1) + "")
//                                * 4;
//            } catch (Exception ex) {
//                continue;
//            }
            try {
                ipV4Packet = IpV4Packet.newPacket(rawData, 14, 0);
            } catch (IllegalRawDataException e) {
                e.printStackTrace();
            }

// tcpOffset 是tcp协议开始的部分，开始于Ethernet协议和IpV4协议头部之后，存在于IpV4协议数据部分里。
            int tcpOffset = 14;
// 方法解释：rawData数据， 头部的长度(按非数据内容处理)，数据的长度(整个长度-非数据内容长度)
            try {
                tcpPacket = TcpPacket.newPacket(rawData, tcpOffset, rawData.length - tcpOffset);
            } catch (IllegalRawDataException e) {
                e.printStackTrace();
            }
        }
    }
}
