package org.barrelorgandiscovery.xml;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractRegisterCommandDef;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.ReferencedPercussion;
import org.barrelorgandiscovery.scale.ReferencedPercussionList;
import org.barrelorgandiscovery.scale.RegisterCommandStartDef;
import org.barrelorgandiscovery.scale.RegisterSetCommandResetDef;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.MarkerEvent;
import org.barrelorgandiscovery.virtualbook.SignatureEvent;
import org.barrelorgandiscovery.virtualbook.TempoChangeEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRenderingFactory;
import org.barrelorgandiscovery.virtualbook.x2016.Annotation;
import org.barrelorgandiscovery.virtualbook.x2016.Holes;
import org.barrelorgandiscovery.virtualbook.x2016.MarkerAnnotation;
import org.barrelorgandiscovery.virtualbook.x2016.PipeStop;
import org.barrelorgandiscovery.virtualbook.x2016.PipeStopSet;
import org.barrelorgandiscovery.virtualbook.x2016.Scale;
import org.barrelorgandiscovery.virtualbook.x2016.ScaleDefinition;
import org.barrelorgandiscovery.virtualbook.x2016.ScaleDocument;
import org.barrelorgandiscovery.virtualbook.x2016.ScaleInformations;
import org.barrelorgandiscovery.virtualbook.x2016.SignatureAnnotation;
import org.barrelorgandiscovery.virtualbook.x2016.TempoAnnotation;
import org.barrelorgandiscovery.virtualbook.x2016.TrackCommandDef;
import org.barrelorgandiscovery.virtualbook.x2016.TrackDef;
import org.barrelorgandiscovery.virtualbook.x2016.TrackDrum;
import org.barrelorgandiscovery.virtualbook.x2016.TrackNoteDef;
import org.barrelorgandiscovery.virtualbook.x2016.TrackRegisterControlResetDef;
import org.barrelorgandiscovery.virtualbook.x2016.TrackRegisterControlStartDef;
import org.barrelorgandiscovery.virtualbook.x2016.TracksDefinition;
import org.barrelorgandiscovery.virtualbook.x2016.VirtualBookDocument;
import org.barrelorgandiscovery.virtualbook.x2016.VirtualBookMetadata;

/**
 * XML to Virtual Book converter
 * 
 * @author use
 * 
 */
public class XMLConverter2016 {

	private static Logger logger = Logger.getLogger(XMLConverter2016.class);

	public static ScaleDocument toScaleDocument(
			org.barrelorgandiscovery.scale.Scale scale) throws Exception {

		ScaleDocument scaledoc = ScaleDocument.Factory.newInstance();
		Scale xmlscale = scaledoc.addNewScale();

		populateXmlScale(scale, xmlscale);
		return scaledoc;
	}

	public static Scale toScale(org.barrelorgandiscovery.scale.Scale scale)
			throws Exception {

		Scale xmlscale = Scale.Factory.newInstance();

		populateXmlScale(scale, xmlscale);

		return xmlscale;
	}

