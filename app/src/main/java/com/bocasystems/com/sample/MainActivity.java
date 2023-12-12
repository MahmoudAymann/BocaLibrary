///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// COMPANY: Boca Systems Inc.
// PROJECT: Android Tablet Printer Tester Sample Code
// RELEASE DATES:
// Version 1.0 - June 2016
// Version 1.1 - August 2016 Fixed portrait/landscape bug and added help screen
// Version 2.0 - January 2017 Implemented the use of our SDK and added an assortment of new functions
//               included download/print logos.  Also eliminated the need for PlugPDF addon.
// Version 3.0 - April 2017 Added USB & WIFI communication to the SDK.  Added auto reconnect functions for BT and USB.
//               Also enhanced the Android Tester Users Guide included as an HTML file.
// Version 4.0 - May 2018 Updated Android Studio to version 3.1.2.  Eliminated deprecated functions. Added BocaConnect
//               to handle opening USB, BT and WIFI ports, with user message to wait. Changes include removing memory
//               leaks via proper use of statics and background threads
// Version 6.0 - May 2018 Went to post on Google playstore, was told already had version 4.0, so 5.0 was used.
//               Prompt on application screen refers to V4.0, but posted as 5.0.  Am reposting as 6 after changing
//               display to say V6.0
// Version 6.1 - Oct 2019 Added a boolean FF parameter to the SDK routine SendData() to control when a <p> command is
//               sent when using WIFI to prevent packets of image data from arriving at the printer after the FF is issued.
//
// Author: Michael Hall - michael@bocasystems.com
//
// This sample project is intended to demonstrate bi-directional Bluetooth, USB and WIFI communication techniques
// between an Android tablet and a Boca Systems bluetooth printer.  This code has been developed using
// Android Studio editor, compiler and linker.  Started with Android Studio version 2.1 but was forced to upgrade
// to version 2.2 and now version 2.3.  The sample code is not intended to be a ticketting system.  It is intended
// to demonstrate how to make calls to the included Boca Systems Android SDK.  This project can also be used
// to quickly test a Boca Systems printer for functionality via BT, USB or WIFI.  The executable code is also
// posted on the Play Store and can be downloaded into an Android base tablet directly.  When the application
// is first started notice the "HELP" button in the top left corner of the main screen.  Pressing this button
// will display an HTML file know as the Android Tester Users Guide.  There is a lot of detail there not included in
// this file on how to use this tool as-is and the funtionality added with the use of the SDK.
//
// The functions demonstrated in the sample code include:
// 1. Entering and sending raw FGL text commands to the printer
// 2. Reading and sending a text file containing FGL commands to the printer.  The FGLTest text
//    files print based on the printers DPI setting.  The custom tickets print based on ticket
//    stock size.  Three custom text files are supplied for cinema, concert and receipt size stock.
//    All of these files are included in the main assests.
// 3. View & Print Raw monochrome BMP image files
// 4. Convert (to monochrome BMP image), View & Print document formats such as BMP, PDF and JPG
//    For my testing I put some BMP, PDF and JPG files in the Downloads folder on the test tablet
//    I emailed them from my PC to the email account on my tablet and saved the attachments.  The
//    specific path is /storage/emulated/0/Download.  I included a few sample files in this project
//    ZIP file downloaded from the Boca Systems website.
// 5. Read status from printer and display results
// 6. Demonstrate control of some configuration items such as ticket size, path, resolution and orientation
// 7. How to Scan for and Connect to Boca Systems printers via Bluetooth
// 8. How to establish a WIFI connection to Boca Systems printers via a wireless router on a LAN
// 9. How to establish a USB connection to Boca Systems printers via USBOTG adapter
//10. Some sample code has been added to attempt to automatically reconnect a USB or BT
//    connection that has been accidently lost. Search below for ATTEMPTING_RECONNECTION to
//    see this code.  When BT and USB connections are lost it triggers an interrupt that can be
//    detected and reacted to.
//
// The SDK routines listed below are all demonstrated in this test application
//
//  Boca Systems SDK Library Routines
//  =================================
//
// Start or end a Bluetooth session
//  BOOL OpenSessionBT(String Device, Context context);
//  void CloseSessionBT();
//
// Start or end a USB session
//  BOOL OpenSessionUSB(Context context);
//  void CloseSessionUSB();
//
// Start or end a WIFI session
//  BOOL OpenSessionWIFI(String IPAddress, Context context);
//  void CloseSessionWIFI();
//
// After it is established, verify the connection for Bluetooth, Wifi or USB.
// When still connected true will be returned else if connection lost false will be returned
//  BOOL VerifyConnectionBT()
//  BOOL VerifyConnectionUSB()
//  BOOL VerifyConnectionWIFI() - Having difficulties with this one upon release of version 3.0
//
// Alter printer configuration items from their default values
//  void ChangeConfiguration(NSString *path, int resolution, bool scaled, bool dithered, int stocksizeindex, NSString *orientation);
//
// Send string to the printer.  This can be character data to be printed on the ticket or it can be FGL commands to be executed by the printer
//  void SendString(NSString *string);
//
// Send a file to the printer for printing.  Supported file formats include suffix *.txt, *.bmp, *.png, *.jpg, *.jpeg and *.pdf
// When transmitting a text file the row and column parameters will not be used.  With any of the other file they will be used for positioning
//  bool SendFile(NSURL *filename, int row, int column);
//
// Send an image file to the printer to be stored in user space as a logo.  Supported file formats include suffix *.bmp, *.png, *.jpg, *.jpeg and *.pdf
//  bool DownloadLogo(NSURL *filename, int idnum);
//
// Print a previously downloaded logo
// When printing a logo on a ticket, the row and column parameters will be used for positioning.
//  bool PrintLogo(int idnum, int row, int column);
//
// Clear user space memory
//  void ClearMemory();
//
// Force printer to eject ticket with printed material (data, text and images) including cutting the ticket
//  void PrintCut();
//
// Force printer to eject ticket with printed material (data, text and images) without cutting the ticket
//  void PrintNoCut();
//
// Read status back from printer in the form of a string
//  NSString *ReadPrinter();
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.bocasystems.com.sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
//4.0 import com.google.android.gms.appindexing.Action;  //4.0 See onStart and onStop below
import com.google.android.gms.appindexing.AppIndex;
// import com.google.firebase.appindexing.Action;
// import com.google.firebase.appindexing.FirebaseAppIndex;
// import com.google.firebase.appindexing.FirebaseUserActions;
// import com.google.firebase.appindexing.Indexable;
// import com.google.firebase.appindexing.builders.Actions;
import com.google.android.gms.common.api.GoogleApiClient;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.*;
import java.lang.ref.WeakReference;         //4.0
import java.util.Set;
import java.util.UUID;
import android.os.Handler;
import android.os.Message;
import android.webkit.*;
import android.graphics.pdf.PdfRenderer;
import android.graphics.pdf.PdfRenderer.Page;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.os.Looper;
import java.util.ArrayList;
import bocasystems.com.sdk.BocaSystemsSDK;

import android.os.AsyncTask;                //4.0
import android.content.Context;             //4.0

import java.text.DecimalFormat;
import android.app.ActivityManager;
// import com.google.android.gms.common.api.GoogleApiClient;
// import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
// import android.support.v4.app.FragmentActivity;


public class MainActivity extends Activity
{

