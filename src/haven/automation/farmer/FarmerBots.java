package haven.automation.farmer;

import haven.Button;
import haven.Coord;
import haven.WidgetVerticalAppender;
import haven.Window;

public class FarmerBots extends Window {

    public FarmerBots() {
        super(new Coord(350, 410), "Farming Bots", "Farming Bots");
    }

    public void added() {
        final WidgetVerticalAppender appender = new WidgetVerticalAppender(this);

        Button carrotBtn = new Button(140, "Carrot", () -> startFarmer("carrot"));
        Button carrotseedBtn = new Button(140, "Carrot Seeds", () -> startFarmer("seed-carrot"));
        Button beetBtn = new Button(140, "Beetroot", () -> startFarmer("beet"));
        Button turnipBtn = new Button(140, "Turnip", () -> startFarmer("turnip"));
        Button turnipseedBtn = new Button(140, "Turnip Seeds", () -> startFarmer("seed-turnip"));
        Button onionBtn = new Button(140, "Yellow Onion", () -> startFarmer("yellowonion"));
        Button redOnionBtn = new Button(140, "Red Onion", () -> startFarmer("redonion"));
        Button leekBtn = new Button(140, "Leeks", () -> startFarmer("leek"));
        Button pumpkinBtn = new Button(140, "Pumpkin", () -> startFarmer("pumpkin"));
        Button barleyBtn = new Button(140, "Barley", () -> startFarmer("seed-barley"));
        Button wheatBtn = new Button(140, "Wheat", () -> startFarmer("seed-wheat"));
        Button milletBtn = new Button(140, "Millet", () -> startFarmer("seed-millet"));
        Button flaxBtn = new Button(140, "Flax", () -> startFarmer("seed-flax"));
        Button hempBtn = new Button(140, "Hemp", () -> startFarmer("seed-hemp"));
        Button poppyBtn = new Button(140, "Poppy", () -> startFarmer("seed-poppy"));
        Button pipeBtn = new Button(140, "Pipeweed", () -> startFarmer("seed-pipeweed"));
        Button lettuceBtn = new Button(140, "Lettuce", () -> startFarmer("seed-lettuce"));

        Button trelHarBtn = new Button(140, "Trellis harvest");
        Button trelDesBtn = new Button(140, "Trellis destroy");
        Button trelPlantBtn = new Button(140, "Trellis plant");

        appender.addRow(carrotBtn, carrotseedBtn);
        appender.add(beetBtn);
        appender.addRow(turnipBtn, turnipseedBtn);
        appender.addRow(onionBtn, redOnionBtn, leekBtn);
        appender.add(pumpkinBtn);
        appender.addRow(barleyBtn, wheatBtn, milletBtn);
        appender.addRow(flaxBtn, hempBtn);
        appender.add(poppyBtn);
        appender.add(pipeBtn);
        appender.add(lettuceBtn);
        appender.addRow(trelHarBtn, trelDesBtn, trelPlantBtn);

        pack();
    }

    public void startFarmer(String name) {
        FarmerBot bot = new FarmerBot(name);
        ui.gui.adda(bot, new Coord(ui.gui.sz.x / 2, ui.gui.sz.y / 2), 0.5, 0.5);
        bot.runner.start();

        destroy();
    }
}
