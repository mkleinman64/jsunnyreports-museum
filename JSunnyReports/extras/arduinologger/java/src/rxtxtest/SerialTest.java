package rxtxtest;

import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.Calendar;
import java.util.Enumeration;

public class SerialTest implements SerialPortEventListener {

    SerialPort serialPort;
    Settings settings = new Settings();
    LoggerOne loggerOne = new LoggerOne(settings);
    LoggerTwo loggerTwo = new LoggerTwo(settings);
    LoggerThree loggerThree = new LoggerThree(settings);
    LoggerFour loggerFour = new LoggerFour(settings);

    /** Buffered input stream from the port */
    private InputStream input;

    /** The output stream to the port */
    private OutputStream output;

    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;

    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 9600;

    public void initialize(String portName) {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // iterate through, looking for the port
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier)portEnum.nextElement();
            if (currPortId.getName().equals(portName.toUpperCase())) {
                portId = currPortId;
                break;
            }
        }

        if (portId == null) {
            System.out.println("Could not find COM port with name : " + portName);
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort)portId.open(this.getClass().getName(), TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            // open the streams
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                // first get the time the event was received.
                Calendar dateReceivedData = Calendar.getInstance();

                int available = input.available();
                byte chunk[] = new byte[available];
                input.read(chunk, 0, available);

                String receivedData = new String(chunk);

                // in theory the inverters *COULD* yell at exactly the same time.
                for (int i = 0; i < receivedData.length(); i++) {
                    String inverter = receivedData.substring(i, i + 1);

                    if (inverter.equals("1")) {
                        loggerOne.doLog(dateReceivedData);
                    }

                    if (inverter.equals("2")) {
                        loggerFour.doLog(dateReceivedData);
                    }

                    if (inverter.equals("3")) {
                        loggerTwo.doLog(dateReceivedData);
                    }

                    if (inverter.equals("4")) {
                        loggerThree.doLog(dateReceivedData);
                    }
                }

            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java SerialTest <comport> e.g. \"java SerialTest COM6\"");
        } else {
            SerialTest main = new SerialTest();

            String portName = args[0];
            main.initialize(portName);
            System.out.println("Started");

        }
    }
}