	/**
	 * populate a scla xml node from a scale
	 * 
	 * @param scale
	 * @param xmlscale
	 * @throws Exception
	 */
	private static void populateXmlScale(
			org.barrelorgandiscovery.scale.Scale scale, Scale xmlscale)
			throws Exception {

		xmlscale.setName(scale.getName());
		ScaleInformations scaleinformation = xmlscale.addNewInfos();
		scaleinformation.setContact(scale.getContact());
		if (scale.getInformations() != null)
			scaleinformation.setDescription(scale.getInformations());
		scaleinformation.setState(scale.getState());

		ScaleDefinition scaledefinition = xmlscale.addNewDefinition();
		scaledefinition.setIntertrackdistance(scale.getIntertrackHeight());
		scaledefinition.setDefaulttrackheight(scale.getTrackWidth());
		scaledefinition.setSpeed(scale.getSpeed());
		scaledefinition.setWidth(scale.getWidth());
		scaledefinition.setFirsttrackdistance(scale.getFirstTrackAxis());
		scaledefinition.setTracknb(scale.getTrackNb());

		if (scale.getRendering() != null)
			scaledefinition.setScaletype(scale.getRendering().getName());

		if (scale.isPreferredViewedInversed())
			scaledefinition.setIspreferredviewinverted(true);
		
		if (scale.isBookMovingRightToLeft())
			scaledefinition.setBookmovefromrighttoleft(true);

		
		
		PipeStopGroupList pipestoplist = scale.getPipeStopGroupList();

		if (pipestoplist != null && pipestoplist.size() > 0) {

			logger.debug("adding pipestops ... ");

			for (Iterator iterator = pipestoplist.iterator(); iterator
					.hasNext();) {
				PipeStopGroup g = (PipeStopGroup) iterator.next();

				// adding registers ....
				PipeStopSet pss = scaledefinition.addNewPipestopsets();

				pss.setName(g.getName());

				org.barrelorgandiscovery.scale.PipeStop[] pipestops = g
						.getPipeStops();

				ArrayList<PipeStop> psret = new ArrayList<PipeStop>();
				for (int i = 0; i < pipestops.length; i++) {
					org.barrelorgandiscovery.scale.PipeStop p = pipestops[i];
					PipeStop psr = PipeStop.Factory.newInstance();
					psr.setName(p.getName());
					psr.setIsPartOfRegister(p.isRegisteredControlled());

					psret.add(psr);
				}

				pss.setPipestopArray(psret.toArray(new PipeStop[0]));

			}

		}

		TracksDefinition tracksdefinition = scaledefinition.addNewTracks();
		AbstractTrackDef[] defs = scale.getTracksDefinition();
		for (int i = 0; i < defs.length; i++) {
			AbstractTrackDef trackdef = defs[i];

			if (trackdef == null)
				continue;

			assert trackdef != null;

			if (trackdef instanceof NoteDef) {

				NoteDef cnd = (NoteDef) trackdef;

				TrackDef newtrack = tracksdefinition.addNewTrack();

				TrackNoteDef tnd = (TrackNoteDef) newtrack
						.changeType(TrackNoteDef.type);

				tnd.setNote(MidiHelper.midiLibelle(cnd.getMidiNote()));
				tnd.setPipestopsetname(cnd.getRegisterSetName());
				tnd.setNo(i);

			} else if (trackdef instanceof PercussionDef) {

				PercussionDef cnd = (PercussionDef) trackdef;

				ReferencedPercussion r = ReferencedPercussionList
						.findReferencedPercussionByMidiCode(cnd.getPercussion());

				if (r != null) {

					TrackDef newtrack = tracksdefinition.addNewTrack();
					TrackDrum tnd = (TrackDrum) newtrack
							.changeType(TrackDrum.type);

					tnd.setMididef(r.getNamecode());

					if (!Double.isNaN(cnd.getRetard()))
						tnd.setDelay(cnd.getRetard());
					if (!Double.isNaN(cnd.getLength()))
						tnd.setFixedlength(cnd.getLength());

					tnd.setNo(i);
				} else {
					logger.error("fail to find percussion :" + cnd);
				}
			} else if (trackdef instanceof AbstractRegisterCommandDef) {
				AbstractRegisterCommandDef ctd = (AbstractRegisterCommandDef) trackdef;

				double length = ctd.getLength();
				double delay = ctd.getRetard();

				if (trackdef instanceof RegisterCommandStartDef) {

					RegisterCommandStartDef rcsd = (RegisterCommandStartDef) trackdef;

					TrackDef newtrack = tracksdefinition.addNewTrack();
					TrackRegisterControlStartDef tnd = (TrackRegisterControlStartDef) newtrack
							.changeType(TrackRegisterControlStartDef.type);

					if (!Double.isNaN(delay))
						tnd.setDelay(delay);
					if (!Double.isNaN(length))
						tnd.setFixedlength(length);

					tnd.setNo(i);
					tnd.setPipestopsetname(rcsd.getRegisterSetName());
					tnd.setPipestopnameinset(rcsd.getRegisterInRegisterSet());

				} else if (trackdef instanceof RegisterSetCommandResetDef) {

					RegisterSetCommandResetDef rr = (RegisterSetCommandResetDef) trackdef;

					TrackDef newtrack = tracksdefinition.addNewTrack();
					TrackRegisterControlResetDef tnd = (TrackRegisterControlResetDef) newtrack
							.changeType(TrackRegisterControlResetDef.type);

					if (!Double.isNaN(delay))
						tnd.setDelay(delay);
					if (!Double.isNaN(length))
						tnd.setFixedlength(length);

					if (rr.getRegisterSet() == null) {
						tnd.setResetall(true);
					} else {
						tnd.setResetpipestopsetname(rr.getRegisterSet());
					}

					tnd.setNo(i);

				} else {
					throw new Exception("unsupported trackedef "
							+ trackdef.getClass().getName() + " "
							+ trackdef.toString());
				}

			} else {
				throw new Exception("unsupported trackedef "
						+ trackdef.getClass().getName() + " "
						+ trackdef.toString());
			}

		}
	}