    final int ACTIVITY_CHOOSE_FILE = 1;
    static final int RESULT_LOAD_IMAGE = 2;            //4.0
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 3;
    final int REQUEST_PICK_FILE = 4;
    final int RESULT_DOWNLOAD_IMAGE_LOGO = 5;
    final int RESULT_DOWNLOAD_PDF_LOGO = 6;
    //final int RESULT_PRINT_LOGO = 7;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static private File selectedFile;                   //4.0

    //User Interface Stuff
    private static final int REQUEST_ENABLE_BT = 1;
    private Button Connect_Disconnect_WIFI;             //3.0
    private Button Connect_Disconnect_BT;               //3.0
    private Button Connect_Disconnect_USB;              //3.0
    private Button Transmit;
    private Button PrintTT;
    private Button DownloadLogo;
    private Button PrintLogo;
    private Button SelectFile;
    private Button Clear;
    private TextView FGL_Text;
    private TextView LogoText;
    private TextView Status;                            //4.0
    private TextView TextSize;
    private ImageView BMPImage;
    private Button Help_Html;
    private Button PrintCut;
    private Button PrintNoCut;
    private Button ClearMem;
    private TextView ConnectMessage;                    //4.0

    public BluetoothAdapter myBluetoothAdapter;
    public TextView text;
    public ListView myListView;
    public ArrayAdapter<String> BTArrayAdapter;
    public CharSequence Device;
    static public boolean connected = false;        //4.0
    public boolean ClosedConnectionOnPurpose = false;
    int STATUS_LIST_SIZE = 4;

    //Stock Sizes
    final int CONCERT = 0;          //Default stock size
    //final int CINEMA = 1;
    //final int CREDITCARD = 2;
    //final int RECEIPT = 3;
    //final int SKI = 4;
    //final int FOURBY = 5;
    //final int W1 = 6;
    //final int W2 = 7;
    //final int LETTER = 8;

    //Global State Variables and default values
    //Setting printer configuration default values based upon most common printer and stock
    int resolution = 300;                           //Default to 300 DPI
    String printerpath = "<P1>";                    //Default to path 1
    String orientation = "<LM>";                    //Default to Landscape mode
    String filetype = "BMP";                        //Default to BMP file type
    double height = 2.0;                            //Default to Concert Stock height
    double width = 5.5;                             //Default to Concert Stock width
    int stocksizeindex = CONCERT;                   //integer representing the size index currently selected default is 0 for Concert Stock (2.0 x 5.5)
    Boolean Scaled = false;                         //default to actual size, not scaled to ticket size
    Boolean Dithered = true;                        //Dither or not when converting an Image to monochrome BMP. Default to yes.
    int progress = 0;
    static String IPAddress = "10.0.2.192";         //4.0
    static String deviceselected = "";              //4.0

    //Used for converting PDF or JPG to BMP
    //When printed should the image be scaled to fit a specific ticket size or not
    String PATH = "";
    String selectedFileName = null; //file.txt
    WebView Web_Screen;
    boolean Web_Open = false;

    //This sample code will allow a connection to be established to any one of the three possible choices.
    //One at a time it is possible to connect to a Boca Systems printer via Bluetooth, USB or WIFI
    //This global variable will be used to keep track of which connection is being used.
    //The possible values are currently "BT", "USB" or "WIFI"
    static String InterfaceOfChoice = "NONE";           //4.0

    private GoogleApiClient client;                     //4.0 maybe make local see onStop and onStart


    private class MyBocaSystemsSDK extends BocaSystemsSDK {
        @Override
        public void StatusReportCallback(String statusReport) {
            if (0 < statusReport.length())
            {
                AppendStatus(statusReport);
            }
        }
        @Override
        public long getMemorySizeInBytes()
        {
            Context context = getApplicationContext();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            long totalMemory = memoryInfo.totalMem;
            return totalMemory;
        }
    }
    MyBocaSystemsSDK boca = null;
    private ArrayList<String> statusList = new ArrayList<String>();

    static public Handler mHandler;                     //4.0
    static public int MessageCode;                      //4.0
    static public int MessageIdnum;                     //4.0
    protected static final int DISPLAY_IMAGE= 0;
    protected static final int IMAGE_COMPLETE = 1;
    protected static final int SEND_FORMFEED = 2;
    protected static final int CONNECTION_LOST = 3;
    protected static final int CONNECTION_RESTORED = 4;
    protected static final int ATTEMPTING_RECONNECTION = 5;
    protected static final int DOWNLOAD_COMPLETE = 6;
    protected static final int DOWNLOAD_FAILED = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //4.0 moved to local from global
        Button Path1;
        Button Path2;
        Button Path3;
        Button Path4;
        Button DPI200;
        Button DPI300;
        Button DPI600;
        Button Portrait;
        Button Landscape;
        Button Actual;
        Button Scale;
        ListView Devices;
        SeekBar TicketSize;
        Button DitherOn;
        Button DitherOff;
        Button BMP;
        Button PDF;
        //4.0 moved to local from global

        statusList.clear();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boca = new MyBocaSystemsSDK();                //Create Boca SDK instance

        //initialize message handler
        mHandler = getHandler();                    //4.0 get an instance of message handler

        // take an instance of BluetoothAdapter - Blue tooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        }

        Connect_Disconnect_WIFI = findViewById(R.id.btnWIFI);           //3.0
        Connect_Disconnect_BT = findViewById(R.id.btnConnect);          //3.0
        Connect_Disconnect_USB = findViewById(R.id.btnUSB);             //3.0
        Transmit = findViewById(R.id.btnTransmitFGL);
        PrintTT = findViewById(R.id.btnTestTicket);
        DownloadLogo = findViewById(R.id.btnDownloadLogo);
        PrintLogo = findViewById(R.id.btnPrintLogo);
        PrintCut = findViewById(R.id.btnPrintCut);
        PrintNoCut = findViewById(R.id.btnPrintNoCut);
        SelectFile = findViewById(R.id.btnSelectFile);
        Clear = findViewById(R.id.btnClear);
        Path1 = findViewById(R.id.rbtnPath1);
        Path2 = findViewById(R.id.rbtnPath2);
        Path3 = findViewById(R.id.rbtnPath3);
        Path4 = findViewById(R.id.rbtnPath4);
        DitherOn = findViewById(R.id.rbtnDitherOn);
        DitherOff = findViewById(R.id.rbtnDitherOff);
        DPI200 = findViewById(R.id.rbtn200DPI);
        DPI300 = findViewById(R.id.rbtn300DPI);
        DPI600 = findViewById(R.id.rbtn600DPI);
        Portrait = findViewById(R.id.rbtnPortrait);
        Landscape = findViewById(R.id.rbtnLandscape);
        Actual = findViewById(R.id.rbtnActual);
        Scale = findViewById(R.id.rbtnScale);
        Devices = findViewById(R.id.lstBTDevices);
        FGL_Text = findViewById(R.id.FGLText);
        LogoText  = findViewById(R.id.LogoText);
        Status = findViewById(R.id.PrinterStatus);
        TicketSize = findViewById(R.id.sbarTicketSize);
        TextSize = findViewById(R.id.textSizeSelected);
        BMPImage = findViewById(R.id.imageView1);
        Help_Html = findViewById(R.id.btnHelp);
        Web_Screen = findViewById(R.id.BocaView);
        Web_Screen.setVisibility(View.INVISIBLE);                       //Initialize help screen as invisible
        BMP = findViewById(R.id.rbtnBMP);
        PDF = findViewById(R.id.rbtnPDF);
        ClearMem = findViewById(R.id.btnClearMemory);
        ConnectMessage = findViewById(R.id.textConnectMessage);                 //4.0
        ConnectMessage.setVisibility(View.INVISIBLE);

