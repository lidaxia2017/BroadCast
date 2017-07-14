/**
 * Created by lidaxia on 23/06/2017.
 */
public class Message {
    private int src;
    private int par;
    private VectorClock vcMsg;
    private String msg;

    //src|par|VectorClock|message
    Message(String m) {
        String[] messageProcessing = m.split("\\|");
        src = Integer.parseInt(messageProcessing[0]);
        par = Integer.parseInt(messageProcessing[1]);
        vcMsg = new VectorClock(messageProcessing[2]);
        msg = messageProcessing[3];
    }

    VectorClock getVcMsg() {
        return vcMsg;
    }

    int getSrc() {
        return src;
    }

    int getPar() {
        return par;
    }

    String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return String.format("%-3s|%-3s|%s|%-4s", src + "", par + "", vcMsg.toString(), msg);
    }
}
