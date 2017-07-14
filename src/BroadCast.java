import javafx.util.Pair;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;


/**
 * Created by lidaxia on 16/06/2017.
 */

public class BroadCast {
    private TreeNode[] tn;
    private int broadcastTimes;
    private int randomMean;
    private int messageNum;
    private TCPSampleServer server;
    private int LocalId;
    private static VectorClock vc;
    private static Semaphore MUTEX_VC = new Semaphore(1,true);
    private static ConcurrentLinkedQueue<Message> bufferedMsg = new ConcurrentLinkedQueue<>();
    private static HashMap<Integer,Pair<Integer,Integer>> ACKPath = new HashMap<>();
    private static Semaphore MUTEX_ACK = new Semaphore(1,true);
    private static Random rand = new Random();
    private static int SUM = 0;
    private static int count = 0;
    private static Semaphore MUTEX_SUM = new Semaphore(1, true);
    private ClientThread[] myClientThreads;
    private ServerThread[] myServerThreads;
    private boolean continueBC = false;
    private PrintStream fileWriter = null;

    /*
    * Construtor: get node information from config.txt.
    */
    private BroadCast(ConfigReader config, int LocalId) {
        tn = config.getTn();
        broadcastTimes = config.getBroadcastTimes();
        randomMean = config.getRandomMean();
        messageNum = broadcastTimes * config.getNumberOfNodes();
        this.LocalId = LocalId;
    }

    public static void main(String[] args) throws InterruptedException {
        ConfigReader config = ReadConfig(args[0] + "/config.txt");
        BroadCast bc = new BroadCast(config, Integer.parseInt(args[1]) - 1);
        bc.RunServer(args[0] + "/log/");
        bc.ConstructSpanningTree(bc.server.getServerSocket());
        bc.createThreads();
        bc.doBroadCast();
    }

    /*
    * Read information from config.txt.
    */
    private static ConfigReader ReadConfig(String fname) {
        String filename = fname;
        ConfigReader file = new ConfigReader();
        file.readFile(filename);
        return file;
    }

