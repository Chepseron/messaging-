package accsms;

import java.io.IOException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.smslib.AGateway;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOrphanedMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.GatewayException;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageTypes;
import org.smslib.modem.SerialModemGateway;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;

public class WDMClient {

    Service srv = Service.getInstance();

    public void doIt(String cport) throws Exception {
        List<InboundMessage> msgList;
        InboundNotification inboundNotification = new InboundNotification();
        CallNotification callNotification = new CallNotification();
        GatewayStatusNotification statusNotification = new GatewayStatusNotification();
        OrphanedMessageNotification orphanedMessageNotification = new OrphanedMessageNotification();

        try {
            System.out.println("Example: Read messages from a serial gsm modem.");
            System.out.println(Library.getLibraryDescription());
            System.out.println("Version: " + Library.getLibraryVersion());
            SerialModemGateway gateway = new SerialModemGateway("modem.com1", cport, 9600, "ZTE", "MF667");
            gateway.setProtocol(Protocols.PDU);
            gateway.setInbound(true);
            gateway.setOutbound(true);
            this.srv.setInboundMessageNotification(inboundNotification);
            this.srv.setCallNotification(callNotification);
            this.srv.setGatewayStatusNotification(statusNotification);
            this.srv.setOrphanedMessageNotification(orphanedMessageNotification);
            this.srv.addGateway(gateway);
            this.srv.startService();
            System.out.println();
            System.out.println("Modem Information:");
            System.out.println("  Manufacturer: " + gateway.getManufacturer());
            System.out.println("  Model: " + gateway.getModel());
            System.out.println("  Serial No: " + gateway.getSerialNo());
            System.out.println("  SIM IMSI: " + gateway.getImsi());
            System.out.println("  Signal Level: " + gateway.getSignalLevel() + "%");
            System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
            System.out.println();
            msgList = new ArrayList<InboundMessage>();
            for (InboundMessage msg : msgList) {
                System.out.println(msg.getText());
                srv.deleteMessage(msg);
            }
            
            WDMWorker worker = new WDMWorker();
            worker.start();
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.srv.stopService();
        }
    }

    public class InboundNotification implements IInboundMessageNotification {

        public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
            if (msgType == MessageTypes.INBOUND) {
                System.out.println(">>> New Inbound message detected from Gateway: " + gateway.getGatewayId());
                System.out.println(msg.getText());
                String SMS = msg.getText();
                try {
                    System.out.println(SMS.substring(SMS.indexOf("2547"), SMS.indexOf("2547") + 12));
                } catch (Exception mex) {
                    mex.printStackTrace();
                }
            } else if (msgType == MessageTypes.STATUSREPORT) {
                System.out.println(">>> New Inbound Status Report message detected from Gateway: " + gateway.getGatewayId());
            }
            try {
                srv.deleteMessage(msg);
            } catch (Exception ex) {
                Logger.getLogger(WDMClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class CallNotification implements ICallNotification {

        public void process(AGateway gateway, String callerId) {
            System.out.println(">>> New call detected from Gateway: " + gateway.getGatewayId() + " : " + callerId);
        }
    }

    public class GatewayStatusNotification implements IGatewayStatusNotification {

        public void process(AGateway gateway, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
            System.out.println(">>> Gateway Status change for " + gateway.getGatewayId() + ", OLD: " + oldStatus + " -> NEW: " + newStatus);
        }
    }

    public class OrphanedMessageNotification implements IOrphanedMessageNotification {

        public boolean process(AGateway gateway, InboundMessage msg) {
            System.out.println(">>> Orphaned message part detected from " + gateway.getGatewayId());
            System.out.println(msg);
            return false;
        }
    }

    public static void main(String args[]) {
        WDMClient app = new WDMClient();
        try {
            System.out.println("COM>>> " + args[0]);
            app.doIt(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, String msisdn) {
        try {
            OutboundMessage om = new OutboundMessage(message, msisdn);
            srv.sendMessage(om);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
