package com.comino.flight.observables;

import org.mavlink.messages.MAV_SEVERITY;

import com.comino.flight.model.AnalysisDataModel;
import com.comino.flight.model.service.AnalysisModelService;
import com.comino.flight.prefs.MAVPreferences;
import com.comino.mavcom.control.IMAVController;
import com.comino.mavcom.model.DataModel;
import com.comino.mavutils.workqueue.WorkQueue;
import com.comino.speech.VoiceTTS;

public class VoiceHandler {
	
	private static VoiceHandler instance;

	private final WorkQueue wq = WorkQueue.getInstance();
	private final AnalysisDataModel model;
	private final StateProperties properties;
	private final VoiceTTS voice;
	private final boolean  enabled;

	public static VoiceHandler getInstance(IMAVController control) {
		if(instance==null)
			instance = new VoiceHandler(control);
		return instance;
	}

	private VoiceHandler(IMAVController control) {

		this.model = AnalysisModelService.getInstance().getCurrent();
		this.properties = StateProperties.getInstance();
		this.voice = VoiceTTS.getInstance();
		this.enabled = MAVPreferences.getInstance().getBoolean("SPEECH", false);

		if(enabled) {
			control.addMAVMessageListener(msg -> {
				if(msg.severity != MAV_SEVERITY.MAV_SEVERITY_EMERGENCY && msg.severity != MAV_SEVERITY.MAV_SEVERITY_CRITICAL)
					return;
				if(msg.text.contains("]"))
					voice.talk(msg.text.substring(msg.text.indexOf(']')));
			});
		}
		
		

		// report battery status every 60 seconds if armed
		wq.addCyclicTask("LP", 60000, () -> {
			if(!properties.getArmedProperty().get())
				return;
			String s = String.format("Battery is at %.0f percent.",model.getValue("BATP")*100f);
			System.out.println(s);
			if(enabled)
				voice.talk(s);
		});
		
		wq.addCyclicTask("LP", 45000, () -> {
			if(!properties.getArmedProperty().get())
				return;
			String s = String.format("Relative altitude is %.1f meters",model.getValue("ALTRE"));
			System.out.println(s);
			if(enabled)
				voice.talk(s);
		});



	}

}
