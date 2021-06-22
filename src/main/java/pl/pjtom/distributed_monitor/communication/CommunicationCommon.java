package pl.pjtom.distributed_monitor.communication;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import org.nustaq.serialization.FSTConfiguration;
import org.zeromq.ZContext;

import pl.pjtom.distributed_monitor.CondVar;
import pl.pjtom.distributed_monitor.Debug;
import pl.pjtom.distributed_monitor.Debug.DebugLevel;
import pl.pjtom.distributed_monitor.monitor.MonitorCommon;
import pl.pjtom.distributed_monitor.node.MyNode;
import pl.pjtom.distributed_monitor.node.OtherNode;

public class CommunicationCommon {
    private final ZContext context = new ZContext();
    private final FSTConfiguration fstConf = FSTConfiguration.createDefaultConfiguration();
    private HashMap<String, OtherNode> otherNodes = new HashMap<>();
    private MyNode myNode;
    private CondVar initialCondVar = new CondVar();


    CommunicationCommon(MonitorCommon monCom) {
        readMyIdentifier();
        readNodesFile(monCom);
    }

    void readMyIdentifier() {
        myNode = new MyNode(System.getenv("DMON_NODE_ID"));
        if (myNode.getIdentifier() == null) {
            Debug.errorPrintf(DebugLevel.NO_DEBUG, Debug.Color.RED, "DMON_NODE_ID env var is not set");
            System.exit(-1);
        }
    }

    public String getMyIdentifier() {
        return myNode.getIdentifier();
    }

    private void readNodesFile(MonitorCommon monCom) {
        String identifier;
        String address, listenOnAddress, listenPort, sendPort;
        String nodesConfigPath = System.getenv("DMON_NODES_CONFIG_PATH");
        String firstNodeId = null;
        if (nodesConfigPath == null) {
            Debug.errorPrintf(DebugLevel.NO_DEBUG, Debug.Color.RED, "DMON_NODES_CONFIG_PATH is not set");
            System.exit(-1);
        }
        File file = new File(nodesConfigPath);
        Scanner fileReader;
        try {
            fileReader = new Scanner(file);
            String data;
            String[] dataSplitted;
            while (fileReader.hasNextLine()) {
                data = fileReader.nextLine();
                if (data.startsWith("#")) {
                    continue;
                }
                dataSplitted = data.split(",");
                if (dataSplitted.length != 5) {
                    Debug.errorPrintf(DebugLevel.NO_DEBUG, Debug.Color.RED, "Incorrect line \"%s\" in %s! Skipping", data,
                            nodesConfigPath);
                    continue;
                }
                identifier = dataSplitted[0];
                if (firstNodeId == null) {
                    firstNodeId = identifier;
                }
                address = dataSplitted[1];
                listenOnAddress = dataSplitted[2];
                listenPort = dataSplitted[3];
                sendPort = dataSplitted[4];
                if (identifier.equals(myNode.getIdentifier())) {
                    myNode.init(address, listenOnAddress, listenPort, sendPort, context);
                } else {
                    otherNodes.put(identifier,
                            new OtherNode(identifier, address, listenOnAddress, listenPort, sendPort, context));
                }
            }
            fileReader.close();
            if (myNode.getAddress() == null) {
                Debug.errorPrintf(DebugLevel.NO_DEBUG, Debug.Color.RED, "Couldn't find configuration for this node (%s) in %s!",
                        myNode.getIdentifier(), nodesConfigPath);
                System.exit(-1);
            }
        } catch (FileNotFoundException e) {
            Debug.errorPrintf(DebugLevel.NO_DEBUG, Debug.Color.RED, "DMON_NODES_CONFIG_PATH doesn't exist");
            System.exit(-1);
        }
        monCom.initCSStructures(myNode, otherNodes, myNode.getIdentifier().equals(firstNodeId));
    }

    public ZContext getContext() {
        return context;
    }

    public MyNode getMyNode() {
        return myNode;
    }

    public int getOhterNodesCount() {
        return otherNodes.size();
    }

    public OtherNode getOtherNode(String identifier) {
        return otherNodes.get(identifier);
    }

    public Collection<OtherNode> getOtherNodesCollection() {
        return otherNodes.values();
    }

    public boolean shouldClose() { // TODO: Sync
        // TODO: 
        return false;
    }

    public byte[] messageToBytes(Message msg) {
        return fstConf.asByteArray(msg);
    }

    public Message bytesToMessage(byte[] data) {
        return (Message) fstConf.asObject(data);
    }

    public CondVar getInitialCondVar() {
        return initialCondVar;
    }

    public void send(Message msg, String recipientId) {
        getOtherNode(recipientId).getSendSocket().send(messageToBytes(msg));
    }

    public void broadcast(Message msg) {
        myNode.getBroadcastSendSocket().send(messageToBytes(msg));
    }

}