package com.hp.hpl.guess.piccolo;

import java.io.*;
import java.util.*;
import java.awt.Dimension;

import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.datasink.*;
import javax.media.format.VideoFormat;


/**
 * This program takes a list of JPEG image files and convert them into
 * a QuickTime movie.
 */
public class JpegImagesToMovie 
    implements ControllerListener, DataSinkListener, Runnable {

    ImageDataSource ids = null;
    Processor p = null;
    DataSink dsink = null;
    
    public ImageSourceStream getISS() {
	if (ids != null) {
	    return(ids.getISS());
	} 
	return(null);
    }

    public boolean doIt(int width, int height, 
			int frameRate, MediaLocator outML) {
	ids = new ImageDataSource(width, height, frameRate);
	

	
	try {
	    p = Manager.createProcessor(ids);
	} catch (Exception e) {
	    System.err.println("Yikes!  Cannot create a processor from the data source.");
	    return false;
	}

	p.addControllerListener(this);

	// Put the Processor into configured state so we can set
	// some processing options on the processor.
	p.configure();
	if (!waitForState(p, p.Configured)) {
	    System.err.println("Failed to configure the processor.");
	    return false;
	}

	// Set the output content descriptor to QuickTime. 
	p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

	// Query for the processor for supported formats.
	// Then set it on the processor.
	TrackControl tcs[] = p.getTrackControls();
	Format f[] = tcs[0].getSupportedFormats();
	if (f == null || f.length <= 0) {
	    System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
	    return false;
	}

	tcs[0].setFormat(f[0]);

	//System.err.println("Setting the track format to: " + f[0]);

	// We are done with programming the processor.  Let's just
	// realize it.
	p.realize();
	if (!waitForState(p, p.Realized)) {
	    System.err.println("Failed to realize the processor.");
	    return false;
	}

	// Now, we'll need to create a DataSink.
	if ((dsink = createDataSink(p, outML)) == null) {
	    System.err.println("Failed to create a DataSink for the given output MediaLocator: " + outML);
	    return false;
	}

	dsink.addDataSinkListener(this);
	fileDone = false;

	//System.err.println("start processing...");

	// OK, we can now start the actual transcoding.
	try {
	    p.start();
	    dsink.start();
	} catch (IOException e) {
	    System.err.println("IO error during processing");
	    return false;
	}
	return true;
    }


    public void run() {
	//System.out.println("running...");
	waitForFileDone();
	//System.out.println("stopping...");
	// Cleanup.
	try {
	    dsink.close();
	} catch (Exception e) {}
	p.removeControllerListener(this);
    }

    /**
     * Create the DataSink.
     */
    DataSink createDataSink(Processor p, MediaLocator outML) {

	DataSource ds;

	if ((ds = p.getDataOutput()) == null) {
	    System.err.println("Something is really wrong: the processor does not have an output DataSource");
	    return null;
	}

	DataSink dsink;

	try {
	    //System.err.println("- create DataSink for: " + outML);
	    dsink = Manager.createDataSink(ds, outML);
	    dsink.open();
	} catch (Exception e) {
	    System.err.println("Cannot create the DataSink: " + e);
	    return null;
	}

	return dsink;
    }


    Object waitSync = new Object();
    boolean stateTransitionOK = true;

    /**
     * Block until the processor has transitioned to the given state.
     * Return false if the transition failed.
     */
    boolean waitForState(Processor p, int state) {
	synchronized (waitSync) {
	    try {
		while (p.getState() < state && stateTransitionOK)
		    waitSync.wait();
	    } catch (Exception e) {}
	}
	return stateTransitionOK;
    }


    /**
     * Controller Listener.
     */
    public void controllerUpdate(ControllerEvent evt) {

	if (evt instanceof ConfigureCompleteEvent ||
	    evt instanceof RealizeCompleteEvent ||
	    evt instanceof PrefetchCompleteEvent) {
	    synchronized (waitSync) {
		stateTransitionOK = true;
		waitSync.notifyAll();
	    }
	} else if (evt instanceof ResourceUnavailableEvent) {
	    synchronized (waitSync) {
		stateTransitionOK = false;
		waitSync.notifyAll();
	    }
	} else if (evt instanceof EndOfMediaEvent) {
	    evt.getSourceController().stop();
	    evt.getSourceController().close();
	}
    }


    
    Object waitFileSync = new Object();
    boolean fileDone = false;
    boolean fileSuccess = true;

    /**
     * Block until file writing is done. 
     */
    boolean waitForFileDone() {
	synchronized (waitFileSync) {
	    try {
		while (!fileDone)
		    waitFileSync.wait();
	    } catch (Exception e) {}
	}
	return fileSuccess;
    }



    /**
     * Event handler for the file writer.
     */
    public void dataSinkUpdate(DataSinkEvent evt) {

	if (evt instanceof EndOfStreamEvent) {
	    synchronized (waitFileSync) {
		fileDone = true;
		waitFileSync.notifyAll();
	    }
	} else if (evt instanceof DataSinkErrorEvent) {
	    synchronized (waitFileSync) {
		fileDone = true;
		fileSuccess = false;
		waitFileSync.notifyAll();
	    }
	}
    }

    public JpegImagesToMovie(int width, int height, 
			     int frameRate, String outputURL) {
	MediaLocator oml;
	
	if ((oml = createMediaLocator(outputURL)) == null) {
	    System.err.println("Cannot build media locator from: " + 
			       outputURL);
	} else {
	    doIt(width,height,frameRate,oml);
	}
    }

    /**
     * Create a media locator from the given string.
     */
    static MediaLocator createMediaLocator(String url) {

	MediaLocator ml;

	ml = new MediaLocator("file:"+url);
	return ml;
    }

    public static void main(String[] args) {
	new JpegImagesToMovie(639,421,30,"abc.mov");
    }


    ///////////////////////////////////////////////
    //
    // Inner classes.
    ///////////////////////////////////////////////


    /**
     * A DataSource to read from a list of JPEG image files and
     * turn that into a stream of JMF buffers.
     * The DataSource is not seekable or positionable.
     */
    class ImageDataSource extends PushBufferDataSource {

	ImageSourceStream streams[];

	ImageDataSource(int width, int height, int frameRate) {
	    streams = new ImageSourceStream[1];
	    streams[0] = new ImageSourceStream(width, height, frameRate);
	}

	public void setLocator(MediaLocator source) {
	}

	public MediaLocator getLocator() {
	    return null;
	}

	/**
	 * Content type is of RAW since we are sending buffers of video
	 * frames without a container format.
	 */
	public String getContentType() {
	    return ContentDescriptor.RAW;
	}

	public void connect() {
	}

	public void disconnect() {
	}

	public void start() {
	}

	public void stop() {
	}

	/**
	 * Return the ImageSourceStreams.
	 */
	public PushBufferStream[] getStreams() {
	    return streams;
	}

	/**
	 * We could have derived the duration from the number of
	 * frames and frame rate.  But for the purpose of this program,
	 * it's not necessary.
	 */
	public Time getDuration() {
	    return DURATION_UNKNOWN;
	}

	public Object[] getControls() {
	    return new Object[0];
	}

	public Object getControl(String type) {
	    return null;
	}

	public ImageSourceStream getISS() {
	    return((ImageSourceStream)streams[0]);
	}
    }


    /**
     * The source stream to go along with ImageDataSource.
     */
    class ImageSourceStream implements PushBufferStream {

	Vector images;
	int width, height;
	VideoFormat format;

	int nextImage = 0;	// index of the next image to be read.
	boolean ended = false;

	public ImageSourceStream(int width, int height, int frameRate) {
	    this.width = width;
	    this.height = height;

	    format = new VideoFormat(VideoFormat.JPEG,
				new Dimension(width, height),
				Format.NOT_SPECIFIED,
				Format.byteArray,
				(float)frameRate);
	}

	/**
	 * We should never need to block assuming data are read from files.
	 */
	public boolean willReadBlock() {
	    return false;
	}

	public void stopMovie() {
	    initdata = null;
	    notifyBTH();
	}

	/**
	 * This is called from the Processor to read a frame worth
	 * of video data.
	 */
 	public void read(Buffer buf) throws IOException {

	    if (initdata == null) {
		buf.setEOM(true);
		buf.setOffset(0);
		buf.setLength(0);
		ended = true;
		return;
	    }

	    byte[] data = null;

	    if (buf.getData() instanceof byte[])
		data = (byte[])buf.getData();

	    if (data == null || data.length < initdata.length) {
		data = new byte[(int)initdata.length];
		buf.setData(data);
	    }

	    // Read the entire JPEG image from the file.
	    System.arraycopy(initdata,0,data,0,initdata.length);
	    buf.setOffset(0);
	    buf.setLength(initdata.length);
	    buf.setFormat(format);
	    buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);
	}

	byte[] initdata = null;

	public void loadBuffer(byte[] initdata) {
	    this.initdata = initdata;
	    notifyBTH();
	}

	/**
	 * Return the format of each video frame.  That will be JPEG.
	 */
	public Format getFormat() {
	    return format;
	}

	public ContentDescriptor getContentDescriptor() {
	    return new ContentDescriptor(ContentDescriptor.RAW);
	}

	public long getContentLength() {
	    return 0;
	}

	public boolean endOfStream() {
	    return ended;
	}

	public Object[] getControls() {
	    return new Object[0];
	}

	public Object getControl(String type) {
	    return null;
	}

	BufferTransferHandler bth = null;

	public void setTransferHandler(BufferTransferHandler th) {
	    //System.out.println(th);
	    this.bth = th;
	}
	
	public void notifyBTH() {
	    if (bth != null) 
		bth.transferData(this);
	}
    }
}