        int height    = Status.getHeight();
        int scrollY   = Status.getScrollY();
        Layout layout = Status.getLayout();
        if (null != layout)
        {
            int firstVisibleLineNumber = layout.getLineForVertical(scrollY);
            int lastVisibleLineNumber  = layout.getLineForVertical(scrollY+height);
            STATUS_LIST_SIZE = lastVisibleLineNumber;
        }

        //Start with all the transmission buttons disabled.  Enable them once a BlueTooth connection is made.
        Transmit.setEnabled(false);
        PrintTT.setEnabled(false);
        ClearMem.setEnabled(false);
        DownloadLogo.setEnabled(false);
        PrintLogo.setEnabled(false);
        SelectFile.setEnabled(false);
        PrintCut.setEnabled(false);
        PrintNoCut.setEnabled(false);

        //The FGL_Text data entry field serves two purposes.  It's primary use is to allow a user to enter a raw FGL string
        //of data/commands and send it to the printer.  A string such as:  <RC10,10><F11>This is a test<p>
        //followed by pressing the "TRANSMIT FGL TEXT" button, would cause a ticket to be printed
        //with the text "This is a test" at row 10, column 10, with the resident Font #11.
        //Due to limited space on the screen, it's secondary use is to allow a user to enter the IP address of the
        //Boca Systems WIFI printer just before pressing the "WIFI CONNECT" button.  Once connected the field can
        //again be used for its primary function.
        //
        //I left these commented commands below for examples
        //FGL_Text.setText("<S2>");                             //preset sample status command.  Once sent, press "READ STATUS" button to see response
        //FGL_Text.setText("<p>");                              //preset sample form feed command in text field.  This can be any FGL command for testing
        //FGL_Text.setText("<RC10,10><F11>This is a test<p>");  //preset sample test ticket.  Once printed, press "READ STATUS" button to see TICKET ACK
        //FGL_Text.setText(R.string.String_IP);                   //4.0 preset sample IP Address.  The IP Address needs to match your printer.
        //FGL_Text.setText("<SP25,50><LD1><p>");                //preset sample logo command.  Print logo #1 at row 25, column 50
        //FGL_Text.setText("10.0.2.71");                        //When testing put the IP address here so you don't have to re-enter it each test

        //preset Logo number as 1.  User can change to any value between 1 and 1000, on the GUI or here
        //The LogoText field also doubles as the test ticket count number.  To print 10 test ticket change
        //this field to 10 and then press the test ticket button
        LogoText.setText("1");

        //Set up HTML Help file from asset folder
        Help_Html.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //when the help html file is open, hide the buttons
                if(!Web_Open)
                {
                    Transmit.setVisibility(View.INVISIBLE);
                    PrintTT.setVisibility(View.INVISIBLE);
                    ClearMem.setVisibility(View.INVISIBLE);
                    DownloadLogo.setVisibility(View.INVISIBLE);
                    PrintLogo.setVisibility(View.INVISIBLE);
                    SelectFile.setVisibility(View.INVISIBLE);
                    PrintCut.setVisibility(View.INVISIBLE);
                    PrintNoCut.setVisibility(View.INVISIBLE);
                    Web_Screen.setVisibility(View.VISIBLE);
                    Web_Screen.bringToFront();
                    Web_Screen.loadUrl("file:///android_asset/help.htm");
                    Help_Html.setText(R.string.String_Close);                       //4.0
                    Web_Open = true;
                    Clear.setVisibility(View.INVISIBLE);
                    Connect_Disconnect_BT.setVisibility(View.INVISIBLE);            //3.0
                    Connect_Disconnect_USB.setVisibility(View.INVISIBLE);           //3.0
                    Connect_Disconnect_WIFI.setVisibility(View.INVISIBLE);          //3.0
                }
                //when the help html file is closed, display the buttons
                else
                {
                    Transmit.setVisibility(View.VISIBLE);
                    PrintTT.setVisibility(View.VISIBLE);
                    ClearMem.setVisibility(View.VISIBLE);
                    DownloadLogo.setVisibility(View.VISIBLE);
                    PrintLogo.setVisibility(View.VISIBLE);
                    SelectFile.setVisibility(View.VISIBLE);
                    PrintCut.setVisibility(View.VISIBLE);
                    PrintNoCut.setVisibility(View.VISIBLE);
                    Web_Screen.setVisibility(View.INVISIBLE);
                    Help_Html.setText(R.string.String_Help);                    //4.0
                    Web_Open = false;
                    Clear.setVisibility(View.VISIBLE);
                    Connect_Disconnect_BT.setVisibility(View.VISIBLE);          //3.0
                    Connect_Disconnect_USB.setVisibility(View.VISIBLE);         //3.0
                    Connect_Disconnect_WIFI.setVisibility(View.VISIBLE);        //3.0
                }
            }
        });

        //Click Scan/Connect/Disconnect button
        //Turn On Blue Tooth, Find BT devices and display them in a list
        Connect_Disconnect_BT.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), "Scan BT Button Pushed", Toast.LENGTH_LONG).show();

                if(InterfaceOfChoice.equals("BT"))
                    ClosedConnectionOnPurpose = true;
                on(view);
                find(view);
                myListView = findViewById(R.id.lstBTDevices);
                myListView.setAdapter(BTArrayAdapter);
                list(view);
            }
        });

        //3.0  Turn On Wifi.  Connect based on the IP address entered
        Connect_Disconnect_WIFI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), "Scan BT Button Pushed", Toast.LENGTH_LONG).show();

                IPAddress = FGL_Text.getText().toString();      //4.0
                InterfaceOfChoice = "WIFI";

                if (!connected)
                {
                    if(IPAddress.equals(""))
                        AppendStatus("Enter an IP Address in the FGL text field and then connect");
                    else
                        new BocaConnect(MainActivity.this).execute("",null,"");    //4.0
                }
                else
                {
                    AppendStatus("Disconnecting from WIFI... ");
                    boca.CloseSessionWIFI();
                    connected = false;
                    SetupScreen();              //4.0 enable or disable buttons on GUI as appropriate for current connection state
                }
            }
        });

        //3.0 Click Connect/Disconnect USB button
        Connect_Disconnect_USB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), "Open USB Button Pushed", Toast.LENGTH_LONG).show();

                String deviceselected = "";
                InterfaceOfChoice = "USB";

                if(!connected) {
                    Device = ((TextView) view).getText();
                    deviceselected = (String) Device;
                    new BocaConnect(MainActivity.this).execute("", null, "");          //4.0
                }
                else
                {
                    AppendStatus("Disconnecting from USB... ");
                    // boca.ReadPrinter();
                    ClosedConnectionOnPurpose = true;
                    boca.CloseSessionUSB();
                    connected = false;
                    SetupScreen();              //4.0 enable or disable buttons on GUI as appropriate for current connection state
                }
            }
        });


        //Call Boca Systems SDK routine to execute a form feed with the cut function
        PrintCut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                boca.PrintCut();
            }
        });

        //Call Boca Systems SDK routine to execute a form feed without the cut function
        PrintNoCut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                boca.PrintNoCut();
            }
        });

        //Call transmit command routine with FGL string command
        Transmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                try
//                {
                    CharSequence msg = "";
                    msg = FGL_Text.getText();
                    if (0 == msg.length())
                    {
                        msg = "<S1>";
                        FGL_Text.setText(msg);
                    }
                    String start = "<RC100,100><F11>";
                    String txt = "Hello" + "\n" + "mahmoud";
                    String end = "<p>";
                    boca.SendString(start + txt + end);