	/**
	 * Convert the virtual book in an xml document ..
	 * 
	 * @param vb
	 *            the virtual book to convert
	 * @return
	 * @throws Exception
	 */
	public static VirtualBookDocument toVirtualBookDocument(VirtualBook vb)
			throws Exception {

		VirtualBookDocument xmlvbdoc = VirtualBookDocument.Factory
				.newInstance();

		org.barrelorgandiscovery.virtualbook.x2016.VirtualBook xmlvb = xmlvbdoc
				.addNewVirtualBook();

		Scale xmlscale = toScale(vb.getScale());

		xmlvb.setScale(xmlscale);
		xmlvb.setTitle(vb.getName());

		if (xmlvb.getMetadata() == null) {
			xmlvb.addNewMetadata();
		}

		logger.debug("fill metadata");

		VirtualBookMetadata xmlmetadata = xmlvb.getMetadata();

		if (vb.getMetadata() != null) {
			org.barrelorgandiscovery.virtualbook.VirtualBookMetadata mt = vb
					.getMetadata();

			if (mt.getLastModifiedDate() != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(mt.getLastModifiedDate());
				xmlmetadata.setLastModificationDate(c);
			}

			if (mt.getCreationDate() != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(mt.getCreationDate());
				xmlmetadata.setCreationDate(c);
			}

			xmlmetadata.setGenre(mt.getGenre());
			xmlmetadata.setAuthor(mt.getAuthor());
			xmlmetadata.setArranger(mt.getArranger());
			xmlmetadata.setDescription(mt.getDescription());
			
			if (mt.getCover() != null) {
				Image cover = mt.getCover();
				BufferedImage bi = ImageTools.loadImage(cover);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageTools.saveJpeg(bi, baos);

				xmlvb.setFrontimage(baos.toByteArray());
			}

		}

		logger.debug("fill holes");

		List<Hole> holes = vb.getOrderedHolesCopy();

		Holes xmlholes = xmlvb.addNewHoles();
		for (Iterator iterator = holes.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();

			org.barrelorgandiscovery.virtualbook.x2016.Hole xmlhole = xmlholes
					.addNewHole();
			xmlhole.setTimestamp(hole.getTimestamp());
			xmlhole.setLength(hole.getTimeLength());
			xmlhole.setTrack(hole.getTrack());
		}

		logger.debug("events handling");

		// Write the ordered events
		Set<AbstractEvent> orderedEventsByRef = vb.getOrderedEventsByRef();
		if (orderedEventsByRef != null) {
			for (Iterator iterator = orderedEventsByRef.iterator(); iterator
					.hasNext();) {
				AbstractEvent abstractEvent = (AbstractEvent) iterator.next();

				if (abstractEvent instanceof SignatureEvent) {

					SignatureEvent sigevent = (SignatureEvent) abstractEvent;

					Annotation an = xmlvb.addNewAnnotations();
					SignatureAnnotation sig = (SignatureAnnotation) an
							.changeType(SignatureAnnotation.type);

					sig.setTimestamp(sigevent.getTimestamp());
					sig.setNumerator(sigevent.getNumerateur());
					sig.setDenominator(sigevent.getDenominateur());

				} else if (abstractEvent instanceof TempoChangeEvent) {

					TempoChangeEvent tce = (TempoChangeEvent) abstractEvent;
					Annotation an = xmlvb.addNewAnnotations();
					TempoAnnotation ta = (TempoAnnotation) an
							.changeType(TempoAnnotation.type);

					ta.setTimestamp(tce.getTimestamp());
					ta.setBeatlength(tce.getNoirLength());

				} else if (abstractEvent instanceof MarkerEvent) {

					MarkerEvent me = (MarkerEvent) abstractEvent;
					Annotation an = xmlvb.addNewAnnotations();
					MarkerAnnotation ta = (MarkerAnnotation) an
							.changeType(MarkerAnnotation.type);

					ta.setTimestamp(me.getTimestamp());
					ta.setName(me.getMarkerName());

				} else {
					logger.warn("unsupported stored event :" + abstractEvent);
				}

			}
		}

		return xmlvbdoc;

	}

