package haven.purus;

import static haven.OCache.posres;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import haven.*;
import haven.automation.GobSelectCallback;

public class StockpileFiller extends Window implements GobSelectCallback, ItemClickCallback {

	private ArrayList<Gob> stockpiles = new ArrayList<Gob>();
	private String invobj, terobj;

	private boolean stop = false;
	private boolean terobjCallback = false;
	private Button clearBtn,startBtn;

	public StockpileFiller() {
		super(new Coord(270, 200), "Stockpile Filler");

		Widget inf = add(new Widget(new Coord(245, 30)) {
			public void draw(GOut g) {
				g.chcolor(0, 0, 0, 128);
				g.frect(Coord.z, sz);
				g.chcolor();
				super.draw(g);
			}

		}, new Coord(10, 10).add(wbox.btloff()));
		Frame.around(this, Collections.singletonList(inf));
		Label infolbl = inf.add(new Label("Alt + Click to select stockpiles"), new Coord(5, 0));
		int y = 40;
		Button invobjBtn = new Button(140, "Choose item from inventory") {
			@Override
			public void click() {
				BotUtils.sysMsg("Click the stockpile item in inventory", Color.GREEN);
				registerItemCallback();
			}
		};
		add(invobjBtn, new Coord(20, y));
		y += 35;
		Button terobjBtn = new Button(140, "Choose item from ground") {
			@Override
			public void click() {
				terobjCallback = true;
				BotUtils.sysMsg("Alt + Click to select ground item", Color.GREEN);
			}
		};
		add(terobjBtn, new Coord(20, y));
		y += 35;
		clearBtn = new Button(140, "Clear/Stop") {
			@Override
			public void click() {
				startBtn.show();
				stop = true;
				if (t!=null)
					t.interrupt();
				stockpiles.clear();
				BotUtils.sysMsg("Cleared the list of selected stockpiles", Color.GREEN);
			}
		};
		add(clearBtn, new Coord(20, y));
		y += 35;
		startBtn = new Button(140, "Start") {
			@Override
			public void click() {
				this.hide();
				stop = false;
				if(stockpiles.isEmpty()) {
					BotUtils.sysMsg("No stockpiles chosen!", Color.GREEN);
				} else if(terobj==null) {
					BotUtils.sysMsg("No ground item chosen!", Color.GREEN);
				} else if(invobj==null) {
					BotUtils.sysMsg("No inventory item chosen!", Color.GREEN);
				} else {
				 t.start();
				}
			}
		};
		add(startBtn, new Coord(20, y));
		y += 35;
	}

