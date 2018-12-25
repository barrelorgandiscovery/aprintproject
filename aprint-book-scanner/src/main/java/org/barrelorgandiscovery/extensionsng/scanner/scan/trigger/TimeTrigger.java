package org.barrelorgandiscovery.extensionsng.scanner.scan.trigger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.barrelorgandiscovery.extensionsng.scanner.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.scan.IWebCamListener;
import org.barrelorgandiscovery.tools.Disposable;

import com.github.sarxos.webcam.Webcam;

public class TimeTrigger extends Trigger implements Disposable {

  private ScheduledExecutorService e = null;

  private double seconds = Double.NaN;

  public TimeTrigger(Webcam webcam, IWebCamListener listener, PerfoScanFolder psf, double seconds) {
    super(webcam, listener, psf);
    this.seconds = seconds;
  }

  @Override
  public void start() {
    dispose();
    e = Executors.newSingleThreadScheduledExecutor();
    long time = (long) seconds * 1000;
    assert time > 0;
    e.scheduleWithFixedDelay(webCamPictureTaker, time, time, TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() {
    dispose();
  }

  @Override
  public void dispose() {
    if (e != null) {
      e.shutdownNow();
    }
    e = null;
  }
}
