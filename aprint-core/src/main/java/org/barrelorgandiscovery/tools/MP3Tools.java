package org.barrelorgandiscovery.tools;

public class MP3Tools {

	// don't use MP3 because of license conflicts

	public static void convert(String wavfile, String mp3file) throws Exception {
		throw new Exception("not implemented");
	}

	// public static void init() throws Exception {
	// JNIEasy jniEasy = JNIEasy.get();
	// // jniEasy.setFeature("jnieasy.license.dir","a path");
	// jniEasy.load();
	//
	// NativeTypeManager typeMgr = jniEasy.getTypeManager();
	// String osName = System.getProperty("os.name");
	// if (osName.startsWith("Windows"))
	// typeMgr.defineMacro("Windows");
	// else if (osName.startsWith("Mac OS X"))
	// typeMgr.defineMacro("MacOSX");
	// else if (osName.startsWith("Linux"))
	// typeMgr.defineMacro("Linux");
	// else if (osName.startsWith("SunOS"))
	// typeMgr.defineMacro("SunOS");
	// else
	// throw new RuntimeException("Platform not supported");
	// }
	//
	// public static void convert(String wavFile, String mp3File) throws
	// Exception {
	// //
	// view-source:http://stuff.mit.edu/afs/sipb/user/gamache/src/lame-3.91/Dll/BladeMP3EncDLL.c
	// //
	// http://www.koders.com/cpp/fid8B71B2E32247F195DA00237E702C5031468A55C1.aspx?s=lame_encode_buffer
	//
	// new File(mp3File).delete();
	//
	// lame_global_flags flags = Lame.lame_init();
	//
	// System.out.println("LAME Version: " + Lame.get_lame_version());
	// System.out.println("Home page: " + Lame.get_lame_url());
	//
	// int rc;
	// rc = Lame.lame_set_error_protection(flags, 1);
	// checkError(rc);
	//
	// rc = Lame.lame_set_num_channels(flags, 2);
	// checkError(rc);
	// rc = Lame.lame_set_mode(flags, MPEG_mode.JOINT_STEREO);
	// checkError(rc);
	// rc = Lame.lame_set_in_samplerate(flags, 44100);
	// checkError(rc);
	//
	// rc = Lame.lame_set_out_samplerate(flags, 0); // LAME selects
	// checkError(rc);
	// rc = Lame.lame_set_disable_reservoir(flags, 1);
	// checkError(rc);
	// rc = Lame.lame_set_padding_type(flags, Padding_type.PAD_NO);
	// checkError(rc);
	// rc = Lame.lame_set_bWriteVbrTag(flags, 1);
	// checkError(rc);
	//
	// rc = Lame.lame_set_brate(flags, 128);
	// checkError(rc);
	// rc = Lame.lame_set_quality(flags, 2);
	// checkError(rc);
	// rc = Lame.lame_set_original(flags, 1);
	// checkError(rc);
	//
	// rc = Lame.lame_init_params(flags);
	// checkError(rc);
	//
	// /*
	// * LameMsgCallbackTest cb = new LameMsgCallbackTest();
	// *
	// * Lame.lame_set_errorf(flags,cb); Lame.lame_set_debugf(flags,cb);
	// * Lame.lame_set_msgf(flags,cb);
	// *
	// * Lame.lame_print_config(flags);
	// */
	// int numChannels = Lame.lame_get_num_channels(flags);
	//
	// // For MPEG-I, 1152 samples per frame per channel
	// int mSamplesPerFrameAndChannel = 1152;
	// int mSamplesPerFrame = 1152 * numChannels;
	//
	// // Worst case MPEG-I (see lame.h/lame_encode_buffer())
	// int mOutBufferSize = mSamplesPerFrame * (320 / 8) / 8 + 4 * 1152
	// * (320 / 8) / 8 + 512;
	// // int mOutBufferSize = (int)(1.25 * ( mSamplesPerFrame /
	// // Lame.lame_get_num_channels(flags) ) + 7200);
	//
	// // Allocate buffers
	// // short[] pWAVBuffer = new short[mSamplesPerFrame];
	// byte[] pWAVBuffer = new byte[mSamplesPerFrame * 2]; // 2 bytes per short
	// byte[] pMP3Buffer = new byte[mOutBufferSize];
	//
	// // WAV file supposed 44100 Hz, Stereo, 16 bits
	// BufferedInputStream wavStream = new BufferedInputStream(
	// new FileInputStream(wavFile));
	// BufferedOutputStream mp3Stream = new BufferedOutputStream(
	// new FileOutputStream(mp3File));
	//
	// wavStream.skip(44); // Skipping the WAV header
	//
	// // Convert All PCM samples
	// int read;
	// while ((read = wavStream.read(pWAVBuffer, 0, pWAVBuffer.length)) > 0) {
	// // Encode samples
	// // read is "bytes", each sample is 2 bytes
	// int readSamplesAllChannels = read / 2;
	// int readSamples = readSamplesAllChannels / numChannels;
	// int nOutputBytes = Lame.lame_encode_buffer_interleaved(flags,
	// pWAVBuffer, readSamples, pMP3Buffer, 0);
	//
	// // write nOutputBytes bytes that are returned in the pMP3Buffer to
	// // disk
	// mp3Stream.write(pMP3Buffer, 0, nOutputBytes);
	// }
	//
	// int nOutputBytes = Lame.lame_encode_flush_nogap(flags, pMP3Buffer, 0);
	// // Are there any bytes pending?
	// // If so, write them to disk
	// if (nOutputBytes != 0)
	// mp3Stream.write(pMP3Buffer, 0, nOutputBytes);
	//
	// mp3Stream.flush();
	//
	// wavStream.close();
	// mp3Stream.close();
	//
	// if (Lame.lame_get_bWriteVbrTag(flags) > 0) {
	// FileUtil.FILE fpStream = FileUtil.fopen(mp3File, "rb+");
	//
	// Lame.lame_mp3_tags_fid(flags, fpStream);
	//
	// FileUtil.fclose(fpStream);
	// }
	//
	// Lame.lame_close(flags);
	//
	// // Test.checkFiles(mp3File,mp3RefFile,false);
	// }
	//
	// private static void checkError(int err) {
	// if (err != lame_errorcodes_t.LAME_OKAY)
	// throw new RuntimeException("ERROR " + err);
	// }
	//
	// public static void main(String[] args) throws Exception {
	// init();
	// convert(
	// "C:\\Documents and Settings\\Freydiere Patrice\\Local
	// Settings\\Temp\\temp41878wav",
	// "c:\\test.mp3");
	// }
}
