package rdp.gold.brute.rdp;

public class ServerPacketSniffer extends PacketSniffer {
    private static final PacketSniffer.Pair[] serverRegexps = { new PacketSniffer.Pair("Server FastPath update", "04"), new PacketSniffer.Pair("Server X224ConnectionRequest", "03 00 XX XX 0E D0"),
            new PacketSniffer.Pair("Server MCSConnectResponse", "03 00 XX XX 02 F0 80 7F 66 5A"), new PacketSniffer.Pair("Server AttachUserConfirm", "03 00 XX XX 02 F0 80 2E"),
            new PacketSniffer.Pair("Server ChannelJoinConfirm", "03 00 XX XX 02 F0 80 3E"), new PacketSniffer.Pair("Server ErrorAlert", "03 00 XX XX 02 F0 80 68 00 01 03 EB 70 14 80 00"),
            new PacketSniffer.Pair("Server DemandActivePDU", "03 00 XX XX 02 F0 80 68 00 01 03 EB 70 XX XX XX XX 11"),
            new PacketSniffer.Pair("Server ControlPDU", "03 00 XX XX 02 F0 80 68 00 01 03 EB 70 XX XX XX 17 00 EA 03 EA 03 XX 00 XX XX XX XX 14"),
            new PacketSniffer.Pair("Server SynchronizePDU", "03 00 XX XX 02 F0 80 68 00 01 03 EB 70 XX XX XX 17 00 EA 03 EA 03 XX 00 XX XX XX XX 1F"),
            new PacketSniffer.Pair("Server FontMapPDU", "03 00 XX XX 02 F0 80 68 00 01 03 EB 70 XX XX XX 17 00 EA 03 EA 03 XX 00 XX XX XX XX 28"),
            new PacketSniffer.Pair("Server SET_ERROR_INFO_PDU", "03 00 XX XX 02 F0 80 68 00 01 03 EB 30 XX XX XX 17 00 00 00 EA 03 XX 00 XX XX XX XX 2F"),
            new PacketSniffer.Pair("Server DeactivateAllPDU", "03 00 XX XX 02 F0 80 68 00 01 03 EB 70 XX XX XX 16 00"), new PacketSniffer.Pair("Server CloseConnection", "03 00 00 09 02 F0 80 21 80"),
            new PacketSniffer.Pair("Server CredSSP", "30"), new PacketSniffer.Pair("Server a TPKT packet", "03"), new PacketSniffer.Pair("Server a FastPath packet", "00") };

    public ServerPacketSniffer(String id) {
        super(id, serverRegexps);
    }
}
