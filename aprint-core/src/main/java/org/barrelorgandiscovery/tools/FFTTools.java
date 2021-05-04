package org.barrelorgandiscovery.tools;

import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;

import gervill.FFT;

/**
 * FFT Tools
 * 
 * @author use
 * 
 */
public class FFTTools {

	private static Logger logger = Logger.getLogger(FFTTools.class);

	/**
	 * Compute the nearest pow of 2 number from the number given in parameter
	 * 
	 * @param len
	 * @return
	 */
	public static int computeNearestPow2(int len) {
		return (int) computeNearestPow2((long) len);
	}

	public static long computeNearestPow2(long len) {
		double l = Math.log(len / 2) / Math.log(2);
		logger.debug("l : " + l);

		long pow = (int) Math.pow(2, 1.0 * ((int) l));

		return pow;
	}

	public static double mainFreq(double[] d, float framerate) throws Exception {

		int pow = computeNearestPow2(d.length);

		FFT fft = new FFT(pow, 1);
		fft.transform(d);

		double max = -Double.MAX_VALUE;

		int maxi = 0;
		for (int i = 0; i < 2 * pow; i += 2) {

			double re = d[i];
			double im = d[i + 1];

			// valeur de la norme
			double norm = Math.sqrt(Math.pow(re, 2) + Math.pow(im, 2));

			max = Math.max(norm, max);
			if (max == norm)
				maxi = i / 2;

			// System.out.println(i +" : " + d[i]);
		}

		logger.debug("max i:" + maxi + " , norm :" + max + ", pow " + pow / 2);

		int index = Math.abs(maxi / 2 - (pow / 2));

		logger.debug(" -> maxi - pow : " + index);

		// calcul de la frequence principale ...
		double freq = framerate / (pow / 2) * ((pow / 2) - maxi / 2);

		return freq;

	}

	/**
	 * 
	 * Convert a 16 bit sample frame to a real / imaginary array for the FFT
	 * computation
	 * 
	 * @param b
	 * @return
	 */
	public static double[] createRealAndImaginaryArray(byte[] b)
			throws Exception {

		if (b.length % 2 != 0) {
			throw new Exception("length must be an even number");
		}

		double[] d = new double[b.length];
		for (int i = 0; i < b.length / 2; i++) {
			byte b1 = b[2 * i];
			byte b2 = b[2 * i + 1];
			double e = (1.0 * b1 + 1.0 * 128 * b2) / Short.MAX_VALUE;
			d[2 * i] = e; // partie reelle
			d[2 * i + 1] = 0;
		}
		return d;
	}

	private static int MAXFRAME = computeNearestPow2(2050000);

	private static long computeFrameLength(AudioInputStream ais)
			throws Exception {
		byte[] buffer = new byte[10000];
		long l = 0;

		int cpt = 0;
		while ((cpt = ais.read(buffer)) == buffer.length) {
			l += cpt;
		}

		l += cpt;
		
		logger.debug("computeframelength :"  + l /2);
		
		return l / 2;
	}

	public static int findMidiNote(ManagedAudioInputStream mais)
			throws Exception {

		mais.reset();

		logger.debug("frame size :" + mais.getFormat().getFrameSize());

		long frameLength = computeFrameLength(mais) * 2;

		logger.debug("FrameLength :" + frameLength);

		long f = computeNearestPow2(frameLength * 2);

		logger.debug("FFT frame size :" + f);

		ArrayList<Double> d = new ArrayList<Double>();

		if (f > MAXFRAME)
			f = MAXFRAME;

		int it = (int) (frameLength / f);
		for (int i = 0; i < it; i++) {
			byte[] buffer = new byte[(int) f];
			mais.reset();
			int cpt = mais.read(buffer);

			assert buffer.length == cpt;

			double[] b = createRealAndImaginaryArray(buffer);
			double freq = mainFreq(b, mais.getFormat().getFrameRate());

			logger.debug("freq :" + freq);
			d.add(freq);
		}

		// last ...

		double mean = 0;
		for (Iterator iterator = d.iterator(); iterator.hasNext();) {
			Double double1 = (Double) iterator.next();
			mean += double1.doubleValue();
		}

		double meanfreq = mean / d.size();

		double maxdist = Double.MAX_VALUE;
		int maxint = -1;
		for (int i = 0; i < 128; i++) {
			double h = MidiHelper.hertz(i);

			double dist = Math.pow((h - meanfreq), 2.0);
			if (dist < maxdist) {
				maxdist = dist;
				maxint = i;
			}

		}

		logger.debug("freq found :" + MidiHelper.getMidiNote(maxint));

		return maxint;

	}

}