//                     String status = boca.ReadPrinter();
//                     AppendStatus(status);
//                } catch(Exception e)
//                {
//                    e.printStackTrace();
//                }
            }
        });

        //User Selected Device from the BT device list
        //Set Device string with selection made
        Devices.setOnItemClickListener(new OnItemClickListener() {

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                InterfaceOfChoice = "BT";
                if(!connected)
                {
                    Device = ((TextView) view).getText();
                    deviceselected = (String) Device;
                    new BocaConnect(MainActivity.this).execute("",null,"");          //4.0
                }
                else
                {
                    AppendStatus("Disconnecting from BT... ");
                    ClosedConnectionOnPurpose = true;
                    boca.CloseSessionBT();
                    connected = false;
                    SetupScreen();              //4.0 enable or disable buttons on GUI as appropriate for current connection state
                }
            }
        });

        //Establish Ticket Stock Size.  Used by Print Custom Ticket Button
        TicketSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                SetProgress(progress);
                //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });

        //This routine demonstrates printing a text file currently included in the Assets folder, included in the project
        //Print an FGL Test ticket based upon the printer resolution setting DPI 200, 300 or 600
        //The LogoText field can also be used as a test ticket count
        PrintTT.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //LogoText value can also be used to print numerous Test Tickets
                //should be set between 1-1000 when being used
                String test = LogoText.getText().toString();

                //Initialize ticket count to 1 in case there is a typo on the screen
                int TicketCount=1;

                if(test.matches("\\d+")) //check if only digits. Could also be text.matches("[0-9]+")
                {
                    TicketCount = Integer.parseInt(test);
                }

                //Test Range
                if((TicketCount > 1000) || (TicketCount < 1))
                {
                    TicketCount = 1;
                    Toast.makeText(MainActivity.this,  "The Test Ticket Count must be between 1-1000.  Currently set to 1.", Toast.LENGTH_SHORT).show();
                }

                for (int i = 0; i < TicketCount; i++) {

                    switch (resolution) {
                        case 600:
                            //Read 600 DPI text file and send all bytes to the printer
                            RWFile("fgltest600.txt");
                            break;

                        case 200:
                            //Read 200 DPI text file and send all bytes to the printer
                            RWFile("fgltest200.txt");
                            break;

                        default:
                            //Read 300 DPI text file and send all bytes to the printer
                            RWFile("fgltest300.txt");
                            break;
                    }
                }

            }
        });

        //Convert a PDF, BMP, JPG or PNG file to a monochrome BMP image and then download as a logo
        DownloadLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                //Download BMP, JPG or PNG image file as a logo
                if ((filetype.equals("BMP")) || (filetype.equals("JPG")) || (filetype.equals("PNG")))
                {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_DOWNLOAD_IMAGE_LOGO);
                }
                //Download PDF file as a logo
                else if (filetype.equals("PDF"))
                {
                    Intent intent = new Intent(getBaseContext(), FilePicker.class);
                    startActivityForResult(intent, RESULT_DOWNLOAD_PDF_LOGO);
                }
            }
        });

        //Print an FGL Logo previously downloaded into printer memory
        PrintLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {

                //LogoText value should be set between 1-1000 when being used
                String test = LogoText.getText().toString();

                //Initialize each of these three as default zero for testing here
                //any of these values can be set to something else, as needed
                int idnum=0;
                int row = 0;
                int col = 0;
                if(test.matches("\\d+")) //check if only digits. Could also be text.matches("[0-9]+")
                {
                    idnum = Integer.parseInt(test);
                }

                boca.PrintLogo(idnum,row,col);                          //SDK call to load logo from printer memory and then print it
                boca.PrintCut();                                        //SDK call to eject ticket and cut it
            }
        });


        //Select a BMP, JPG, PNG or PDF image file for conversion to monochrome BMP so that the image
        //Select a TXT file filled with FGL commands and printable text
        //Any of these file types can be sent to a Boca Systems printer
        //If selecting a BMP, JPG or PNG look in photo images folder
        //If selecting a PDF or TXT file look in the SDCARD directories
        SelectFile.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                if ((filetype.equals("BMP")) || (filetype.equals("JPG")) || (filetype.equals("PNG")))
                {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                }
                else if ((filetype.equals("PDF")) || (filetype.equals("TXT")))
                {
                    // String str = "<F3><HW1,1><NR><RC20,60>Test Ticket for VMP<F3><HW1,1><NR><RC60,60>LA Convention Center<F3><HW1,1><NR><RC100,60>Show Date<F6><HW1,1><NR><RC150,60>Sunday Advance Adult w/ Parking<F12><HW1,1><NR><RC260,60>$1.00<F3><HW1,1><NR><RC340,60>Ticket price includes taxes<F3><HW1,1><NR><RC410,60>Ticket #TKT# 000000000<F3><HW1,1><NR><RC450,60>Transaction #TRANS# 0000000000000<F3><HW1,1><NR><RC490,310>NO REFUNDS/ EXCHANGES<QRV2><RC220,920><QR11>{QR Code}<RC20,1480><X2><NL10>*Barcode Number*<F3><HW1,1><RC500,1510><RL>Ticket #TKT# 000000000<F6><HW1,1><NR><RC400,1550><RL>$1.00<F9><HW1,1><RC500,1610><RL>Sunday Advance Adult w/ Parking<F9><HW1,1><RC450,1300><RL>Purchased Date: Purchase Date<F9><HW1,1><RC450,1340><RL>Purchased Time: Purchase Time<p>";
                    // boca.SendString(str);
                    // The above works great!
                    // boca.RWTextFile("/storage/emulated/0/Download/SampleFGLCodeSmaller.txt");
                    Context context = getBaseContext();
                    if (null != context) {
                        Intent intent = new Intent(context, FilePicker.class);
                        if (null != intent) {
                            startActivityForResult(intent, REQUEST_PICK_FILE);
                        } else {
                            AppendStatus("Error, Intent is NULL");
                        }
                    }
                    else {
                        AppendStatus("Error, Context is NULL");
                    }
                }
            }
        });

        //Clear the Printer's Users space memory such as previously downloaded user logos and user fonts.
        //All resident logos and resident fonts installed in the factory are not in user space and will remain
        ClearMem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boca.ClearMemory();
            }
        });

        Clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                statusList.clear();
                Status.setText("");
                BMPImage.setImageBitmap(null);
            }
        });

        //turn on/off the dithering feature when printing or downloading image files
        //as the file is converted to monochrome BMP, which is what the printer understands, dithering can
        //be performed if desired
        DitherOn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Dithered = true;
                boca.ChangeConfiguration(printerpath, resolution, Scaled, Dithered, stocksizeindex, orientation);
            }
        });

        DitherOff.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Dithered = false;
                boca.ChangeConfiguration(printerpath, resolution, Scaled, Dithered, stocksizeindex, orientation);
            }
        });

        //Change file type.  Choices are Image files like BMP, JPG, PNG, PDF or text file TXT
        BMP.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                filetype = "BMP";                        //Set file type to include BMP, JPG and PNG
            }
        });

        PDF.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                filetype = "PDF";                        //Set file type to include PDF or TXT
            }
        });

        //Change printer path to path 1
        Path1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                printerpath = "<P1>";                        //Set global path variable to path 1
                boca.SendString(printerpath);                //Send FGL path command to printer
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Change printer path to path 2 (used for dual path printers)
        Path2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                printerpath = "<P2>";                        //Set global path variable to path 2
                boca.SendString(printerpath);                //Send FGL path command to printer
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Change printer path to path 3 (used for multifeed path printers)
        Path3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                printerpath = "<P3>";                        //Set global path variable to path 3
                boca.SendString(printerpath);                //Send FGL path command to printer
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Change printer path to path 4 (used for multifeed path printers)
        Path4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                printerpath = "<P4>";                        //Set global path variable to path 4
                boca.SendString(printerpath);                //Send FGL path command to printer
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Establish resolution for a 200 DPI printer
        DPI200.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resolution = 200;
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Establish resolution for a 300 DPI printer
        DPI300.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resolution = 300;
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Establish resolution for a 600 DPI printer
        DPI600.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resolution = 600;
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Set orientation portrait
        Portrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                orientation = "<PM>";            //Set Global orientation to portrait mode
                SetProgress(progress);
            }
        });

        //Set orientation landscape
        Landscape.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                orientation = "<LM>";            //Set Global orientation to Landscape mode
                SetProgress(progress);
            }
        });

        //Set orientation portrait
        Scale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Scaled = true;                  //Set image scaling to ticket size
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });

        //Set orientation landscape
        Actual.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Scaled = false;                 //Set image scaling to actual size
                boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //Read one of the text files included in Assets and send it to the printer
    private void RWFile(String file_name) {
        //InputStream inStream = null;
        BufferedInputStream bis = null;
        String msg = "";
        if (file_name.isEmpty())
        {
            return;
        }
        // AppendStatus(file_name);
        try {
            AssetManager am = getApplicationContext().getAssets();
            InputStream inStream = am.open(file_name);
            if (null == inStream)
            {
                AppendStatus("Error, could not read file");
                return;
            }

            // input stream is converted to buffered input stream
            bis = new BufferedInputStream(inStream);
            if (null == bis)
            {
                inStream.close();
                AppendStatus("Error, could not read file");
                return;
            }
            // read until a single byte is available
            while (bis.available() > 0) {
                // read next available character
                char ch = (char) bis.read();

                //Debug print the read character.
                //System.out.println("The character read = " + ch );
                msg = msg + ch;
            }
            bis.close();
            inStream.close();
            boca.SendString(msg);                //Send FGL text file to printer as a string
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Check to see if Blue tooth is enabled in the tablet
    public void on(View view)
    {
        if (!myBluetoothAdapter.isEnabled())
        {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_LONG).show();
        }
        //debug
        //else
        //{
        //Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_LONG).show();
        //}
    }


    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        int permission;

        //LogoText value should be set between 1-1000 if being used
        String test = LogoText.getText().toString();

        //Initialize each of these three as default zero for testing here
        //any of these values can be set to something else, as needed
        int idnum=0;

        if(test.matches("\\d+")) //check if only digits. Could also be text.matches("[0-9]+")
        {
            idnum = Integer.parseInt(test);
        }

        //Select a text or PDF file from any directory on the SDCARD path
        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK)
        {
            if (data.hasExtra(FilePicker.EXTRA_FILE_PATH))
            {
                selectedFile = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                selectedFileName = selectedFile.toString();
                // AppendStatus(selectedFileName);
                MessageCode = RESULT_LOAD_IMAGE;
                Toast.makeText(getApplicationContext(), "Converting/Processing Image, please wait...", Toast.LENGTH_LONG).show();
                Display_File(selectedFileName);                         //If BMP,JPG,PNG or PDF display on tablet
            }

        }

        //Select a PDF file from any directory on the SDCARD path to download as a logo
        if (requestCode == RESULT_DOWNLOAD_PDF_LOGO && resultCode == RESULT_OK)
        {
            if (data.hasExtra(FilePicker.EXTRA_FILE_PATH))
            {
                selectedFile = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                selectedFileName = selectedFile.toString();
                MessageCode = RESULT_DOWNLOAD_PDF_LOGO;
                MessageIdnum = idnum;
                Toast.makeText(getApplicationContext(), "Converting/Processing Image, please wait...", Toast.LENGTH_LONG).show();
                Display_File(selectedFileName);                         //If BMP,JPG,PNG or PDF display on tablet
            }

        }

        if (requestCode == REQUEST_EXTERNAL_STORAGE)
        {
            // Check if we have read/write permission
            permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED)
            {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }

        }

        // When an BMP, JPG or PNG Image is picked to print or download as a logo
        if ((requestCode == RESULT_LOAD_IMAGE || (requestCode == RESULT_DOWNLOAD_IMAGE_LOGO)) && resultCode == RESULT_OK && null != data)
        {
            // Check if we have write permission
            permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED)
            {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }

            // Get the Image from data
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            // Get the cursor
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgDecodableString = cursor.getString(columnIndex);
            cursor.close();

            selectedFileName = imgDecodableString;
            PATH = imgDecodableString;

            if (requestCode == RESULT_LOAD_IMAGE)
            {
                MessageCode = RESULT_LOAD_IMAGE;
                Toast.makeText(getApplicationContext(), "Converting/Processing Image, please wait...", Toast.LENGTH_LONG).show();
                Display_File(selectedFileName);                         //If BMP,JPG,PNG or PDF display on tablet
            }
            else if (requestCode == RESULT_DOWNLOAD_IMAGE_LOGO)
            {
                MessageCode = RESULT_DOWNLOAD_IMAGE_LOGO;
                MessageIdnum = idnum;
                Toast.makeText(getApplicationContext(), "Converting/Processing Image, please wait...", Toast.LENGTH_LONG).show();
                Display_File(selectedFileName);                         //If BMP,JPG,PNG or PDF display on tablet
            }

        }

        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (myBluetoothAdapter.isEnabled())
                text.setText(R.string.String_EN);           //4.0
            else
                text.setText(R.string.String_DIS);          //4.0
        }

        // Check which request we're responding to
        if (requestCode == ACTIVITY_CHOOSE_FILE)
        {
            // Make sure the request was successful
            if (resultCode == RESULT_OK)
            {
                // get the file uri
                Uri fileUri = data.getData();
                //4.0 validate uri
                if (fileUri != null) {                                  //4.0
                    // validate the file
                    String validationMssg = validateFile(fileUri);
                    if (validationMssg != null) {
                        Toast toast = Toast.makeText(getApplicationContext(), validationMssg, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        }

    }

    private ParcelFileDescriptor getSeekableFileDescriptor(String filename)
    {
        ParcelFileDescriptor fd = null;
        try {
            fd = ParcelFileDescriptor.open(new File(filename), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fd;
    }


    //4.0 static inner class doesn't hold an implicit reference to the outer class
    private static class mHandler extends Handler
    {

        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;

        private mHandler(MainActivity myClassInstance)
        {
            myClassWeakReference = new WeakReference<MainActivity>(myClassInstance);
        }

        //4.0
        //Handle Connection, disconnect, connection failure & return status read from printer
        public void handleMessage(Message msg)
        {
            //Using a weak reference means you won't prevent garbage collection
            final MainActivity mainActivity = myClassWeakReference.get();     //4.0

            super.handleMessage(msg);

            switch (msg.what)
            {
                case DISPLAY_IMAGE:
                    //not currently used
                    break;

                //Once PDF, BMP, JPG or PNG file has been converted to an image, it is displayed
                //Once displayed call send file to convert to monochrome BMP and then send to printer
                case IMAGE_COMPLETE:

                    // mainActivity.Status.setText("");
                    if (MessageCode == RESULT_LOAD_IMAGE) {
                        AppendStatus("Converting images varies depending on the size and complexity of the");
                        AppendStatus("image. If this is taking too long consider downloading the image.");
                        // AppendStatus("Converting images varies depending on the size and complexity of the");
                        // AppendStatus("image. If this is taking too long consider downloading the image ");
                        // AppendStatus("once as a logo and then using FGL to print the converted ");
                        // AppendStatus("logo/template from the printers internal memory.");

                        //Submitting this to run as a background thread since converting a color PDF, JPG, BMP or PNG
                        //to a monochrome (B&W) and then transmitting it to the printer can take a few seconds depending
                        //on the size and complexity of the image.
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                //send image file to print at row 0 and column 0.
                                //Obviously this can be changed to position an image on the ticket
                                int row = 0;
                                int col = 0;

                                mainActivity.boca.SendFile(selectedFile.getPath(), row, col);        //SDK call to send file to printer

                                if(InterfaceOfChoice != "WIFI")                             //6.1 Controlling FF on WIFI when printing images
                                    mHandler.sendEmptyMessage(SEND_FORMFEED);               //Once sent, followup with an FGL formfeed
                            }
                        }).start();

                    } else {
                        AppendStatus("Converting images varies depending on the size and complexity of the");
                        AppendStatus("image. See the printer LCD for status such as ");
                        AppendStatus("DOWNLOADING, PROGRAMMING and DOWNLOAD OK.");

                        //Submitting this to run as a background thread since converting a color PDF, JPG, BMP or PNG
                        //to a monochrome (B&W) and then transmitting it to the printer can take a few seconds depending
                        //on the size and complexity of the image.  Should the Boca Systems printer have an LCD, it will
                        //display informatioin to show the process
                        //DOWNLOADING - In the process of transmitting data and storing into printer memory
                        //PROGRAMMING - Performing Checksum and storing data about size and location of logo
                        //DOWNLOAD OK - Completed successfully and printer is ready for use again.
                        //
                        //Example : File is downloaded successfully with logo ID #1.  To print this logo enter
                        //the following FGL text command and press the "TRANSMIT FGL TEXT" button
                        //FGL Text command -->  <LD1><p>
                        //To position the logo at row 10 and column 100 include the SP command as shown below
                        //FGL Text command -->  <SP10,100><LD1><p>
                        //Please refer to the FGL users guide for a complete list of FGL commands.
                        //To program into your apllication put together the string and call SendString("<SP10,100><LD1><p>");
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                boolean status;
                                status = mainActivity.boca.DownloadLogo(selectedFile.getPath(), MessageIdnum);
                                //Once logo downloaded, notify user of results
                                if (status)
                                    mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
                                else
                                    mHandler.sendEmptyMessage(DOWNLOAD_FAILED);

                            }
                        }).start();
                    }

                    break;

                case DOWNLOAD_COMPLETE:
                    AppendStatus("DOWNLOAD OF LOGO COMPLETED SUCCESSFULLY");
                    break;

                case DOWNLOAD_FAILED:
                    AppendStatus("DOWNLOAD OF LOGO FAILED");
                    break;

                //Once data is sent to printer, send form feed to print ticket
                case SEND_FORMFEED:
                    mainActivity.boca.PrintCut();                                        //SDK call to eject ticket and cut it
                    break;

                case CONNECTION_LOST:
                    // mainActivity.Status.setText("");
                    AppendStatus("Interface Connection Lost");
                    connected = false;

                    //4.0 SetupScreen();
                    mainActivity.SetupScreen();

                    break;

                case ATTEMPTING_RECONNECTION:

                    //This piece of code is optional.  This is an attempt to automatically reconnect on the
                    //USB or BT port in the event of an accidental disconnect.  When disconnecting on either of these
                    //two ports, an interrupt is generated by the operating systen and detected by the SDK.
                    if ((InterfaceOfChoice.equals("BT")) || (InterfaceOfChoice.equals("USB"))) {
                        connected = false;

                        Context context;
                        context = mainActivity.getApplicationContext();

                        int count = 0;
                        while ((!connected) & (count < 3)) {
                            AppendStatus("Attempting to reconnect to " + InterfaceOfChoice);
                            //May want to put in a pause if more time needed when for example the printer looses power
                            //and reinitialization of the printer takes several seconds
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (InterfaceOfChoice.equals("BT"))
                                connected = mainActivity.boca.OpenSessionBT(InterfaceOfChoice, context);        //4.0
                            else
                                connected = mainActivity.boca.OpenSessionUSB(context);                          //4.0
                            count++;
                        }
                    }

                    //if the reconnect is successsful signal CONNECTION_RESTORED
                    if (connected)
                        mHandler.sendEmptyMessage(CONNECTION_RESTORED);
                    else
                        AppendStatus("Interface Connection Not Restored");

                    break;

                case CONNECTION_RESTORED:
                    AppendStatus("Interface Connection Restored");
                    connected = true;
                    //4.0 SetupScreen();
                    mainActivity.SetupScreen();
                    break;

                default:
                    break;
            }
        }

        //4.0 };

        //4.0 append text message to Printer Status area
        private void AppendStatus(String msg) {
            final MainActivity mainActivity = myClassWeakReference.get();
            if (0 == msg.trim().length())
            {
                return;
            }
            String[] status2 = msg.split("\n\r");
            String text;
            int i;
            for (i = 0; i < status2.length; i++) {
                if (0 < status2[i].trim().length())
                {
                    mainActivity.statusList.add(status2[i]);
                }
            }
            while (mainActivity.STATUS_LIST_SIZE < mainActivity.statusList.size())
            {
                mainActivity.statusList.remove(0);
            }
            StringBuilder builder = new StringBuilder();
            for (String s: mainActivity.statusList)
            {
                builder.append(s);
                builder.append("\n");
            }
            text = builder.toString();
            mainActivity.Status.setText("");
            mainActivity.Status.setText(text.substring(0, text.lastIndexOf("\n")));
        }
    }

    //4.0
    private Handler getHandler() {
        return new mHandler(this);
    }

    //This routine will constantly monitor whichever one of the three interfaces is currently open
    //If the signal is lost the user is notified on the USB and BT connections.  The WIFI is
    //not cooperating at this time.

    //I attempted to use postDelayed to control the speed of the loops and checking, to no aval
    //I also attempted to use the coundown timer code below, but could not get it to work.  I
    //am conviced this is the best way to control the speed of the checks.
    //Finally I resorted to using the Thread.sleep you see below to control the speed with which
    //the loop checks to verify a connection.  Not the best way to do this but it works.
    public void MonitorInterfaceConnection() {
        //final Handler handler = new Handler();
        //handler.postDelayed(new Runnable()
        new Thread(new Runnable()
        {
            boolean checking = true;

            @Override
            public void run()
            {
                try{

                    boolean status = true;
                    Looper.prepare();
                    while (checking)
                    {
                        //checking = start();

                        Thread.sleep(2000);

                        switch (InterfaceOfChoice) {
                            //connect USB through the OTG adapter
                            case "USB":                                                     //4.0
                                status = boca.VerifyConnectionUSB();
                                break;

                            case "BT":                                                      //4.0
                                status = boca.VerifyConnectionBT();
                                break;

                            case "WIFI":                                                    //4.0
                                status = boca.VerifyConnectionWIFI();
                                break;

                            default:
                                checking = false;
                                break;
                        }

                        if (!status) {
                            //notify user of lost connection
                            mHandler.sendEmptyMessage(CONNECTION_LOST);
                            //toggle flag to exit loop
                            checking = false;

                            //if the BT or USB connection was lost accidentally (not by user), then attempt to reconnect
                            //The flag ClosedConnectionOnPurpose is set to true if the user presses the disconnect button so
                            //there is no attempt to reconnect when the user disconnects purposely.
                            //This will only work if the problem is resolved quickly.  Should the reason be, the printer is
                            //off or the USB cable is physically disconnected for too long, then obviously, this will not work.
                            //If there is no need for automatic reconnect then comment out these two lines of code
                            if ((!ClosedConnectionOnPurpose) && ((InterfaceOfChoice.equals("BT")) || (InterfaceOfChoice.equals("USB")))) {
                                mHandler.sendEmptyMessage(ATTEMPTING_RECONNECTION);
                            }
                        }

                        }
                    //cancel();
                    Looper.loop();
                } catch (Throwable t) {
                    Log.d("halted due to an error",t.getMessage());
                }
            }

            //This CountDownTimer code was not working correctly at release time so the code above with the Thread.sleep command
            //is being used.  I left the code below in the sample because I know it is almost correct.  Should someone get this working so that the
            //Thread.sleep above is not needed, please feel free to email the fix to michael@bocasystems.com.  We are always looking
            //to improve the sample code.
            CountDownTimer countDownTimer;
            boolean runloop = true;

            public boolean start( ) {

                //countDownTimer:
                new CountDownTimer(5000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        Log.d("seconds remaining: ", String.valueOf(millisUntilFinished / 1000));
                    }

                    @Override
                    public void onFinish() {

                        boolean status = true;

                        switch (InterfaceOfChoice) {
                        //connect USB through the OTG adapter
                        case "USB":                                                     //4.0
                            status = boca.VerifyConnectionUSB();
                            break;

                        case "BT":                                                      //4.0
                            status = boca.VerifyConnectionBT();
                            break;

                        case "WIFI":                                                    //4.0
                            status = boca.VerifyConnectionWIFI();
                            break;

                        default:
                            runloop = false;
                            break;
                        }

                        if (!status) {
                            mHandler.sendEmptyMessage(CONNECTION_LOST);

                            //if the BT or USB connection was lost accidentally (not by user), then attempt to reconnect
                            //The flag ClosedConnectionOnPurpose is set to true if the user presses the disconnect button so
                            //there is no attempt to reconnect when the user disconnects purposely.
                            //This will only work if the problem is resolved quickly.  Should the reason be, the printer is
                            //off or the USB cable is physically disconnected for too long, then obviously, this will not work.
                            //If there is no need for automatic reconnect then comment out these two lines of code
                            if ((!ClosedConnectionOnPurpose) && ((InterfaceOfChoice.equals("BT")) || (InterfaceOfChoice.equals("USB")))) {
                                mHandler.sendEmptyMessage(ATTEMPTING_RECONNECTION);
                            }
                        }

                    }
                };
                return (runloop);
            }

            private void cancel(){

                if(countDownTimer != null)
                {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
            }
        }).start();


    }

    //Display on the tablet PDF, BMP, JPG and PNG image files
    //TXT files will not be displayed
    public void Display_File(final String filename)
    {
        selectedFile = (new File(filename));

        //Process pdf file to display image on tablet and then send image to printer
        if (filename.toLowerCase().endsWith("pdf"))
        {
            try
            {

                //Toast.makeText(getApplicationContext(), "Converting/Processing Image, please wait...", Toast.LENGTH_LONG).show();

                PdfRenderer renderer = new PdfRenderer(getSeekableFileDescriptor(filename));
                Page page = renderer.openPage(0);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bitmap, 0, 0, null);

                //Render PDF to BMP for showing on the screen
                page.render(bitmap, null, null, Page.RENDER_MODE_FOR_DISPLAY);
                //display color bitmap image on tablet
                BMPImage.setImageBitmap(bitmap);

                //signal image display completed
                mHandler.sendEmptyMessage(IMAGE_COMPLETE);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }   //Process bmp, jpg, png file to display image on tablet and then send image to printer
        else if (filename.toLowerCase().endsWith("bmp")  || filename.toLowerCase().endsWith("jpg") || filename.toLowerCase().endsWith("png"))
        {
            //Toast.makeText(getApplicationContext(), "Converting/Processing Image, please wait...", Toast.LENGTH_LONG).show();
            Bitmap bitmap = BitmapFactory.decodeFile(filename);
            //display color bitmap image on tablet
            BMPImage.setImageBitmap(bitmap);

            //signal image display completed
            mHandler.sendEmptyMessage(IMAGE_COMPLETE);
        }
        //Send text file to the printer
        else if (filename.toLowerCase().endsWith("txt"))
        {
            BMPImage.setImageBitmap(null);
            boca.SendFile(filename,0,0);
        }
    }

    public void SetupScreen()
    {
        if(connected)
        {
            //Enable GUI buttons since a connection was successfully made
            Transmit.setEnabled(true);
            PrintTT.setEnabled(true);
            ClearMem.setEnabled(true);
            DownloadLogo.setEnabled(true);
            PrintLogo.setEnabled(true);
            SelectFile.setEnabled(true);
            PrintCut.setEnabled(true);
            PrintNoCut.setEnabled(true);

            switch (InterfaceOfChoice) {
                //connect USB through the OTG adapter
                case "USB":                                                     //4.0
                    Connect_Disconnect_WIFI.setEnabled(false);                  //3.0
                    Connect_Disconnect_USB.setText(R.string.Button_Dis_USB);    //3.0
                    Connect_Disconnect_BT.setEnabled(false);                    //3.0
                    //Use this to know when to attempt an automatic reconnect
                    ClosedConnectionOnPurpose = false;                          //3.0 set to true, only if user clicks Close button
                    break;

                case "BT":                                                      //4.0
                    Connect_Disconnect_WIFI.setEnabled(false);                  //3.0
                    Connect_Disconnect_USB.setEnabled(false);                   //3.0
                    Connect_Disconnect_BT.setText(R.string.Button_Dis);         //3.0
                    //Use this to know when to attempt an automatic reconnect
                    ClosedConnectionOnPurpose = false;                          //3.0 set to true, only if user clicks Close button
                    break;

                case "WIFI":                                                    //4.0
                    Connect_Disconnect_WIFI.setText(R.string.Button_Dis_WIFI);  //3.0
                    Connect_Disconnect_USB.setEnabled(false);                   //3.0
                    Connect_Disconnect_BT.setEnabled(false);                    //3.0
                    break;

                default:
                    break;
            }

            AppendStatus("Connected");
            //Monitor interface connectivity
            MonitorInterfaceConnection();


        }
        else
        {
            //Disable GUI buttons since currently disconnected
            Transmit.setEnabled(false);
            PrintTT.setEnabled(false);
            ClearMem.setEnabled(false);
            DownloadLogo.setEnabled(false);
            PrintLogo.setEnabled(false);
            SelectFile.setEnabled(false);
            PrintCut.setEnabled(false);
            PrintNoCut.setEnabled(false);

            switch (InterfaceOfChoice) {
                //connect USB through the OTG adapter
                case "USB":                                                     //4.0
                    Connect_Disconnect_WIFI.setEnabled(true);                   //3.0
                    Connect_Disconnect_USB.setText(R.string.Button_Con_USB);    //3.0
                    Connect_Disconnect_BT.setEnabled(true);                     //3.0
                    break;

                case "BT":                                                      //4.0
                    Connect_Disconnect_WIFI.setEnabled(true);                   //3.0
                    Connect_Disconnect_USB.setEnabled(true);                    //3.0
                    Connect_Disconnect_BT.setText(R.string.Button_Con);         //3.0
                    break;

                case "WIFI":                                                    //4.0
                    Connect_Disconnect_WIFI.setText(R.string.Button_Con_WIFI);  //3.0
                    Connect_Disconnect_USB.setEnabled(true);                    //3.0
                    Connect_Disconnect_BT.setEnabled(true);                     //3.0
                    break;

                default:
                    break;
            }

            if(ClosedConnectionOnPurpose)
                InterfaceOfChoice = "";
            AppendStatus("Not Connected");

        }

    }

    //Check Connect/Disconnect status due to button press and and perform appropriate function
    public void list(View view)
    {
        //Toast.makeText(getApplicationContext(), "Arrived at list", Toast.LENGTH_LONG).show();
        //if not already connected to a device, display choices and attempt to connect
        if (!connected)             //4.0
        {
            final Set<BluetoothDevice> pairedDevices;
            //get paired devices
            pairedDevices = myBluetoothAdapter.getBondedDevices();

            //put it on the display list
            for (BluetoothDevice device : pairedDevices)
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Connect_Disconnect_BT.setText(R.string.Button_Con);         //3.0
            AppendStatus("Tap on a device from list to connect");

        }
        else            //connected so need to disconnect
        {
            boca.CloseSessionBT();
            Connect_Disconnect_BT.setText(R.string.Button_Scan);        //3.0
            Connect_Disconnect_USB.setEnabled(true);                    //3.0
            Connect_Disconnect_WIFI.setEnabled(true);                    //3.0
            connected = false;
            SetupScreen();                                              //4.0
        }
    }


    public void find(View view)
    {
        if (myBluetoothAdapter.isDiscovering())
        {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        else
        {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
        }
    }

    public void off(View view) {
        myBluetoothAdapter.disable();
        text.setText(R.string.String_DISC);         //4.0
        Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.bocasystems.BocaPrinter/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.bocasystems.BocaPrinter/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
*/

    //A placeholder fragment containing a simple view.
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.content_main, container, false);
            return rootView;
        }
    }

    private String validateFile(Uri uri) {
        String mssg = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        File file = new File(uri.getPath());
        // is the file readable?
        if (!file.canRead()) {
            mssg = "File selected: ".concat(uri.getPath()).concat("\nPlease, select a PDF file from your SD card");
        }
        // simple extension check
        else if (!extension.toLowerCase().equals("pdf")) {
            mssg = "File selected: ".concat(uri.getPath()).concat("\nPlease, select a PDF file from your SD card");
        }
        return mssg;
    }

    public void AppendStatus(String msg) {
        if (0 == msg.trim().length())
        {
            return;
        }
        String[] status2 = msg.split("\n\r");
        String text;
        int i;
        for (i = 0; i < status2.length; i++) {
            if (0 < status2[i].trim().length())
            {
                statusList.add(status2[i]);
            }
        }
        while (STATUS_LIST_SIZE < statusList.size())
        {
            statusList.remove(0);
        }
        StringBuilder builder = new StringBuilder();
        for (String s: statusList)
        {
            builder.append(s);
            builder.append("\n");
        }
        text = builder.toString();
        Status.setText("");
        Status.setText(text.substring(0, text.lastIndexOf("\n")));
    }

    //Used when setting Stock Size
    private void SetProgress(int progress) {

        stocksizeindex = progress;
        switch (progress) {
            case 0:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Con_L);
                    height = 2.0;
                    width = 5.5;
                } else {
                    TextSize.setText(R.string.Ticket_Con_P);
                    height = 5.5;
                    width = 2.0;
                }
                break;

            case 1:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Cin_L);
                    height = 3.25;
                    width = 2.0;
                } else {
                    TextSize.setText(R.string.Ticket_Cin_P);
                    height = 2.0;
                    width = 3.25;
                }
                break;

            case 2:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_CC_L);
                    height = 2.13;
                    width = 3.37;
                } else {
                    TextSize.setText(R.string.Ticket_CC_P);
                    height = 3.37;
                    width = 2.13;
                }
                break;

            case 3:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Rec_L);
                    height = 3.25;
                    width = 8.0;
                } else {
                    TextSize.setText(R.string.Ticket_Rec_P);
                    height = 8.0;
                    width = 3.25;
                }
                break;

            case 4:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Ski_L);
                    height = 3.25;
                    width = 6.0;
                } else {
                    TextSize.setText(R.string.Ticket_Ski_P);
                    height = 6.0;
                    width = 3.25;
                }
                break;

            case 5:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Four_L);
                    height = 4.0;
                    width = 6.0;
                } else {
                    TextSize.setText(R.string.Ticket_Four_P);
                    height = 6.0;
                    width = 4.0;
                }
                break;

            case 6:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Wst1_L);
                    height = 1.0;
                    width = 11.0;
                } else {
                    TextSize.setText(R.string.Ticket_Wst1_P);
                    height = 11.0;
                    width = 1.0;
                }
                break;

            case 7:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Wst2_L);
                    height = 1.328;
                    width = 11.0;
                } else {
                    TextSize.setText(R.string.Ticket_Wst2_P);
                    height = 11.0;
                    width = 1.328;
                }
                break;

            //Letter size not really used, but can be used as a generic size
            case 8:
                if (orientation.equals("<LM>")) {
                    TextSize.setText(R.string.Ticket_Generic_L);
                    height = 8.5;
                    width = 11.0;
                } else {
                    TextSize.setText(R.string.Ticket_Generic_P);
                    height = 11.0;
                    width = 8.5;
                }
                break;
        }
        boca.ChangeConfiguration(printerpath,resolution,Scaled,Dithered,stocksizeindex,orientation);
    }

    @Override
    public void onBackPressed()
    {
        //We have a one screen application so we will simply ignore the back button.
        //Toast.makeText(getApplicationContext(), "On Back Pressed 2", Toast.LENGTH_LONG).show();
        //super.onBackPressed();
    }

    //4.0 private static class coupled with WeakReference recommended to avoid memory leak
    private static class BocaConnect extends AsyncTask<String, Void, String > {

        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> activityReference;

        BocaConnect(MainActivity myClassInstance) {
            activityReference = new WeakReference<MainActivity>(myClassInstance);
        }


        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            MainActivity mainActivity = activityReference.get();     //4.0
            mainActivity.Status.setText("");
            mainActivity.statusList.clear();
            mainActivity.ConnectMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params){
            // No UI thread related functions here, exceptions will be raised otherwise
            return null;
        }

        @Override
        protected void onPostExecute(String device){
            super.onPostExecute(device);

            MainActivity mainActivity = activityReference.get();     //4.0
            if (mainActivity != null) {

                Context context;
                context = mainActivity.getApplicationContext();

                switch (InterfaceOfChoice) {
                    //connect USB through the OTG adapter
                    case "USB":
                        connected = mainActivity.boca.OpenSessionUSB(context);
                        break;

                    case "BT":
                        connected = mainActivity.boca.OpenSessionBT(deviceselected, context);
                        break;

                    case "WIFI":
                        connected = mainActivity.boca.OpenSessionWIFI(IPAddress, context);
                        break;

                    default:
                        break;
                }

                mainActivity.SetupScreen();              //4.0 enable or disable buttons on GUI as appropriate for current connection state
                mainActivity.ConnectMessage.setVisibility(View.INVISIBLE);
            }
        }
    }
}