    /*
    * Create a result.txt to store messages.
    * Run server.
    */
    private void RunServer(String path) {
        try {
            File outputFile = new File(path + "result" + LocalId + "-" + tn[LocalId].getHostName() + "-" + tn[LocalId].getPort() + ".txt");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();
            fileWriter = new PrintStream(new FileOutputStream(outputFile));
            fileWriter.println("************Output Format************");
            fileWriter.println("*                                   *");
            fileWriter.println("*Send: Src|Par|VectorClock|Message  *");
            fileWriter.println("*RECV: Src|Par|VectorClock|Message  *");
            fileWriter.println("*             |VectorClock|         *");
            fileWriter.println("*                                   *");
            fileWriter.println("*************************************");
            fileWriter.println();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        server = new TCPSampleServer(tn[LocalId].getHostName(),tn[LocalId].getPort());
        server.go();
    }

    /*
    * Construct spanning tree. The second parameter is initiator.
    */
    private void ConstructSpanningTree(ServerSocket serverSock) {
        SpanningTreeConstructor spt = new SpanningTreeConstructor(LocalId, 0, this.tn);
        spt.generate(serverSock);
    }

    /*
    * Create two thread arrays.
    * ClientThreads are nodes which try to connect to this server. This server will listen to these clients later.
    * ServerThreads are nodes which will receive message from this node. This node will send message to these servers later.
    */
    private void createThreads() {
        int treeNeighboursNumber = tn[LocalId].getTreeNeighbour().size();
        Thread a = (new Thread() {
            @Override
            public void run() {
                ClientThread ct = new ClientThread(server.getServerSocket(),treeNeighboursNumber);
                ct.acceptClients();
                myClientThreads = ct.threads;
            }
        });
        a.start();
        Thread b = (new Thread() {
            @Override
            public void run() {
                ServerThread st = new ServerThread(tn, LocalId, treeNeighboursNumber);
                st.connectServers();
                myServerThreads = st.threads;
            }
        });
        b.start();
        try {
            a.join();
            b.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /*
    * Construct a vector clock filled with 0.
    * Busy checking buffer.
    * Start to listen to client thread.
    * Start to do broadcast.
    */
    private void doBroadCast() {
        vc = new VectorClock(tn.length);
        checkBuffer();
        waitForMessage();
        broadCast();
    }

    /*
    * while buffer is not empty and vl>vm, deliver message
    */
    private void checkBuffer() {
        (new Thread() {
            @Override
            public void run(){
                while (true) {
                    while (!bufferedMsg.isEmpty()) {
                        Wait(MUTEX_VC);
                        for (Message m : bufferedMsg) {
                            if (vc.compareTo(m.getVcMsg()) == 1){
                                fileWriter.println("********** READ FROM BUFFER **********");
                                deliverMsg(m);
                                bufferedMsg.remove(m);
                                break;
                            }
                        }
                        Signal(MUTEX_VC);
                    }
                }
            }
        }).start();
    }

    /*
    * Create thread for each client.
    * When the message is ACK and current node has receive all ACK from its neighbours except par, it will send ACK to par.
    * If current node is src node, it will continue to do next broadcast.
    * When the message is normal message, determine whether to deliver or buffer by its clock.
    */
    private void waitForMessage() {
        (new Thread() {
            @Override
            public void run() {
                for (ClientThread ct : myClientThreads) {
                    (new Thread() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    String message;
                                    while ((message = ct.in.readLine()) != null) {
                                        if (message.startsWith("ACK")) {
                                            String[] messageProcessing = message.split("\\|");
                                            int dst = Integer.parseInt(messageProcessing[1]);
                                            Wait(MUTEX_ACK);
                                            if (ACKPath.get(dst).getValue() == 1) {
                                                if (dst == LocalId) {
                                                    continueBC = true;
//                                                    fileWriter.println("ACK RECEIVED");
                                                } else sendACK(ACKPath.get(dst).getKey(),message);
                                            } else {
                                                ACKPath.put(dst, new Pair<>(ACKPath.get(dst).getKey(), ACKPath.get(dst).getValue() - 1));
                                            }
                                            Signal(MUTEX_ACK);
                                        } else {
                                           Message m = new Message(message);
                                           transferMsg(m);
                                           Wait(MUTEX_VC);
                                           if (vc.compareTo(m.getVcMsg()) == 1) {
                                               deliverMsg(m);
                                           } else {
                                               bufferedMsg.add(m);
                                           }
                                           Signal(MUTEX_VC);
                                       }
                                   }
                               } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }).start();
                }
            }
        }).start();
    }

    /*
    * Transfer message to its neighbours excepts par node.
    * Build a ACKPath map.
    * ACKPath map contains src, par and how many acknowledgement current node should receive before sending ACK.
    */
    private void transferMsg(Message m) {
        String message = m.getSrc()+"|" + LocalId + "|" + m.getVcMsg().toString() + "|" + m.getMsg();
        for(ServerThread st : myServerThreads) {
            if (st.serverId == m.getPar()) continue;
            st.out.println(message);
        }
        if (tn[LocalId].getTreeNeighbour().size() != 1) {
            Wait(MUTEX_ACK);
            ACKPath.put(m.getSrc(), new Pair<>(m.getPar(), tn[LocalId].getTreeNeighbour().size() - 1));
            Signal(MUTEX_ACK);
        }
    }

    /*
    * Deliver meaasge.
    * Write message to result.txt.
    * maintain vector clock.
    */
    private void deliverMsg(Message m) {
        fileWriter.println("Recv: " + m);
        fileWriter.println(String.format("%13s|%s|", "", vc.toString()));
        vc.Tick(m.getSrc());
        Wait(MUTEX_SUM);
        SUM += Integer.parseInt(m.getMsg());
        count ++;
        if (count == messageNum) {
            fileWriter.println("**********" + SUM + "**********");
            fileWriter.close();
            System.out.println("BroadCast finish. Please check result file.");
        }
        Signal(MUTEX_SUM);
        if (tn[LocalId].getTreeNeighbour().size() == 1) sendACK(m.getPar(), "ACK|" + m.getSrc());
    }

    /*
    * Send ACK.
    */
    private void sendACK(int par, String message) {
        for(ServerThread st : myServerThreads) {
            if (st.serverId != par) continue;
            st.out.println(message);
        }
    }

    private void broadCast() {
        while (broadcastTimes > 0) {
            autoBroadCast();
            try {
                Thread.sleep(getNext());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            broadcastTimes--;
        }
    }

    private void autoBroadCast() {
        //src|par|VectorClock|message
        String input = "" + (int)(Math.random() * 100 + 1);
        Wait(MUTEX_VC);
        String message = LocalId + "|" + LocalId + "|" + vc.toString() + "|" + input;
        fileWriter.println("Send: " + String.format("%-3s|%-3s|%s|%-4s", LocalId + "", LocalId + "", vc.toString(), input));
        vc.Tick(LocalId);
        Signal(MUTEX_VC);
        Wait(MUTEX_ACK);
        ACKPath.put(LocalId, new Pair<>(LocalId, tn[LocalId].getTreeNeighbour().size()));
        Signal(MUTEX_ACK);
        for(ServerThread st : myServerThreads) {
            st.out.println(message);
        }
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (continueBC) break;
        }
        continueBC = false;
        Wait(MUTEX_SUM);
        SUM += Integer.parseInt(input);
        count++;
        if (count == messageNum) {
            fileWriter.println("**********" + SUM + "**********");
            fileWriter.close();
            System.out.println("BroadCast finish. Please check result file.");
        }
        Signal(MUTEX_SUM);
    }

    private long getNext() {
        return  (long)(Math.log(1- rand.nextDouble())/(-(1.0 / randomMean)));
    }

    private void Wait(Semaphore s) {
        try
        {
            s.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void Signal(Semaphore s) {
        s.release();
    }
}