
/**
 * Created by lidaxia on 23/06/2017.
 */
public class VectorClock {
    public int[] vc;

    VectorClock(int size) {
        vc = new int[size];
    }

    VectorClock(String s) {
        String[] timestamp = s.split(" ");
        vc = new int[timestamp.length];
        for (int i = 0; i < timestamp.length; i++) {
            vc[i] = Integer.parseInt(timestamp[i]);
        }
    }

    void Tick(int idx) {
        vc[idx]++;
    }

    int compareTo(VectorClock vcMsg) {
        for (int i = 0; i < vc.length; i++) {
            while (i < vc.length && vc[i] == vcMsg.vc[i]) {
                i++;
            }
            if (i == vc.length) break;
            if (vc[i] > vcMsg.vc[i]) {
                while (i < vc.length) {
                    if (vc[i] < vcMsg.vc[i]) return 0;
                    i++;
                }
                return 1;
            } else if (vc[i] < vcMsg.vc[i]) {
                while (i < vc.length) {
                    if (vc[i] > vcMsg.vc[i]) return 0;
                    i++;
                }
                return -1;
            }
        }
        return 1;
    }

    public String toString() {
        String res = "";
        for (int i = 0; i < vc.length-1; i++) {
            res += vc[i] + " ";
        }
        res += vc[vc.length-1];
        return res;
    }
}
