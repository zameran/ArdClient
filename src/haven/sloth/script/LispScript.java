package haven.sloth.script;

import com.google.common.flogger.FluentLogger;
import org.armedbear.lisp.Interpreter;
import org.armedbear.lisp.Load;

//@SuppressWarnings("unused")
//public class LispScript extends Script {
//    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
//    private static final Interpreter engine = Interpreter.createInstance();
//
//    public static void reloadConfig() {
//        Load.load("data/scripts/lib/_config.lisp");
//    }
//
//    private final String script;
//
//    LispScript(final String script, final long id, final SessionDetails session) {
//        super(id, session);
//        this.script = script;
//    }
//
//    @Override
//    public String name() {
//        return script;
//    }
//
//    @Override
//    public void script_run() {
//        Load.load(String.format("data/scripts/%s.lisp", script));
//    }
//}
