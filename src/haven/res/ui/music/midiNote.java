package haven.res.ui.music;

public class midiNote {
	int tick;
	int channel;
	int key;
	int velocity;
	int msg;

	public midiNote(int tick, int channel, int key, int velocity, int msg) {
		this.tick = tick;
		this.channel = channel;
		this.key = key;
		this.velocity = velocity;
		this.msg = msg;
	}

	public int getTick() {
		return this.tick;
	}
	public int getChannel() {
		return this.channel;
	}
	public int getKey() {
		return this.key;
	}
	public int getVelocity() {
		return this.velocity;
	}
	public int getMsg() {
		return this.msg;
	}
}