	Thread t = new Thread(new Runnable() {
		public void run() {
			try {
				while (BotUtils.findObjectByNames(5000, terobj) != null) {
					System.out.println("In main loop");
					if (stop)
						break;
					/*Gob gob = BotUtils.findObjectByNames(1000, terobj);
					if (gob == null) {
						BotUtils.sysMsg("No more items on ground found!", Color.GREEN);
						break;
					}
					while (gob != null && BotUtils.getItemAtHand() == null) {
						if (stop)
							break;
						BotUtils.doClick(gob, 3, 1);
						while (BotUtils.findObjectById(gob.id) != null && BotUtils.getItemAtHand() == null) {
							System.out.println("waiting to pickup item");
							BotUtils.sleep(100);
						}
						gob = BotUtils.findObjectByNames(1000, terobj);
					}*/

				//	while (BotUtils.invFreeSlots() > 0) {
					while(BotUtils.getItemAtHand() == null){
						if (stop)
							break;
						if (BotUtils.findObjectByNames(5000, terobj) == null) {
							BotUtils.sysLogAppend("Out of items to stockpile, finishing.", "white");
							stop = true;
							break;
						}
						BotUtils.sysLogAppend("Grabbing stuff.", "white");
						Gob g = BotUtils.findObjectByNames(5000, terobj);
						//if (g == null) {
						//	BotUtils.sysMsg("No more items on ground found!", Color.GREEN);
							//break;
						//}
						gameui().map.wdgmsg("click", g.sc, g.rc.floor(posres), 3, 1, 0, (int) g.id, g.rc.floor(posres), 0, -1);
						BotUtils.sleep(1000);
						//	gui.map.wdgmsg("click", cistern.sc, cistern.rc.floor(posres), 3, 0, 0, (int) cistern.id, cistern.rc.floor(posres), 0, -1);
						//BotUtils.pfRightClick(g, 0);
						while(BotUtils.getItemAtHand() == null & BotUtils.findObjectByNames(5000,terobj)!=null && BotUtils.isMoving()) {
						System.out.println("waiting for item on  hand");
							BotUtils.sleep(10);
						}
						//while(BotUtils.invFreeSlots() > 0 && BotUtils.findObjectByNames(1000,terobj)!= null)
						//	BotUtils.sleep(10);
						System.out.println("inv free slots : "+BotUtils.invFreeSlots());
					}

					BotUtils.sysLogAppend("Done Grabbing stuff.", "white");
					if (stop)
						break;

					//if (BotUtils.getItemAtHand() == null && BotUtils.getInventoryItemsByName(BotUtils.playerInventory(), invobj).size() > 0) {
					//	BotUtils.takeItem(BotUtils.getInventoryItemsByName(BotUtils.playerInventory(), invobj).get(0).item);
					//	}
					while (BotUtils.getInventoryItemsByName(BotUtils.playerInventory(), invobj).size() != 0 && !stop) {
						if (stop)
							break;
						System.out.println("In stockpile loop");
						BotUtils.sleep(1000);
						if (BotUtils.getItemAtHand() != null)
							BotUtils.dropItem(0);
						if (stockpiles.isEmpty()) {
							System.out.println("Stockpiles empty");
							BotUtils.sysMsg("All chosen stockpiles full!", Color.GREEN);
							stop = true;
							break;
						}

						if (BotUtils.stockpileIsFull(BotUtils.findObjectById(stockpiles.get(0).id))) {
							System.out.println("Stockpile full");
							stockpiles.remove(0);
							continue;
						}
						if (stop)
							break;
						if(stockpiles.size() == 0){
							BotUtils.sysMsg("Stockpile list now empty, stopping.",Color.white);
							stop = true;
							stop();
						}
						BotUtils.pfRightClick(stockpiles.get(0), 0);
						int retry = 0;
						while (gameui().getwnd("Stockpile") == null) {
							if(!BotUtils.isMoving())
							retry++;
							if (retry > 100) {
								if (stop)
									break;
								retry = 0;
								System.out.println("Retry : "+retry);
								BotUtils.sysLogAppend("Retrying stockpile interaction", "white");
								BotUtils.dropItem(0);
								BotUtils.pfRightClick(stockpiles.get(0), 0);
							}
							BotUtils.sleep(10);
						}
						BotUtils.sleep(1000);
						System.out.println("clicking stockpile");
						while (BotUtils.getItemAtHand() == null)
							BotUtils.takeItem(BotUtils.getInventoryItemsByName(BotUtils.playerInventory(), invobj).get(0).item);
						int cnt = BotUtils.invFreeSlots();
						try {
							BotUtils.gui.map.wdgmsg("itemact", Coord.z, stockpiles.get(0).rc.floor(posres), 1, 0, (int) stockpiles.get(0).id, stockpiles.get(0).rc.floor(posres), 0, -1);
						}catch(IndexOutOfBoundsException lolindexes){
							BotUtils.sysMsg("Critical error in stockpile list, stopping thread to prevent crash.",Color.white);
							stop = true;
							stop();
						}
						while (BotUtils.invFreeSlots() == cnt) {
							System.out.println("waiting for inv update");
							BotUtils.sleep(100);
						}
						//if (BotUtils.getItemAtHand() == null && BotUtils.getInventoryItemsByName(BotUtils.playerInventory(), invobj).size() > 0) {
						//	System.out.println("grabbing new item");
						//	BotUtils.takeItem(BotUtils.getInventoryItemsByName(BotUtils.playerInventory(), invobj).get(0).item);
						//}
					}
					if (BotUtils.findObjectByNames(5000, terobj) == null)
						break;
				}
				BotUtils.sysMsg("Stockpile Filler finished!", Color.GREEN);
				startBtn.show();
				reqdestroy();
			}catch(Loading | NullPointerException q){}
		}
	});
	
	private void registerItemCallback() {
		synchronized (GobSelectCallback.class) {
    		BotUtils.gui.registerItemCallback(this);
    	}
	}

	@Override
	public void gobselect(Gob gob) {
		if(terobjCallback) {
			terobjCallback = false;
			terobj = gob.getres().name;
			BotUtils.sysMsg("Ground item chosen!", Color.GREEN);
		} else if (gob.getres().basename().contains("stockpile")) {
			stockpiles.add(gob);
			BotUtils.sysMsg("Stockpile added to list! Total stockpiles chosen: " + stockpiles.size(), Color.GREEN);
		}
		synchronized (GobSelectCallback.class) {
    		BotUtils.gui.map.registerGobSelect(this);
    	}
	}
	
	@Override
	public void itemClick(WItem item) {
		invobj = item.item.getres().name;
		BotUtils.sysMsg("Inventory item to put in the stockpiles chosen!", Color.GREEN);
		synchronized (ItemClickCallback.class) {
    		BotUtils.gui.unregisterItemCallback();
    	}
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if (msg == "close") {
			stop();
			reqdestroy();
		} else
			super.wdgmsg(sender, msg, args);
	}

	public void stop() {
		t.interrupt();
		stop = true;
		synchronized (ItemClickCallback.class) {
    		BotUtils.gui.unregisterItemCallback();
    	}
		synchronized (ItemClickCallback.class) {
    		BotUtils.gui.unregisterItemCallback();
    	}
		reqdestroy();
		//gameui().map.wdgmsg("click", Coord.z, new Coord((int)BotUtils.player().rc.x, (int)BotUtils.player().rc.y), 1, 0);
	}
}