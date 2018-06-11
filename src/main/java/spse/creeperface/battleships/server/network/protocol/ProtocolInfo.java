package spse.creeperface.battleships.server.network.protocol;

/**
 * @author CreeperFace
 */
public class ProtocolInfo {

    public static final int PROTOCOL_VERSION = 1;

    public static final int CONNECTION_REQUEST = 0x01;
    public static final int CONNECTION_ACCEPTED = 0x02;
    public static final int GAME_INFO = 0x03;
    public static final int GAME_STATE_CHANGE = 0x04;
    public static final int GAME_TURN = 0x05;
    public static final int LOGOUT = 0x06;
}