	public static AbstractTrackDef fromTrack(TrackDef td) throws Exception {

		if (td == null)
			return null;

		AbstractTrackDef retvalue = null;

		if (td instanceof TrackCommandDef) {

			if (td instanceof TrackDrum) {

				TrackDrum trackDrum = (TrackDrum) td;

				logger.warn("match search from the drum name not implemented");

				int midicode = ReferencedPercussionList
						.findReferencePercussionByName(trackDrum.getMididef());
				if (midicode == -1)
					throw new Exception(
							"cannot find the midi code for percussion associated to "
									+ trackDrum.getMididef());

				retvalue = new PercussionDef(midicode, trackDrum.getDelay(),
						trackDrum.getFixedlength());

				// search the drum from the name ....

			} else if (td instanceof TrackRegisterControlResetDef) {

				TrackRegisterControlResetDef trackRegisterControlResetDef = (TrackRegisterControlResetDef) td;

				retvalue = new RegisterSetCommandResetDef(
						trackRegisterControlResetDef.getResetpipestopsetname(),
						trackRegisterControlResetDef.getDelay(),
						trackRegisterControlResetDef.getFixedlength());

			} else if (td instanceof TrackRegisterControlStartDef) {

				TrackRegisterControlStartDef registerControlStartDef = (TrackRegisterControlStartDef) td;

				retvalue = new RegisterCommandStartDef(
						registerControlStartDef.getPipestopsetname(),
						registerControlStartDef.getPipestopnameinset(),
						registerControlStartDef.getDelay(),
						registerControlStartDef.getFixedlength());

			} else {
				throw new Exception("track definition " + td.xmlText()
						+ " unknown");
			}

		} else if (td instanceof TrackNoteDef) {

			TrackNoteDef trackNoteDef = (TrackNoteDef) td;

			retvalue = new NoteDef(MidiHelper.midiCode(trackNoteDef.getNote()),
					trackNoteDef.getPipestopsetname());

		} else {
			throw new Exception("track definition " + td.xmlText() + " unknown");
		}

		return retvalue;

	}

