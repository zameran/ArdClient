package haven.res.ui.music;

import org.json.JSONArray;

import java.util.ArrayList;

public class midiTrack {
	public int number;
	public int size, normalSize;
	public ArrayList<JSONArray> notes = new ArrayList<>();
	public ArrayList<midiNote> normalNotes = new ArrayList<>();

	public int[] channelAmount = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	public ArrayList<Integer> currentTrackChannelAmount = new ArrayList<Integer>();

	public midiTrack(int number, int size, ArrayList<JSONArray> notes) {
		this.number = number;
		this.size = size;
		this.notes = notes;

		for (int i = 0; i < notes.size(); i++) {
			if (this.notes.get(i).length() == 5) {
				Integer tick = (Integer) this.notes.get(i).getJSONObject(1).get("tick");
				Integer channel = (Integer) this.notes.get(i).getJSONObject(2).get("channel");
				Integer key = (Integer) this.notes.get(i).getJSONObject(3).get("key");
				Integer velocity = (Integer) this.notes.get(i).getJSONObject(4).get("velocity");
				Integer msg = (Integer) this.notes.get(i).getJSONObject(0).get("msg");

				channelAmount[channel]++;

				this.normalNotes.add(new midiNote(tick, channel, key, velocity, msg));
//				logingln(tick + " " + channel + " " + key + " " + velocity);
			}
		}

		if (normalNotes == null)
			this.normalSize = 0;
		else
			this.normalSize = normalNotes.size();

		for (int i = 0; i < channelAmount.length; i++) {
//			loging(channelAmount[i] + " ");
		}
//		logingln(this.number + " " + this.size + " " + this.normalSize + " " + this.normalNotes);
	}

	public midiTrack(ArrayList<midiNote> normalNotes, int number, int size) {
		this.number = number;
		this.size = size;
		this.normalNotes = normalNotes;

		if (normalNotes == null)
			this.normalSize = 0;
		else
			this.normalSize = normalNotes.size();

		for (int i = 0; i < channelAmount.length; i++) {
//			loging(channelAmount[i] + " ");
		}
//		logingln(this.number + " " + this.size + " " + this.normalSize + " " + this.normalNotes);
	}
}