	public static org.barrelorgandiscovery.scale.Scale fromScale(
			org.barrelorgandiscovery.virtualbook.x2016.Scale scale)
			throws Exception {

		String scalename = scale.getName();
		ScaleDefinition definition = scale.getDefinition();
		TracksDefinition tracks = definition.getTracks();
		double firsttrackdistance = definition.getFirsttrackdistance();
		double intertrackdistance = definition.getIntertrackdistance();
		double defaulttrackheight = definition.getDefaulttrackheight();
		double speed = definition.getSpeed();
		double width = definition.getWidth();
		boolean ispreferredviewinverted = definition
				.getIspreferredviewinverted();
		
		boolean bookMovingFromRightToLeft = definition.getBookmovefromrighttoleft();
		
		TrackDef[] trackArray = tracks.getTrackArray();
		
		
		int maxtrack = -1;
		
		if (definition.isSetTracknb()) {
			maxtrack = definition.getTracknb();
		} else {
			// may not be used any more
			logger.debug("compute the track number from tracks definition");
			for (TrackDef t : trackArray) {
				int tn = t.getNo();
				maxtrack = Math.max(tn, maxtrack);
			}
		}

		logger.debug("max track :" + maxtrack);
		AbstractTrackDef[] tdefinitions = new AbstractTrackDef[maxtrack + 1];
		for (TrackDef t : trackArray) {
			AbstractTrackDef fromTrack = fromTrack(t);
			logger.debug("insert at " + (t.getNo() - 1));
			tdefinitions[t.getNo()] = fromTrack;
		}

		logger.debug("reading pipestopdefinition");

		PipeStopGroupList psgl = null;

		PipeStopSet[] pipestopsetsArray = definition.getPipestopsetsArray();
		if (logger.isDebugEnabled()) {
			logger.debug("pipestopArray :" + pipestopsetsArray);
			for (int i = 0; i < pipestopsetsArray.length; i++) {
				PipeStopSet pipeStopSet = pipestopsetsArray[i];
				logger.debug("pipestopset " + pipeStopSet.toString());
			}
		}
		if (pipestopsetsArray != null) {

			psgl = new PipeStopGroupList();

			for (int i = 0; i < pipestopsetsArray.length; i++) {
				PipeStopSet pipeStopSet = pipestopsetsArray[i];

				String pipeStopGroupName = pipeStopSet.getName();
				logger.debug("analysing pipestopset :" + pipeStopGroupName);

				PipeStop[] pipestoparray = pipeStopSet.getPipestopArray();
				logger.debug("pipestop array :" + pipestoparray);

				ArrayList<org.barrelorgandiscovery.scale.PipeStop> retpipestop = new ArrayList<org.barrelorgandiscovery.scale.PipeStop>();

				if (pipestoparray != null) {
					for (int j = 0; j < pipestoparray.length; j++) {
						PipeStop pipeStop = pipestoparray[j];

						org.barrelorgandiscovery.scale.PipeStop p = new org.barrelorgandiscovery.scale.PipeStop(
								pipeStop.getName(),
								pipeStop.getIsPartOfRegister());
						retpipestop.add(p);
					}
				}

				PipeStopGroup g = new PipeStopGroup(
						pipeStopGroupName,
						retpipestop
								.toArray(new org.barrelorgandiscovery.scale.PipeStop[0]));

				if (psgl == null)
					psgl = new PipeStopGroupList();

				psgl.put(g);

			}
		}

		VirtualBookRendering rendering = VirtualBookRenderingFactory
				.createRenderingFromName(scale.getDefinition().getScaletype());

		org.barrelorgandiscovery.scale.Scale s = new org.barrelorgandiscovery.scale.Scale(
				scalename, width, intertrackdistance, defaulttrackheight,
				firsttrackdistance, tdefinitions.length, tdefinitions, psgl,
				speed, (ConstraintList) null,
				scale.getInfos().getDescription(), scale.getInfos().getState(),
				scale.getInfos().getContact(), rendering,
				ispreferredviewinverted,bookMovingFromRightToLeft, null);

		return s;
	}

	public static VirtualBook fromVirtualBookDocument(VirtualBookDocument vbd)
			throws Exception {

		org.barrelorgandiscovery.virtualbook.x2016.VirtualBook xmlvirtualBook = vbd
				.getVirtualBook();
		org.barrelorgandiscovery.scale.Scale s = fromScale(xmlvirtualBook
				.getScale());

		logger.debug("converting holes ...");

		ArrayList<Hole> holes = new ArrayList<Hole>();

		org.barrelorgandiscovery.virtualbook.x2016.Hole[] xmlholeArray = xmlvirtualBook
				.getHoles().getHoleArray();
		for (org.barrelorgandiscovery.virtualbook.x2016.Hole h : xmlholeArray) {

			Hole hole = new Hole(h.getTrack(), h.getTimestamp(), h.getLength());
			holes.add(hole);
		}

		VirtualBook outVirtualBook = new VirtualBook(s, holes);

		outVirtualBook.setName(xmlvirtualBook.getTitle());
		
		VirtualBookMetadata xmlmetadata = xmlvirtualBook.getMetadata();
		if (xmlmetadata != null) {

			org.barrelorgandiscovery.virtualbook.VirtualBookMetadata m = new org.barrelorgandiscovery.virtualbook.VirtualBookMetadata();
			m.setArranger(xmlmetadata.getArranger());
			m.setAuthor(xmlmetadata.getAuthor());
			m.setGenre(xmlmetadata.getGenre());

			if (xmlmetadata.getCreationDate() != null) {
				Calendar c = xmlmetadata.getCreationDate();
				m.setCreationDate(new Date(c.getTimeInMillis()));
			}

			if (xmlmetadata.getLastModificationDate() != null) {
				Calendar c = xmlmetadata.getLastModificationDate();
				m.setLastModifiedDate(new Date(c.getTimeInMillis()));
			}

			m.setDescription(xmlmetadata.getDescription());
			m.setName(xmlvirtualBook.getTitle());
			outVirtualBook.setName( m.getName());
			outVirtualBook.setMetadata(m);

			byte[] frontimage = xmlvirtualBook.getFrontimage();
			if (frontimage != null) {
				try {
					BufferedImage image = ImageIO
							.read(new ByteArrayInputStream(frontimage));

					m.setCover(image);
				} catch (Exception ex) {
					logger.warn("Cannot read image in XML");
				}
			}

		}


		Annotation[] annotationsArray = xmlvirtualBook.getAnnotationsArray();
		if (annotationsArray != null) {
			logger.debug("reading annotations");
			for (int i = 0; i < annotationsArray.length; i++) {
				Annotation annotation = annotationsArray[i];
				if (annotation == null)
					continue;
				
				long ts = annotation.getTimestamp();
				if (annotation instanceof SignatureAnnotation) {

					SignatureAnnotation sig = (SignatureAnnotation) annotation;
					SignatureEvent signatureEvent = new SignatureEvent(ts,
							sig.getNumerator(), sig.getDenominator());
					if (logger.isDebugEnabled())
						logger.debug("signature event :" + signatureEvent);
					outVirtualBook.addEvent(signatureEvent);

				} else if (annotation instanceof TempoAnnotation) {
					TempoAnnotation ta = (TempoAnnotation) annotation;

					TempoChangeEvent tce = new TempoChangeEvent(ts,
							ta.getBeatlength());
					if (logger.isDebugEnabled()) {
						logger.debug("tempo change event :" + tce);
					}
					outVirtualBook.addEvent(tce);

				} else if (annotation instanceof MarkerAnnotation) {
					MarkerAnnotation ta = (MarkerAnnotation) annotation;

					MarkerEvent tce = new MarkerEvent(ts, ta.getName());
					if (logger.isDebugEnabled()) {
						logger.debug("Marker event :" + tce);
					}
					outVirtualBook.addEvent(tce);

				} else {
					logger.warn(" annotation :" + annotation
							+ " is unknown, it won't be taken into account");
				}
			}

		}

		return outVirtualBook;

	}

}
